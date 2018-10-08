package open.com.permissionsmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by sultanm on 12/28/17.
 */

public class ApplicationsDatabase {
    private final static int TASK_RETURN_A_COPY = 1;
    private static final int TASK_REPLACE = 2;
    public static final String SHARED_PREF_KEY_TEMPORARILY_IGNORED_APPS = "IGNORED_APPS";
    public static final String SHARED_PREF_KEY_DUMMY = "DUMMY";
    public static final String AOSP_APPS_PREFIX = "com.android.";
    private List<AndroidApplication> applications = new ArrayList<>();
    private Context context;
    private SharedPreferences permissionsManagerSharedPreferences;
    private static ApplicationsDatabase applicationsDatabase;
    private List<ApplicationDatabaseChangeListener> applicationDatabaseChangeListeners;
    private boolean scanInProgress = false;

    private ApplicationsDatabase(Context context) {
        this.context = context;
        permissionsManagerSharedPreferences = Utils.getSharedPreferences(context);
        applicationDatabaseChangeListeners = new ArrayList<>(3);
        new Thread() {
            @Override
            public void run() {
                updateApplicationsDatabase();
            }
        }.start();
    }

    public boolean isScanInProgress() {
        return scanInProgress;
    }

    public synchronized static ApplicationsDatabase getApplicationsDatabase(Context context) {
        if (applicationsDatabase == null)
            applicationsDatabase = new ApplicationsDatabase(context);
        return applicationsDatabase;
    }

    private synchronized List<AndroidApplication> performSynchronizedTask(int task, List<AndroidApplication> newApplicationsList) {
        switch (task) {
            case TASK_RETURN_A_COPY:
                return new ArrayList<>(applications);
            case TASK_REPLACE:
                return applications = newApplicationsList;
        }
        throw new RuntimeException("No task with id " + task + " found");
    }

    public List<AndroidApplication> getACopyOfApplications() {
        return performSynchronizedTask(TASK_RETURN_A_COPY, null);
    }

    public void updateApplicationsDatabase() {
        scanInProgress = true;
        ArrayList<AndroidApplication> newApplicationsList = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        AndroidApplication androidApplication;
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        Set<String> ignoredPermissionsForAllApps = getIgnoredPermissionsForAllApps();
        Set<String> temporarilyIgnoredApps = permissionsManagerSharedPreferences.getStringSet(SHARED_PREF_KEY_TEMPORARILY_IGNORED_APPS, new HashSet<String>(0));
        for (ApplicationInfo applicationInfo : packages) {
            if (!applicationInfo.enabled || applicationInfo.packageName.startsWith(AOSP_APPS_PREFIX))
                continue;
            try {
                androidApplication = createAndroidApplication(pm, applicationInfo, temporarilyIgnoredApps, ignoredPermissionsForAllApps);
                if (androidApplication.getWarnablePermissions().size() == 0)
                    continue;
                newApplicationsList.add(androidApplication);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        performSynchronizedTask(TASK_REPLACE, newApplicationsList);
        scanInProgress = false;
        for (ApplicationDatabaseChangeListener applicationDatabaseChangeListener : applicationDatabaseChangeListeners)
            applicationDatabaseChangeListener.applicationsDatabaseUpdated(performSynchronizedTask(TASK_RETURN_A_COPY, applications));
    }

    @NonNull
    private AndroidApplication createAndroidApplication(PackageManager pm, ApplicationInfo applicationInfo, Set<String> temporarilyIgnoredApps, Set<String> ignoredPermissionsForAllApps) throws PackageManager.NameNotFoundException {
        PackageInfo packageInfo;
        packageInfo = pm.getPackageInfo(applicationInfo.packageName, PackageManager.GET_PERMISSIONS);
        List<String> nonwarnablePermission = new ArrayList<>();
        List<String> warnablePermissions = new ArrayList<>(3);
        Set<String> appSpecificIgnoreList;
        if (packageInfo.requestedPermissions != null) {
            appSpecificIgnoreList = getAppSpecificIgnoreList(applicationInfo.packageName);
            for (String permission : packageInfo.requestedPermissions) {
                if (pm.checkPermission(permission, packageInfo.packageName) == PackageManager.PERMISSION_GRANTED) {
                    if (isDangerous(permission, pm) && !ignoredPermissionsForAllApps.contains(permission) && !appSpecificIgnoreList.contains(permission))
                        warnablePermissions.add(permission);
                    else
                        nonwarnablePermission.add(permission);
                }
            }
        }
        return new AndroidApplication.Builder(packageInfo.packageName)
                .withName(getApplicationName(pm, applicationInfo))
                .withNonWarnablePermissions(nonwarnablePermission)
                .withWarnablePermissions(warnablePermissions)
                .withIcon(pm.getApplicationIcon(packageInfo.packageName))
                .withIgnoredTemporarily(temporarilyIgnoredApps.contains(packageInfo.packageName))
                .build();
    }

    @NonNull
    private Set<String> getAppSpecificIgnoreList(String packageName) {
        return permissionsManagerSharedPreferences.getStringSet(packageName, new HashSet<String>(0));
    }

    private boolean isDangerous(String permission, PackageManager pm) throws PackageManager.NameNotFoundException {
        return pm.getPermissionInfo(permission, PackageManager.GET_META_DATA).protectionLevel == PermissionInfo.PROTECTION_DANGEROUS;
    }

    private String getApplicationName(PackageManager pm, ApplicationInfo applicationInfo) {
        try {
            return pm.getApplicationLabel(applicationInfo).toString();
        } catch (Exception e) {
            System.out.println("This application has no name hence using its package name" + applicationInfo.packageName);
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

    public Set<String> getIgnoredPermissionsForAllApps() {
        return permissionsManagerSharedPreferences.getStringSet(context.getString(R.string.allowed_permissions), new HashSet<String>(0));
    }

    public void ignorePermissionForAllApps(String permission) {
        Set<String> ignoredPermissionsForAllApps = getIgnoredPermissionsForAllApps();
        ignoredPermissionsForAllApps.add(permission);
        permissionsManagerSharedPreferences
                .edit()
                .putInt(SHARED_PREF_KEY_DUMMY, new Random().nextInt())
                .putStringSet(context.getString(R.string.allowed_permissions), ignoredPermissionsForAllApps)
                .apply();
        new Thread() {
            @Override
            public void run() {
                updateApplicationsDatabase();
            }
        }.start();
    }

    public void ignorePermissionForSpecificApp(String packageName, String permission) {
        int indexOfApp = applications.indexOf(new AndroidApplication(packageName));
        if (indexOfApp == -1)
            return;
        AndroidApplication application = applications.get(indexOfApp);
        List<String> warnablePermissions = application.getWarnablePermissions();
        if (!warnablePermissions.contains(permission))
            return;
        warnablePermissions.remove(permission);
        application.getNonwarnablePermissions().add(permission);


        Set<String> ignoredPermissionsForGivenApp = getAppSpecificIgnoreList(packageName);
        ignoredPermissionsForGivenApp.add(permission);

        permissionsManagerSharedPreferences
                .edit()
                .putInt(SHARED_PREF_KEY_DUMMY, new Random().nextInt())
                .putStringSet(packageName, ignoredPermissionsForGivenApp)
                .apply();
        permissionsManagerSharedPreferences = Utils.getSharedPreferences(context);

        for (ApplicationDatabaseChangeListener applicationDatabaseChangeListener : applicationDatabaseChangeListeners)
            applicationDatabaseChangeListener.applicationPermissionsUpdated(application);
    }


    public void addApplicationDatabaseChangeListener(ApplicationDatabaseChangeListener applicationDatabaseChangeListener) {
        applicationDatabaseChangeListeners.add(applicationDatabaseChangeListener);
    }

    public void removeApplicationDatabaseChangeListener(ApplicationDatabaseChangeListener applicationDatabaseChangeListener) {
        applicationDatabaseChangeListeners.remove(applicationDatabaseChangeListener);
    }

    public AndroidApplication getApplication(String packageName) {
        if (packageName == null)
            return null;
        int indexOfApplication = applications.indexOf(new AndroidApplication(packageName));
        if (indexOfApplication == -1)
            return null;
        return applications.get(indexOfApplication);
    }

    public void addAppToIgnoreList(AndroidApplication androidApplication) {
        if (!applications.contains(androidApplication))
            return;
        Set<String> ignoredApps = permissionsManagerSharedPreferences.getStringSet(SHARED_PREF_KEY_TEMPORARILY_IGNORED_APPS, new HashSet<String>(1));
        ignoredApps.add(androidApplication.getPackageName());
        permissionsManagerSharedPreferences.edit()
                .putInt(SHARED_PREF_KEY_DUMMY, new Random().nextInt())
                .putStringSet(SHARED_PREF_KEY_TEMPORARILY_IGNORED_APPS, ignoredApps)
                .apply();
        //android 19 has issue saving hashset, so we have to save something random with it
        AndroidApplication ignoredApplication = createACopyOfAndroidApplicationButIgnoredFlag(true, androidApplication);
        applications.remove(ignoredApplication);
        applications.add(ignoredApplication);
        for(ApplicationDatabaseChangeListener applicationDatabaseChangeListener : applicationDatabaseChangeListeners)
            applicationDatabaseChangeListener.applicationAddedToIgnoreList(ignoredApplication);
    }

    private AndroidApplication createACopyOfAndroidApplicationButIgnoredFlag(boolean ignored, AndroidApplication androidApplication) {
        return new AndroidApplication.Builder(androidApplication.getPackageName())
                .withIgnoredTemporarily(ignored)
                .withIcon(androidApplication.getIcon())
                .withName(androidApplication.getName())
                .withNonWarnablePermissions(androidApplication.getNonwarnablePermissions())
                .withWarnablePermissions(androidApplication.getWarnablePermissions())
                .build();
    }

    public void removeAppFromIgnoreList(AndroidApplication androidApplication) {
        if (!applications.contains(androidApplication))
            return;
        Set<String> ignored_apps = permissionsManagerSharedPreferences.getStringSet(SHARED_PREF_KEY_TEMPORARILY_IGNORED_APPS, new HashSet<String>(1));
        ignored_apps.remove(androidApplication.getPackageName());
        permissionsManagerSharedPreferences.edit()
                .putInt(SHARED_PREF_KEY_DUMMY, new Random().nextInt())
                .putStringSet(SHARED_PREF_KEY_TEMPORARILY_IGNORED_APPS, ignored_apps)
                .apply();
        //android 19 has issue saving hashset, so we have to save something random with it
        AndroidApplication unignoredApplication = createACopyOfAndroidApplicationButIgnoredFlag(false, androidApplication);
        applications.remove(unignoredApplication);
        applications.add(unignoredApplication);
        for(ApplicationDatabaseChangeListener applicationDatabaseChangeListener : applicationDatabaseChangeListeners)
            applicationDatabaseChangeListener.applicationRemovedFromIgnoredList(unignoredApplication);
    }

    public Set<String> getIgnoredAppsList(){
        return permissionsManagerSharedPreferences.getStringSet(SHARED_PREF_KEY_TEMPORARILY_IGNORED_APPS, new HashSet<String>(0));
    }
}