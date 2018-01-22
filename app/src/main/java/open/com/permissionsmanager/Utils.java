package open.com.permissionsmanager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by sultanm on 1/23/18.
 */

public class Utils {
    public static HashSet<String> makeHashSet(String content, String delimiter){
        return new HashSet<>(Arrays.asList(content.split(delimiter)));
    }

    public static String makeString(Set<String> set, String delimiter) {
        StringBuffer stringBuffer = new StringBuffer();
        for(String key : set){
            stringBuffer.append(key).append(delimiter);
        }
        return stringBuffer.toString();
    }
}
