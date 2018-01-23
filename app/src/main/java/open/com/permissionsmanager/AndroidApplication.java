package open.com.permissionsmanager;

import java.util.List;

/**
 * Created by sultanm on 12/28/17.
 */

public class AndroidApplication {

    private String packageName;
    private String name;
    private List<String> nonwarnablePermissions;
    private List<String> warnablePermissions;
    private boolean enabled;

    public AndroidApplication(String name, String packageName, List<String> nonwarnablePermissions, List<String> warnablePermissions, boolean enabled) {
        this.packageName = packageName;
        this.name = name;
        this.nonwarnablePermissions = nonwarnablePermissions;
        this.warnablePermissions = warnablePermissions;
        this.enabled = enabled;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<String> getNonwarnablePermissions() {
        return nonwarnablePermissions;
    }

    public void setNonwarnablePermissions(List<String> nonwarnablePermissions) {
        this.nonwarnablePermissions = nonwarnablePermissions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public List<String> getWarnablePermissions() {
        return warnablePermissions;
    }

    public void setWarnablePermissions(List<String> warnablePermissions) {
        this.warnablePermissions = warnablePermissions;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
