package com.example.rocketsms.background;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.SmsManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.rocketsms.R;
import com.example.rocketsms.model.SmsPayload;
import com.example.rocketsms.network.SmsApiClient;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BackgroundSmsService extends Service {

    private static final String CHANNEL_ID = "rocket_sms_background_channel";
    private static final int NOTIFICATION_ID = 101;
    private static final long API_POLL_INTERVAL_MINUTES = 3L;

    private ScheduledExecutorService scheduler;
    private SmsApiClient apiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        apiClient = new SmsApiClient();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification());
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::pollApiAndSendSms, 0, API_POLL_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    private void pollApiAndSendSms() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = null;

        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "RocketSms::ApiPollWakeLock");
            wakeLock.acquire(2 * 60 * 1000L);
        }

        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            List<SmsPayload> pendingSmsList = apiClient.fetchPendingSms();
            SmsManager smsManager = SmsManager.getDefault();

            for (SmsPayload payload : pendingSmsList) {
                smsManager.sendTextMessage(payload.getPhoneNumber(), null, payload.getMessage(), null, null);
            }
        } catch (Exception ignored) {
        } finally {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.background_notification_title))
                .setContentText(getString(R.string.background_notification_text))
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.background_channel_name),
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
