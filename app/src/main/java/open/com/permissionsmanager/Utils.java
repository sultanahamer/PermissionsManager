package open.com.permissionsmanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static android.app.AlarmManager.INTERVAL_HOUR;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static open.com.permissionsmanager.ValidatePermissionsBroadcastReceiver.REQUEST_CODE;

/**
 * Created by sultanm on 1/23/18.
 */

public class Utils {

    public static final String SCAN = "SCAN";

    public static HashSet<String> makeHashSet(String content, String delimiter){
        return new HashSet<>(Arrays.asList(content.split(delimiter)));
    }

    public static String makeString(Set<String> set, String delimiter) {
        StringBuffer stringBuffer = new StringBuffer();
        for(String key : set){
            stringBuffer.append(key).append(delimiter);
        }
        return stringBuffer.toString();
    }
    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(context.getString(R.string.permissions_manager), context.MODE_PRIVATE);
    }

    public static void setAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent broadcastIntent = new Intent(context.getApplicationContext(), ValidatePermissionsBroadcastReceiver.class);
        broadcastIntent.setAction(SCAN);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, broadcastIntent, FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, INTERVAL_HOUR * 4, pendingIntent);
    }

}
