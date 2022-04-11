package open.com.permissionsmanager;

import static android.app.NotificationManager.IMPORTANCE_HIGH;

import static open.com.permissionsmanager.ValidatePermissionsBroadcastReceiver.FOUR_HOURLY_SCAN_RESULT_NOTIICATION_CODE;
import static open.com.permissionsmanager.ValidatePermissionsBroadcastReceiver.GENERIC_REQUEST_CODE;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class PermissionsManagerApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel dangerousPermissionsGrantedChannel = new NotificationChannel("" + FOUR_HOURLY_SCAN_RESULT_NOTIICATION_CODE, "Dangerous permissions granted", IMPORTANCE_HIGH);
            dangerousPermissionsGrantedChannel.setDescription("Warns about dangerous permissions granted");

            NotificationChannel notifyBlah = new NotificationChannel("" + 6684, "Generic notifications", IMPORTANCE_HIGH);
            dangerousPermissionsGrantedChannel.setDescription("Generic notifications");

            NotificationChannel ignoredAppsChannel = new NotificationChannel("" + GENERIC_REQUEST_CODE, "Ignored apps reminder", IMPORTANCE_HIGH);
            dangerousPermissionsGrantedChannel.setDescription("Reminds about ignored apps");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(dangerousPermissionsGrantedChannel);
            notificationManager.createNotificationChannel(ignoredAppsChannel);
            notificationManager.createNotificationChannel(notifyBlah);
        }

    }

}
