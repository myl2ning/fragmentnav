package ray.easydev.fragmentnav;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import java.util.List;

/**
 * Created by Ray on 2017/11/23.
 */

public interface FragmentNav {
    int TQ_FORWARD = 0x0;
    int TQ_BACKWARD = 0x1;
    int FQ_FORWARD = 0x0;
    int FQ_BACKWARD = 0x2;

    @NonNull
    FnFragment startFragmentForResult(@Nullable FnFragment invoker, int requestCode, @NonNull FragmentIntent... intents);

    @NonNull
    FnFragment startFragment(@Nullable FnFragment invoker, @NonNull FragmentIntent... intents);

    void finish(FnFragment fragment);

    void finishTask(FnFragment fragment);

    void finishTasks(int... taskIds);

    FnFragment getCurrentFragment();

    /**
     * Find fragment by id
     * @param id {@link FnFragment#getFnId()}
     * @param <T>
     * @return
     */
    @Nullable <T extends FnFragment> T findFragment(@NonNull String id);

    /**
     * Find the first fragment which its class equals the input class
     * @param cls The fragment class
     * @param directionFlags Combine of follow flags, default(0) means search from task queue's head and fragment queue's head
     * <p>{@link FragmentNav#TQ_FORWARD}:Search from the head of the task queue</p>
     * <p>{@link FragmentNav#TQ_BACKWARD}:Search from the tail of the task queue</p>
     * <p>{@link FragmentNav#FQ_FORWARD}:Search from the head of the fragment queue</p>
     * <p>{@link FragmentNav#FQ_BACKWARD}:Search from the tail of the fragment queue</p>
     * @param <T>
     * @return
     */
    @Nullable <T extends FnFragment> T findFragment(@NonNull Class<? extends FnFragment> cls, int directionFlags);

    boolean hasFragment(Class<? extends FnFragment> cls);

    int getTaskId(FnFragment fragment);

    @NonNull List<Integer> taskIds();

    @NonNull List<FnFragment> getFragments(int taskId);

    int fragmentSize();

    FragmentActivity getActivity();

    boolean isReady();

    void saveState(Bundle bundle);

    void restoreState(Bundle bundle);

}
