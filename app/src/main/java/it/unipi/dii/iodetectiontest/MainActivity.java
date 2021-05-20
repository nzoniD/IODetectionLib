package it.unipi.dii.iodetectiontest;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import it.unipi.dii.iodetectionlib.IODetector;
import it.unipi.dii.iodetectionlib.IOStatus;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{

	private static final int REQUEST_ENABLE_BT = 0xBEEF;
	private static final int PERMISSION_REQUEST_CODE = 0xC0DE;
	private static final String TAG = MainActivity.class.getName();
	private final BroadcastReceiver ioReceiver = new BroadcastReceiver()
	{
		@SuppressLint("SetTextI18n")
		@Override
		public void onReceive(Context context, Intent intent)
		{
			TextView tv = findViewById(R.id.io);
			TextView validatedTv = findViewById(R.id.validatedIo);
			IOStatus status = (IOStatus)intent.getSerializableExtra("iostatus");
			float confidence = intent.getFloatExtra("confidence", Float.NaN);
			float raw = intent.getFloatExtra("raw", Float.NaN);
			IOStatus validatedStatus = (IOStatus)intent.getSerializableExtra("validated_iostatus");
			float validatedConfidence = intent.getFloatExtra("validated_confidence", Float.NaN);
			float validatedRaw = intent.getFloatExtra("validated_raw", Float.NaN);
			if (status == null)
				status = IOStatus.UNKNOWN;
			if (validatedStatus == null)
				validatedStatus = IOStatus.UNKNOWN;
			if (status == IOStatus.UNKNOWN)
				confidence = raw;
			if (validatedStatus == IOStatus.UNKNOWN)
				validatedConfidence = validatedRaw;
			tv.setText(status + " (" + confidence + ")");
			validatedTv.setText(validatedStatus + " (" + validatedConfidence + ")");
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		IntentFilter intentFilter = new IntentFilter(IODetectionService.IODetectionAction);
		this.registerReceiver(ioReceiver, intentFilter);
		((ToggleButton)findViewById(R.id.toggleButton)).setChecked(isServiceRunning());
	}

	private String[] filterPermissionsNotGranted(String[] permissions)
	{
		List<String> notGranted = new ArrayList<>();
		for (String permission: permissions)
			if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
				notGranted.add(permission);
			else
				Log.d(TAG, "Permission " + permission + " already granted.");
		return notGranted.toArray(new String[0]);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (grantResults.length == 0) {
			((ToggleButton)findViewById(R.id.toggleButton)).setChecked(false);
			return;
		}
		for (int i = 0; i < permissions.length; i++) {
			String permission = permissions[i];
			boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
			Log.d(TAG, permission + " " + (granted ? "granted." : "denied."));
		}

		String[] notGranted = filterPermissionsNotGranted(IODetector.getRequiredPermissions());
		if (notGranted.length > 0) {
			Log.e(TAG, "Some permissions are still not granted.");
			((ToggleButton)findViewById(R.id.toggleButton)).setChecked(false);
			return;
		}

		continueStartService();
	}

	private void startService()
	{
		Log.d(TAG, "START SERVICE");
		String[] notGranted = filterPermissionsNotGranted(IODetector.getRequiredPermissions());
		if (notGranted.length > 0)
			requestPermissions(notGranted, PERMISSION_REQUEST_CODE);
		else
			continueStartService();
	}

	private void continueStartService()
	{
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			Log.e(TAG, "Device doesn't support bluetooth.");
		} else if (!bluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}

		Intent i = new Intent(getApplicationContext(), IODetectionService.class);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			getApplicationContext().startForegroundService(i);
		} else {
			getApplicationContext().startService(i);
		}
		Log.d(TAG, "SERVICE STARTED");
		((ToggleButton)findViewById(R.id.toggleButton)).setChecked(true);
		IntentFilter intentFilter = new IntentFilter(IODetectionService.IODetectionAction);
		this.registerReceiver(ioReceiver, intentFilter);
	}

	private void stopService()
	{
		this.unregisterReceiver(ioReceiver);
		Intent i = new Intent(getApplicationContext(), IODetectionService.class);
		getApplicationContext().stopService(i);
		Log.d(TAG, "SERVICE STOPPED");
		((ToggleButton)findViewById(R.id.toggleButton)).setChecked(false);
	}

	@Override
	protected void onDestroy()
	{
		stopService();
		super.onDestroy();
	}

	@Override
	public void onClick(View v)
	{
		if (isServiceRunning())
			stopService();
		else
			startService();
	}

	private boolean isServiceRunning()
	{
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service: manager.getRunningServices(Integer.MAX_VALUE))
			if (IODetectionService.class.getName().equals(service.service.getClassName()))
				return true;
		return false;
	}
}