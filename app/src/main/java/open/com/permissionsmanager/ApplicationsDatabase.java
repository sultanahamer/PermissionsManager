package open.com.permissionsmanager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sultanm on 12/28/17.
 */

public class ApplicationsDatabase {
    List<AndroidApplication> applications = new ArrayList<>();
    Context context;
    private static ApplicationsDatabase applicationsDatabase;
    private ApplicationsDatabase(Context context){
        this.context = context;
        updateApplicationsDatabase();
    }
    static ApplicationsDatabase getApplicationsDatabase(Context context){
        if(applicationsDatabase == null)
            applicationsDatabase = new ApplicationsDatabase(context);
        return applicationsDatabase;
    }
    private void updateApplicationsDatabase() {
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo applicationInfo : packages) {
            Log.d("test", "App: " + applicationInfo.name + ","+ applicationInfo.className + ","+ applicationInfo.backupAgentName + ","+ " Package: " + applicationInfo.packageName);
            try {
                PackageInfo packageInfo = pm.getPackageInfo(applicationInfo.packageName, PackageManager.GET_PERMISSIONS);

                //Get Permissions
                applications.add(new AndroidApplication(pm.getApplicationLabel(applicationInfo).toString(), packageInfo.requestedPermissions));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}
