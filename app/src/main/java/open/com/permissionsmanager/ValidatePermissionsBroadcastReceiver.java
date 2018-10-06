package open.com.permissionsmanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static open.com.permissionsmanager.Utils.SCAN;

public class ValidatePermissionsBroadcastReceiver extends BroadcastReceiver{

    public static final int REQUEST_CODE = 123;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!SCAN.equals(intent.getAction()))
            return;
        System.out.println("validate permissions broadcast reciever yolo " + intent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(REQUEST_CODE);

        ApplicationsDatabase applicationsDatabase = ApplicationsDatabase.getApplicationsDatabase(context);
        for(AndroidApplication application : applicationsDatabase.getACopyOfApplications()){
            if(application.getWarnablePermissions().size() == 0)
                continue;

            Notification notification = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_warning_black_24dp)
                    .setTicker("Apps with dangerous permissions lurking!!!")
                    .setContentText("Apps with dangerous permissions lurking!!!")
                    .setContentTitle("Attention")
                    .setContentIntent(PendingIntent.getActivity(context, REQUEST_CODE, new Intent(context, MainActivity.class), FLAG_UPDATE_CURRENT))
                    .build();
            System.out.println("notifying yolo");
            notificationManager.notify(REQUEST_CODE, notification);
            return;
        }
    }
}
