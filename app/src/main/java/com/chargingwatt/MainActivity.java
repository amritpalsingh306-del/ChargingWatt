package com.chargingwatt;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startMonitor();

        Button stop = (Button) findViewById(R.id.btn_stop);
        stop.setOnClickListener(v -> {
            stopService(new Intent(this, ChargingService.class));
            Toast.makeText(this, "Monitor stopped", Toast.LENGTH_SHORT).show();
        });

        Button start = (Button) findViewById(R.id.btn_start);
        start.setOnClickListener(v -> {
            startMonitor();
            Toast.makeText(this, "Monitor started", Toast.LENGTH_SHORT).show();
        });
    }

    private void startMonitor() {
        Intent i = new Intent(this, ChargingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(i);
        } else {
            startService(i);
        }
    }
}
