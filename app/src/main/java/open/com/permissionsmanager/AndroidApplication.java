package open.com.permissionsmanager;

import java.util.Arrays;

/**
 * Created by sultanm on 12/28/17.
 */

public class AndroidApplication {

    private String packageName;
    private String name;
    private String[] permissions;
    private int warnings;

    public AndroidApplication(String applicationName, String packageName, String[] requestedPermissions) {
        this.name  = applicationName;
        this.permissions = requestedPermissions;
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getWarnings() {
        return warnings;
    }

    public void setWarnings(int warnings) {
        this.warnings = warnings;
    }

    public String[] getPermissions() {
        return permissions;
    }

    public void setPermissions(String[] permissions) {
        this.permissions = permissions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "name='" + name + '\'' +
                ", permissions=" + Arrays.toString(permissions) +
                '}';
    }
}
