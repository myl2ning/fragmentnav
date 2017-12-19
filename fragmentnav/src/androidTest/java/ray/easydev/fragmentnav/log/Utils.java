package ray.easydev.fragmentnav.log;

import java.util.Collection;

/**
 * Created by Ray on 2017/12/1.
 */

public class Utils {
    public static String joinCollections(Collection collection, String linker){
        StringBuilder sb = new StringBuilder();
        for (Object o : collection) {
            if(sb.length() != 0){
                sb.append(linker);
            }
            sb.append(o.toString());
        }

        return sb.toString();
    }
}
