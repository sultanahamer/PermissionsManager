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
                return app2.getWarnablePermissionIndexes().size() - app1.getWarnablePermissionIndexes().size();
            }
        });
    }

    @NonNull
    private AndroidApplication createAndroidApplication(PackageManager pm, ApplicationInfo applicationInfo) throws PackageManager.NameNotFoundException {
        PackageInfo packageInfo = null;
        packageInfo = pm.getPackageInfo(applicationInfo.packageName, PackageManager.GET_PERMISSIONS);
        List<String> grantedPermissions = new ArrayList<>();
        Set<Integer> warnablePermissionIndexes = new HashSet<>();
        HashSet<String> appSpecificIgnoreList;
        if(packageInfo.requestedPermissions != null) {
            appSpecificIgnoreList = Utils.makeHashSet(permissionsManagerSharedPreferences.getString(applicationInfo.packageName, ""), ";");
            for(int i=0; i<packageInfo.requestedPermissions.length; i++){
                String permission = packageInfo.requestedPermissions[i];
                if(pm.checkPermission(permission, packageInfo.packageName) == PackageManager.PERMISSION_GRANTED){
                    grantedPermissions.add(permission);
                    if(isDangerous(permission, pm) && !ignoredPermissionsForAllApps.contains(permission) && !appSpecificIgnoreList.contains(permission))
                        warnablePermissionIndexes.add(i);
                }
            }
        }
        AndroidApplication androidApplication = new AndroidApplication(getApplicationName(pm, applicationInfo), packageInfo.packageName, grantedPermissions, warnablePermissionIndexes);
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
            if(application.getWarnablePermissionIndexes().size() > 0)
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
        String ignoredPermissionsForGivenApp = permissionsManagerSharedPreferences.getString(packageName, "");
        editor.putString(packageName, ignoredPermissionsForGivenApp + ";" + permission);
        editor.commit();
    }

    private void updateAllowedPermissions() {
        ignoredPermissionsForAllApps = getIgnoredPermissionsForAllApps();
    }
}
