package open.com.permissionsmanager;

import java.util.Arrays;

/**
 * Created by sultanm on 12/28/17.
 */

public class AndroidApplication {
    private String name;

    private String[] permissions;

    private int warnings;

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

    public AndroidApplication(String name, String[] permissions) {
        this.name  = name;
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        return "name='" + name + '\'' +
                ", permissions=" + Arrays.toString(permissions) +
                '}';
    }
}
