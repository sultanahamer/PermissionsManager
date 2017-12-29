package open.com.permissionsmanager;

import java.util.Arrays;

/**
 * Created by sultanm on 12/28/17.
 */

public class AndroidApplication {
    String name;
    String[] permissions;

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
