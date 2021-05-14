package it.unipi.dii.iodetectiontest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getName();
    UpdateGUIReceiver updateGUIReceiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent i = new Intent(getApplicationContext(),IODetectionService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplicationContext().startForegroundService(i);
        }else{
            getApplicationContext().startService(i);
        }
        updateGUIReceiver = new UpdateGUIReceiver();
        IntentFilter intentFilter = new IntentFilter(IODetectionService.IODetectionAction);
        this.registerReceiver(updateGUIReceiver,intentFilter);
    }

    private class UpdateGUIReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            TextView tv = findViewById(R.id.io);
            boolean indoor = intent.getBooleanExtra("indoor",true);
            if(indoor){
                tv.setText("INDOOR");
            }else{
                tv.setText("OUTDOOR");
            }
        }
    }

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(updateGUIReceiver);
        super.onDestroy();
    }
}