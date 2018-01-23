package open.com.permissionsmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by sultanm on 12/28/17.
 */

public class ApplicationsDatabase {
    List<AndroidApplication> applications = new ArrayList<>();
    private Context context;
    SharedPreferences permissionsManagerSharedPreferences;
    Set<String> ignoredPermissionsForAllApps;
    private static ApplicationsDatabase applicationsDatabase;
    private ApplicationsDatabase(Context context){
        this.context = context;
        permissionsManagerSharedPreferences = context.getSharedPreferences(context.getString(R.string.permissions_manager), Context.MODE_PRIVATE);
        String ignoredPermissionsForAllAppsString = permissionsManagerSharedPreferences.getString(context.getString(R.string.allowed_permissions), new String());
        ignoredPermissionsForAllApps = Utils.makeHashSet(ignoredPermissionsForAllAppsString, ";");
        updateApplicationsDatabase();
    }
    static ApplicationsDatabase getApplicationsDatabase(Context context){
        if(applicationsDatabase == null)
            applicationsDatabase = new ApplicationsDatabase(context);
        return applicationsDatabase;
    }

    public void updateApplicationsDatabase() {
        applications.clear();
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo applicationInfo : packages) {
            AndroidApplication androidApplication = null;
            try {
                androidApplication = createAndroidApplication(pm, applicationInfo);
                applications.add(androidApplication);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        sort();
    }

    private void sort() {
        Collections.sort(applications, new Comparator<AndroidApplication>() {
            @Override
            public int compare(AndroidApplication app1, AndroidApplication app2) {
                int usualCompareResult = app2.getWarnablePermissions().size() - app1.getWarnablePermissions().size();
                boolean app1Enabled = app1.isEnabled();
                boolean app2Enabled = app2.isEnabled();
                if(app1Enabled == app2Enabled)
                    return usualCompareResult;
                return app1Enabled ? -1 : 1;
            }
        });
    }

    @NonNull
    private AndroidApplication createAndroidApplication(PackageManager pm, ApplicationInfo applicationInfo) throws PackageManager.NameNotFoundException {
        PackageInfo packageInfo = null;
        packageInfo = pm.getPackageInfo(applicationInfo.packageName, PackageManager.GET_PERMISSIONS);
        List<String> nonwarnablePermission = new ArrayList<>();
        List<String> warnablePermissions = new ArrayList<>(3);
        HashSet<String> appSpecificIgnoreList;
        if(packageInfo.requestedPermissions != null) {
            appSpecificIgnoreList = Utils.makeHashSet(permissionsManagerSharedPreferences.getString(applicationInfo.packageName, ""), ";");
            for(int i=0; i<packageInfo.requestedPermissions.length; i++){
                String permission = packageInfo.requestedPermissions[i];
                if(pm.checkPermission(permission, packageInfo.packageName) == PackageManager.PERMISSION_GRANTED){
                    if(isDangerous(permission, pm) && !ignoredPermissionsForAllApps.contains(permission) && !appSpecificIgnoreList.contains(permission))
                        warnablePermissions.add(permission);
                    else
                        nonwarnablePermission.add(permission);
                }
            }
        }
        AndroidApplication androidApplication = new AndroidApplication(getApplicationName(pm, applicationInfo), packageInfo.packageName, nonwarnablePermission, warnablePermissions, applicationInfo.enabled);
        return androidApplication;
    }

    private boolean isDangerous(String permission, PackageManager pm) throws PackageManager.NameNotFoundException {
        return pm.getPermissionInfo(permission, PackageManager.GET_META_DATA).protectionLevel == PermissionInfo.PROTECTION_DANGEROUS;
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

    public void recomputePermissions(){
        PackageManager pm = context.getPackageManager();
        for(int i = 0, numberOfApplications = applications.size(); i < numberOfApplications; i++){
            AndroidApplication application = applications.get(i);
            if(application.getWarnablePermissions().size() > 0)
                try {
                    applications.remove(i);
                    applications.add(i, createAndroidApplication(pm, pm.getApplicationInfo(application.getPackageName(), PackageManager.GET_META_DATA)));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
        }
    }

    public HashSet<String> getIgnoredPermissionsForAllApps(){
        String ignoredPermissionsForAllAppsString  = permissionsManagerSharedPreferences.getString(context.getString(R.string.allowed_permissions), "");
        return Utils.makeHashSet(ignoredPermissionsForAllAppsString, ";");
    }

    public void ignorePermissionForAllApps(String permission){
        SharedPreferences.Editor editor = this.permissionsManagerSharedPreferences.edit();
        ignoredPermissionsForAllApps.add(permission);
        editor.putString(context.getString(R.string.allowed_permissions), Utils.makeString(ignoredPermissionsForAllApps, ";"));
        editor.commit();
        updateAllowedPermissions();
    }

    public void ignorePermissionForSpecificApp(String packageName, String permission){
        SharedPreferences.Editor editor = permissionsManagerSharedPreferences.edit();
        String ignoredPermissionsForGivenAppAsString = permissionsManagerSharedPreferences.getString(packageName, "");
        HashSet<String> ignoredPermissionsForSpecificApp = Utils.makeHashSet(ignoredPermissionsForGivenAppAsString, ";");
        ignoredPermissionsForSpecificApp.add(permission);
        editor.putString(packageName, Utils.makeString(ignoredPermissionsForSpecificApp, ";"));
        editor.commit();
    }

    private void updateAllowedPermissions() {
        ignoredPermissionsForAllApps = getIgnoredPermissionsForAllApps();
    }
}
