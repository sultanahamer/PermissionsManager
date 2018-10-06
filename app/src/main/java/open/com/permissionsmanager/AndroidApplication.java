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

    public AndroidApplication(String packageName){
        this.packageName = packageName;
    }

    public AndroidApplication(String name, String packageName, List<String> nonwarnablePermissions, List<String> warnablePermissions) {
        this.packageName = packageName;
        this.name = name;
        this.nonwarnablePermissions = nonwarnablePermissions;
        this.warnablePermissions = warnablePermissions;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AndroidApplication that = (AndroidApplication) o;

        return packageName.equals(that.packageName);
    }
}
