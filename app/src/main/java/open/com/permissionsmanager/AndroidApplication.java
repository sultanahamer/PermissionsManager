package open.com.permissionsmanager;

import java.util.List;
import java.util.Set;

/**
 * Created by sultanm on 12/28/17.
 */

public class AndroidApplication {

    private String packageName;
    private String name;
    private List<String> permissions;
    private int warnings;
    private Set<String> exclusiveIgnoredPermissionsList;

    public AndroidApplication(String applicationName, String packageName, List requestedPermissions, Set<String> exclusiveIgnoredPermissionsList) {
        this.name  = applicationName;
        this.permissions = requestedPermissions;
        this.packageName = packageName;
        this.exclusiveIgnoredPermissionsList = exclusiveIgnoredPermissionsList;
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

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List permissions) {
        this.permissions = permissions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getExclusiveIgnoredPermissionsList() {
        return exclusiveIgnoredPermissionsList;
    }

}
