package ray.easydev.fragmentnav;

import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Created by Ray on 2017/11/23.
 */

public class FnUtils {
    final static int INVALID_INT = FnFragment.INVALID_INT;

    @NonNull
    public static Bundle safeGetArguments(FnFragment fragment) {
        return (fragment == null || fragment.getArguments() == null) ? new Bundle() : fragment.getArguments();
    }

    @NonNull
    public static int[] toIntArray(Integer[] integers){
        int[] result = new int[integers == null ? 0 : integers.length];
        if(integers != null){
            int i = -1;
            for (Integer integer : integers) {
                result[++ i] = integer;
            }
        }

        return result;
    }

    static boolean hasBit(int flags, int mask) {
        return (flags & mask) != 0;
    }

    static void criticalError(String msg) {
        throw new RuntimeException(msg);
    }

}
