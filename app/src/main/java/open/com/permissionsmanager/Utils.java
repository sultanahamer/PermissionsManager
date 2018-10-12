package open.com.permissionsmanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.app.AlarmManager.INTERVAL_HOUR;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static open.com.permissionsmanager.ValidatePermissionsBroadcastReceiver.GENERIC_REQUEST_CODE;

/**
 * Created by sultanm on 1/23/18.
 */

public class Utils {

    public static final String SCAN = "SCAN";
    public static final String SHARED_PREFERENCES_KEY_IGNORED_APPLICATIONS_WARN_TIMESTAMP = "SHARED_PREFERENCES_KEY_IGNORED_APPLICATIONS_WARN_TIMESTAMP";

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(context.getString(R.string.permissions_manager), context.MODE_PRIVATE);
    }

    private static Intent getIntentToBroadcastValidatePermissions(Context context){
        return new Intent(context.getApplicationContext(), ValidatePermissionsBroadcastReceiver.class)
                    .setAction(SCAN);
    }
    public static boolean isAlarmSet(Context context){
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, GENERIC_REQUEST_CODE, getIntentToBroadcastValidatePermissions(context), PendingIntent.FLAG_NO_CREATE);
        return pendingIntent == null;
    }

    public static void setAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, GENERIC_REQUEST_CODE, getIntentToBroadcastValidatePermissions(context), FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, INTERVAL_HOUR * 4, pendingIntent);
    }

    public static void sort(List<AndroidApplication> applications) {
        Collections.sort(applications, new Comparator<AndroidApplication>() {
            @Override
            public int compare(AndroidApplication app1, AndroidApplication app2) {
                return app2.getWarnablePermissions().size() - app1.getWarnablePermissions().size();
            }
        });
    }

    public static long getLastIgnoredApplicationsWarningNotifiedInstance(Context context){
        return getSharedPreferences(context)
                .getLong(SHARED_PREFERENCES_KEY_IGNORED_APPLICATIONS_WARN_TIMESTAMP, 0);

    }

    public static void setLastIgnoredApplicationsWarningNotifiedInstance(Context context, long timestamp){
        getSharedPreferences(context)
                .edit()
                .putLong(SHARED_PREFERENCES_KEY_IGNORED_APPLICATIONS_WARN_TIMESTAMP, timestamp)
                .apply();
    }

    public static Calendar getCalendarInstanceRelativeFromNow(int field, int amount){
        Calendar instance = Calendar.getInstance();
        instance.add(field, amount);
        return instance;
    }

    public static Calendar getCalendarInstanceWith(long timestamp){
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(timestamp);
        return instance;
    }
}
