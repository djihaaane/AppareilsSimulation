package com.example.Porte;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.Nullable;

public class YourService extends Service {
    public YourService() {
    }
    @Override
    public void onCreate() {
        PowerManager pm = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
        assert pm != null;
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"INFO");
        wl.acquire();
        final BroadcastReceiver vReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
System.out.println("fesfqnmewp");            }
        };
        registerReceiver(vReceiver, new IntentFilter("android.media.VOLUME_CHANGED_ACTION"));

        super.onCreate();  }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // do your jobs here
        onTaskRemoved(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(),this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        startService(restartServiceIntent);

        super.onTaskRemoved(rootIntent);
    }

}
