package ray.easydev.fragmentnav;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

/**
 * Created by Ray on 2017/11/29.
 */

public class FragmentNavHelper {
//    public static void init(@NonNull Application application){
//        FragmentNavImpl.init(application);
//    }

    public @NonNull static FragmentNav createBeforeSuperOnCreate(FragmentActivity fragmentActivity, int resId, Bundle savedInstanceState){
        return new FragmentNavImpl(fragmentActivity, resId, savedInstanceState);
    }

    public static void onBackPressed(@NonNull FragmentNav fragmentNav){
        fragmentNav.finish(fragmentNav.getCurrentFragment());
    }
}
