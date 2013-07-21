package com.greenzeta.savesyncr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by matt on 7/21/13.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(SyncService.class.getName());
        context.startService(serviceIntent);
    }
}
