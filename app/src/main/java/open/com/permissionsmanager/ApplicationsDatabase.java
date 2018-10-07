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
    private static final int TASK_REPLACE = 2;
    private List<AndroidApplication> applications = new ArrayList<>();
    private Context context;
    private SharedPreferences permissionsManagerSharedPreferences;
    private Set<String> ignoredPermissionsForAllApps;
    private static ApplicationsDatabase applicationsDatabase;
    private final static int TASK_RETURN_A_COPY = 1;
    private List<ApplicationDatabaseChangeListener> applicationDatabaseChangeListeners;

    private ApplicationsDatabase(Context context){
        this.context = context;
        permissionsManagerSharedPreferences = Utils.getSharedPreferences(context);
        String ignoredPermissionsForAllAppsString = permissionsManagerSharedPreferences.getString(context.getString(R.string.allowed_permissions), new String());
        ignoredPermissionsForAllApps = Utils.makeHashSet(ignoredPermissionsForAllAppsString, ";");
        applicationDatabaseChangeListeners = new ArrayList<>(3);
        updateApplicationsDatabase();
    }
    public synchronized static ApplicationsDatabase getApplicationsDatabase(Context context){
        if(applicationsDatabase == null)
            applicationsDatabase = new ApplicationsDatabase(context);
        return applicationsDatabase;
    }

    private synchronized List<AndroidApplication> performSynchronizedTask(int task, List<AndroidApplication> newApplicationsList){
        switch (task){
            case TASK_RETURN_A_COPY:
                return new ArrayList<>(applications);
            case TASK_REPLACE:
                return applications = newApplicationsList;
        }
        throw new RuntimeException("No task with id " + task + " found");
    }

    public List<AndroidApplication> getACopyOfApplications(){
        return performSynchronizedTask(TASK_RETURN_A_COPY, null);
    }

    public void updateApplicationsDatabase() {
        ArrayList<AndroidApplication> newApplicationsList = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        AndroidApplication androidApplication;
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo applicationInfo : packages) {
            if(!applicationInfo.enabled)
                continue;
            try {
                androidApplication = createAndroidApplication(pm, applicationInfo);
                if(androidApplication.getWarnablePermissions().size() == 0)
                    continue;
                newApplicationsList.add(androidApplication);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        performSynchronizedTask(TASK_REPLACE, newApplicationsList);

        for(ApplicationDatabaseChangeListener applicationDatabaseChangeListener : applicationDatabaseChangeListeners)
            applicationDatabaseChangeListener.applicationsDatabaseUpdated(performSynchronizedTask(TASK_RETURN_A_COPY, applications));
    }

    @NonNull
    private AndroidApplication createAndroidApplication(PackageManager pm, ApplicationInfo applicationInfo) throws PackageManager.NameNotFoundException {
        PackageInfo packageInfo;
        packageInfo = pm.getPackageInfo(applicationInfo.packageName, PackageManager.GET_PERMISSIONS);
        List<String> nonwarnablePermission = new ArrayList<>();
        List<String> warnablePermissions = new ArrayList<>(3);
        HashSet<String> appSpecificIgnoreList;
        if(packageInfo.requestedPermissions != null) {
            appSpecificIgnoreList = getAppSpecificIgnoreList(applicationInfo);
            for(String permission : packageInfo.requestedPermissions){
                if(pm.checkPermission(permission, packageInfo.packageName) == PackageManager.PERMISSION_GRANTED){
                    if(isDangerous(permission, pm) && !ignoredPermissionsForAllApps.contains(permission) && !appSpecificIgnoreList.contains(permission))
                        warnablePermissions.add(permission);
                    else
                        nonwarnablePermission.add(permission);
                }
            }
        }
        return new AndroidApplication(getApplicationName(pm, applicationInfo), packageInfo.packageName, nonwarnablePermission, warnablePermissions);
    }

    @NonNull
    private HashSet<String> getAppSpecificIgnoreList(ApplicationInfo applicationInfo) {
        return Utils.makeHashSet(permissionsManagerSharedPreferences.getString(applicationInfo.packageName, ""), ";");
    }

    private boolean isDangerous(String permission, PackageManager pm) throws PackageManager.NameNotFoundException {
        return pm.getPermissionInfo(permission, PackageManager.GET_META_DATA).protectionLevel == PermissionInfo.PROTECTION_DANGEROUS;
    }

    private String getApplicationName(PackageManager pm, ApplicationInfo applicationInfo) {
        try{
            return pm.getApplicationLabel(applicationInfo).toString();
        }
        catch (Exception e){
            System.out.println("This application has no name hence using its package name"+ applicationInfo.packageName);
            return applicationInfo.packageName;
        }
    }

//    public void recomputePermissions(){TODO: this would get used later
//        PackageManager pm = context.getPackageManager();
//        for(int i = 0, numberOfApplications = applications.size(); i < numberOfApplications; i++){
//            AndroidApplication application = applications.get(i);
//            if(application.getWarnablePermissions().size() > 0)
//                try {
//                    applications.remove(i);
//                    applications.add(i, createAndroidApplication(pm, pm.getApplicationInfo(application.getPackageName(), PackageManager.GET_META_DATA)));
//                } catch (PackageManager.NameNotFoundException e) {
//                    e.printStackTrace();
//                }
//        }
//    }

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
        new Thread(){
            @Override
            public void run() {
                updateApplicationsDatabase();
            }
        }.start();
    }

    public void ignorePermissionForSpecificApp(String packageName, String permission){
        int indexOfApp = applications.indexOf(new AndroidApplication(packageName));
        if(indexOfApp == -1)
            return;
        AndroidApplication application = applications.get(indexOfApp);
        List<String> warnablePermissions = application.getWarnablePermissions();
        if(!warnablePermissions.contains(permission))
            return;
        warnablePermissions.remove(permission);
        application.getNonwarnablePermissions().add(permission);

        SharedPreferences.Editor editor = permissionsManagerSharedPreferences.edit();
        String ignoredPermissionsForGivenAppAsString = permissionsManagerSharedPreferences.getString(packageName, "");
        HashSet<String> ignoredPermissionsForSpecificApp = Utils.makeHashSet(ignoredPermissionsForGivenAppAsString, ";");
        ignoredPermissionsForSpecificApp.add(permission);
        editor.putString(packageName, Utils.makeString(ignoredPermissionsForSpecificApp, ";"));
        editor.commit();

        for(ApplicationDatabaseChangeListener applicationDatabaseChangeListener : applicationDatabaseChangeListeners)
            applicationDatabaseChangeListener.applicationPermissionsUpdated(application);
    }

    private void updateAllowedPermissions() {
        ignoredPermissionsForAllApps = getIgnoredPermissionsForAllApps();
    }

    public void addApplicationDatabaseChangeListener(ApplicationDatabaseChangeListener applicationDatabaseChangeListener){
        applicationDatabaseChangeListeners.add(applicationDatabaseChangeListener);
    }
    public void removeApplicationDatabaseChangeListener(ApplicationDatabaseChangeListener applicationDatabaseChangeListener){
        applicationDatabaseChangeListeners.remove(applicationDatabaseChangeListener);
    }

    public AndroidApplication getApplication(String packageName) {
        if(packageName == null)
            return null;
        int indexOfApplication = applications.indexOf(new AndroidApplication(packageName));
        if(indexOfApplication == -1)
            return null;
        return applications.get(indexOfApplication);
    }
}
