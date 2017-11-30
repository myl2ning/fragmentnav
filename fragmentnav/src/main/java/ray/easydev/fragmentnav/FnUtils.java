package ray.easydev.fragmentnav;

import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Created by Ray on 2017/11/23.
 */

class FnUtils {
    final static int INVALID_INT = FnFragment.INVALID_INT;

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
