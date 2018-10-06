package open.com.permissionsmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReciever extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        System.out.println("bootbroadcast setting alarm yolo " + intent);
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Utils.setAlarm(context);
        }
    }
}