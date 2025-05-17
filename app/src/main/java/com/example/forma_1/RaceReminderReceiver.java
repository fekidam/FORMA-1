package com.example.forma_1;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;

public class RaceReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "login_channel";
    private static final String CHANNEL_NAME = "Race Reminders";
    private static final int NOTIFICATION_ID = 2;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Create the notification channel (required for API 26+)
        createNotificationChannel(context);

        // Check for POST_NOTIFICATIONS permission (required for API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, cannot post notification
                Toast.makeText(context, "Értesítési jogosultság hiányzik!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Emlékeztető")
                .setContentText("Ma van a következő Forma-1 futam! Készülj fel!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Post the notification with SecurityException handling
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
            Toast.makeText(context, "Forma-1 futam emlékeztető!", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(context, "Értesítés megjelenítése nem sikerült: jogosultság hiányzik!", Toast.LENGTH_LONG).show();
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Emlékeztetők a Forma-1 futamokról");
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}