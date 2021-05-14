package it.unipi.dii.iodetectionlib;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BLTCounter extends BroadcastReceiver
{
    static final String TAG = BLTCounter.class.getName();
    FeatureVector featureVector = null;
    private int tmpBLTNumber;

    public BLTCounter(FeatureVector featureVector) {
        this.featureVector = featureVector;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) { // new device discovered
            Log.d(TAG, "onReceive: A new BLT device discovered");
            tmpBLTNumber += 1;
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            if(featureVector != null){
                featureVector.setDevicesBLT(tmpBLTNumber);
            }
            tmpBLTNumber = 0;
        }
    }
}
