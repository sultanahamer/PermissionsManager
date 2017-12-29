package open.com.permissionsmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

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
        updateApplicationsDatabase();
    }
    static ApplicationsDatabase getApplicationsDatabase(Context context){
        if(applicationsDatabase == null)
            applicationsDatabase = new ApplicationsDatabase(context);
        return applicationsDatabase;
    }

    public void updateApplicationsDatabase() {
        PackageManager pm = context.getPackageManager();
        SharedPreferences allowedPermissionsPreferences = context.getSharedPreferences(context.getString(R.string.allowed_permissions), Context.MODE_PRIVATE);
        allowedPermissions = allowedPermissionsPreferences.getAll();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo applicationInfo : packages) {
            AndroidApplication androidApplication = null;
            try {
                androidApplication = getAndroidApplication(pm, applicationInfo);
                applications.add(androidApplication);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @NonNull
    private AndroidApplication getAndroidApplication(PackageManager pm, ApplicationInfo applicationInfo) throws PackageManager.NameNotFoundException {
        PackageInfo packageInfo = null;
        packageInfo = pm.getPackageInfo(applicationInfo.packageName, PackageManager.GET_PERMISSIONS);
        AndroidApplication androidApplication = new AndroidApplication(getApplicationName(pm, applicationInfo), packageInfo.packageName, packageInfo.requestedPermissions);

        androidApplication.setWarnings(getWarnings(packageInfo, pm));
        return androidApplication;
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

    private int getWarnings(PackageInfo packageInfo, PackageManager pm) {
        int warnings = 0;
        String[] requestedPermissions = packageInfo.requestedPermissions;
        if(requestedPermissions == null) return 0;
        for(String permission : requestedPermissions){
            if(!allowedPermissions.containsKey(permission) && pm.checkPermission(permission, packageInfo.packageName) == PackageManager.PERMISSION_GRANTED)
                warnings ++;
        }
        return warnings;
    }

    public void recomputePermissions(){
        PackageManager pm = context.getPackageManager();
        for(int i = 0, numberOfApplications = applications.size(); i < numberOfApplications; i++){
            AndroidApplication application = applications.get(i);
            if(application.getWarnings() > 0)
                try {
                    applications.remove(i);
                    applications.add(i, getAndroidApplication(pm, pm.getApplicationInfo(application.getPackageName(), PackageManager.GET_META_DATA)));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
        }
    }
}
