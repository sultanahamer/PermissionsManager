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
    private Set<Integer> warnablePermissionIndexes;
    private boolean enabled;

    public AndroidApplication(String name, String packageName, List<String> permissions, Set<Integer> warnablePermissionIndexes, boolean enabled) {
        this.packageName = packageName;
        this.name = name;
        this.permissions = permissions;
        this.warnablePermissionIndexes = warnablePermissionIndexes;
        this.enabled = enabled;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
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
    public Set<Integer> getWarnablePermissionIndexes() {
        return warnablePermissionIndexes;
    }

    public void setWarnablePermissionIndexes(Set<Integer> warnablePermissionIndexes) {
        this.warnablePermissionIndexes = warnablePermissionIndexes;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
