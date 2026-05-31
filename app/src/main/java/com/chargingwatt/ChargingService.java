package com.chargingwatt;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

public class ChargingService extends Service {
    private static final String CHANNEL_ID = "watt_channel";
    private static final int NOTIF_ID = 1;
    private Handler handler;
    private Runnable ticker;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIF_ID, buildNotif("Starting..."));
        ticker = new Runnable() {
            public void run() {
                updateNotif();
                handler.postDelayed(this, 2000);
            }
        };
        handler.post(ticker);
        return START_STICKY;
    }

    private void updateNotif() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(NOTIF_ID, buildNotif(getWattage()));
    }

    private String getWattage() {
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        Intent bat = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (bm == null || bat == null) return "Unavailable";

        int status = bat.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean charging = status == BatteryManager.BATTERY_STATUS_CHARGING
                        || status == BatteryManager.BATTERY_STATUS_FULL;

        long uA = bm.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
        int mV = bat.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
        int pct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        if (uA == Long.MIN_VALUE || mV == 0) {
            return charging ? "Charging • " + pct + "%" : "Discharging • " + pct + "%";
        }

        double W = (Math.abs(uA) / 1_000_000.0) * (mV / 1000.0);
        if (charging) {
            return String.format("⚡ %.1f W  •  %d%%", W, pct);
        } else {
            return String.format("🔋 %.1f W  •  %d%%", W, pct);
        }
    }

    private Notification buildNotif(String text) {
        Notification.Builder b;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            b = new Notification.Builder(this, CHANNEL_ID);
        } else {
            b = new Notification.Builder(this);
        }
        return b.setSmallIcon(android.R.drawable.ic_menu_manage)
                .setContentTitle("Charging Watt")
                .setContentText(text)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Charging Watt", NotificationManager.IMPORTANCE_LOW);
            ch.setSound(null, null);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch);
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        if (handler != null) handler.removeCallbacks(ticker);
    }
}
