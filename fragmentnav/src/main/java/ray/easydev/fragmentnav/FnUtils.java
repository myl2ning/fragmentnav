package ray.easydev.fragmentnav;

import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Created by Ray on 2017/11/23.
 */

public class FnUtils {
    public final static int INVALID_INT = -1;

    public @NonNull
    static Bundle safeGetArguments(FnFragment fragment) {
        return (fragment == null || fragment.getArguments() == null) ? new Bundle() : fragment.getArguments();
    }

    public static boolean hasBit(int flags, int mask) {
        return (flags & mask) != 0;
    }

    static void criticalError(String msg) {
        throw new RuntimeException(msg);
    }

}
