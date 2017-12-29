package open.com.permissionsmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sultanm on 12/28/17.
 */

public class ApplicationsDatabase {
    List<AndroidApplication> applications = new ArrayList<>();
    Context context;
    Map<String, ?> allowedPermissions;
    private static ApplicationsDatabase applicationsDatabase;
    private ApplicationsDatabase(Context context){
        this.context = context;
        SharedPreferences allowedPermissionsPreferences = context.getSharedPreferences(context.getString(R.string.allowed_permissions), Context.MODE_PRIVATE);
        allowedPermissions = allowedPermissionsPreferences.getAll();
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
                AndroidApplication androidApplication = new AndroidApplication(getApplicationName(pm, applicationInfo), packageInfo.requestedPermissions);

                androidApplication.setWarnings(getWarnings(packageInfo));
                applications.add(androidApplication);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private String getApplicationName(PackageManager pm, ApplicationInfo applicationInfo) {
        try{
            return pm.getApplicationLabel(applicationInfo).toString();
        }
        catch (Exception e){
            System.out.println("in exception, return name: "+ applicationInfo.packageName);
            return applicationInfo.packageName;
        }
    }

    private int getWarnings(PackageInfo packageInfo) {
        int warnings = 0;
        String[] requestedPermissions = packageInfo.requestedPermissions;
        if(requestedPermissions == null) return 0;
        for(String permission : requestedPermissions){
            if(!allowedPermissions.containsKey(permission))
                warnings ++;
        }
        return warnings;
    }

}
