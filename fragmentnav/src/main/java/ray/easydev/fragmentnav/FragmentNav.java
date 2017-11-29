package ray.easydev.fragmentnav;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

/**
 * Created by Ray on 2017/11/23.
 */

public interface FragmentNav {
    @NonNull
    FnFragment startFragmentForResult(@Nullable FnFragment invoker, int requestCode, @NonNull FragmentIntent... intents);

    @NonNull
    FnFragment startFragment(@Nullable FnFragment invoker, @NonNull FragmentIntent... intents);

    void finish(FnFragment fragment);

    void finishTask(FnFragment fragment);

    void finishTask(int... taskIds);

    int getViewContainerId();

    FragmentTask getFragmentTask();

    FragmentActivity getActivity();

    boolean isReady();

    void saveState(Bundle bundle);

    void restoreState(Bundle bundle);
}
