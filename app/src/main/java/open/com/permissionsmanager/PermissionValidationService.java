package open.com.permissionsmanager;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionValidationService extends Service {
    public PermissionValidationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void validatePermissions() {
        ApplicationsDatabase applicationsDatabase = ApplicationsDatabase.getApplicationsDatabase(this);
        List<AndroidApplication> applications = applicationsDatabase.applications;
        for(int i = 0, size = applications.size(); i < size; i++){
        }
    }
}
