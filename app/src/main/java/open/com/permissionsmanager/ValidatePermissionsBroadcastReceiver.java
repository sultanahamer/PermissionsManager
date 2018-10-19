package open.com.permissionsmanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static java.util.Calendar.MONTH;
import static open.com.permissionsmanager.Utils.SCAN;

public class ValidatePermissionsBroadcastReceiver extends BroadcastReceiver{

    public static final int GENERIC_REQUEST_CODE = 123; //generic code used for notification id, pending intent id
    public static final int FOUR_HOURLY_SCAN_RESULT_NOTIICATION_CODE = 123;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!SCAN.equals(intent.getAction()))
            return;
        System.out.println("validate permissions broadcast reciever yolo " + intent);
        Utils.updateLastAlarmTime(context);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        ApplicationsDatabase applicationsDatabase = ApplicationsDatabase.getApplicationsDatabase(context);
        waitUntilScanIsComplete(applicationsDatabase);
        for(AndroidApplication application : applicationsDatabase.getACopyOfApplications()){
            if(application.isIgnoredTemporarily())
                continue;
            warnAboutPermissionsLurking(context, notificationManager);
            break;
        }

        if(!applicationsDatabase.getIgnoredAppsList().isEmpty() && isItTimeToWarnAboutIgnoredApps(context))
            warnAboutIgnoredApps(context, notificationManager);
    }

    private void waitUntilScanIsComplete(ApplicationsDatabase applicationsDatabase) {
        while(applicationsDatabase.isScanInProgress()){
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void warnAboutPermissionsLurking(Context context, NotificationManager notificationManager) {
        notificationManager.cancel(FOUR_HOURLY_SCAN_RESULT_NOTIICATION_CODE);

        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_warning_black_24dp)
                .setTicker(context.getString(R.string.apps_with_dangerous_permissions_lurking))
                .setContentText(context.getString(R.string.apps_with_dangerous_permissions_lurking))
                .setContentTitle(context.getString(R.string.attention))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(PendingIntent.getActivity(context, GENERIC_REQUEST_CODE, new Intent(context, MainActivity.class), FLAG_UPDATE_CURRENT))
                .build();
        System.out.println("notifying on usual scan results yolo");
        notificationManager.notify(FOUR_HOURLY_SCAN_RESULT_NOTIICATION_CODE, notification);
    }

    private void warnAboutIgnoredApps(Context context, NotificationManager notificationManager) {
        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_warning_black_24dp)
                .setTicker(context.getString(R.string.look_ignored_apps))
                .setContentText(context.getString(R.string.look_ignored_apps))
                .setContentTitle(context.getString(R.string.attention))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(PendingIntent.getActivity(context, GENERIC_REQUEST_CODE, new Intent(context, MainActivity.class), FLAG_UPDATE_CURRENT))
                .build();
        System.out.println("notifying on ignored apps");
        notificationManager.notify(GENERIC_REQUEST_CODE, notification);
        Utils.setLastIgnoredApplicationsWarningNotifiedInstance(context, Calendar.getInstance().getTimeInMillis());
    }

    private boolean isItTimeToWarnAboutIgnoredApps(Context context){
        Calendar oneMonthAgoTimeStamp = Utils.getCalendarInstanceRelativeFromNow(MONTH, -1);
        Calendar lastWarnedTimeStamp = Utils.getCalendarInstanceWith(Utils.getLastIgnoredApplicationsWarningNotifiedInstance(context));

        return lastWarnedTimeStamp.before(oneMonthAgoTimeStamp);
    }
}