package ray.easydev.fragmentnav;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.SparseArray;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ray.easydev.fragmentnav.utils.LogLevel;
import ray.easydev.fragmentnav.utils.Trace;

import static ray.easydev.fragmentnav.FnUtils.INVALID_INT;
import static ray.easydev.fragmentnav.FnUtils.criticalError;
import static ray.easydev.fragmentnav.FnUtils.hasBit;

/**
 * Created by Ray on 2016/9/19.
 */
@LogLevel(LogLevel.V)
class FragmentNavImpl implements FragmentNav {
    final static Class TAG = FragmentNavImpl.class;

    private FragmentActivity mActivity;
    private Handler mHandler;

    private ArrayList<PendingOps> mPendingOps = new ArrayList<>();
    private ArrayList<Runnable> mPendingCalls = new ArrayList<>();

    boolean mIsActivitySavedInstanceState, mIsRestoring;

    FragmentTaskManager mFragmentTask;

    FragmentNavImpl(@NonNull FragmentActivity activity, int containerViewID, @Nullable Bundle savedInstanceState) {
        activity.getApplication().registerActivityLifecycleCallbacks(activityLifecycleCallbacks);

        mIsRestoring = savedInstanceState != null;
        mFragmentTask = new FragmentTaskManager(this, containerViewID);
        mActivity = activity;
        initHandler(activity);
    }

    private void initHandler(FragmentActivity activity) {
        try {
            Field field = FragmentActivity.class.getDeclaredField("mHandler");
            field.setAccessible(true);
            mHandler = (Handler) field.get(activity);

            Trace.p(getClass(), "Init handler:%s", mHandler != null);
        } catch (Exception e) {
            mHandler = new Handler();
        }
    }

    public @NonNull
    FragmentActivity getActivity() {
        return mActivity;
    }

    public FnFragment getCurrentFragment() {
        return mFragmentTask.getCurrentFragment();
    }

    @Override
    public <T extends FnFragment> T findFragment(@NonNull String id) {
        return mFragmentTask.findFragment(id);
    }

    @Override
    public boolean hasFragment(Class<? extends FnFragment> cls) {
        return mFragmentTask.hasFragment(cls);
    }

    public @NonNull FnFragment startFragmentForResult(@Nullable FnFragment invoker, int requestCode, @NonNull FragmentIntent intent){
        return innerStartFragment(invoker, requestCode, intent);
    }

    public @NonNull FnFragment startFragment(@Nullable final FnFragment invoker, @NonNull final FragmentIntent... intents) {
        return innerStartFragment(invoker, null, intents);
    }

    private @NonNull FnFragment innerStartFragment(@Nullable FnFragment invoker, Integer requestCode, @NonNull FragmentIntent... intents) {
        if(intents.length == 0){
            criticalError("Intents for start fragmemt");
        }
        ArrayList<Op> ops = new ArrayList<>();
        FnFragment finalFragment = invoker;

        RequestCodeInfo requestCodeInfo = null;
        if(invoker != null && requestCode != null){
            requestCodeInfo = new RequestCodeInfo(invoker.getFnId(), requestCode);
        }

        for (FragmentIntent intent : intents) {
            finalFragment = startSingleFragment(finalFragment, intent, ops);

            if(requestCodeInfo != null){
                requestCodeInfo.writeTo(finalFragment.getArguments());
            }
        }

        printOps(ops);
        tryCommit(ops);
        return finalFragment;
    }

    private @NonNull FnFragment startSingleFragment(FnFragment invoker, FragmentIntent intent, List<Op> ops) {
        Class cls = intent.getTargetCls();
        if (invoker == null) {
            intent.addFlag(FragmentIntent.FLAG_NEW_TASK);
        } else {
            intent.invokerId = invoker.getFnId();
        }

        FnFragment targetFragment;
        final int tFlag = intent.getFlags();
        if (!hasBit(tFlag, FragmentIntent.FLAG_BROUGHT_TO_FRONT)
                || (targetFragment = bringToFront(cls, ops)) == null) {

            try {
                targetFragment = (FnFragment) cls.newInstance();
                targetFragment.setArguments(intent.getExtras());
            } catch (Exception e) {
                throw new RuntimeException("Can not instance fragment");
            }

            targetFragment.setFnId(IdGenerator.gen());
            targetFragment.setIntent(intent);

            add(ops, targetFragment);

            if(invoker != null){
//                if (hasBit(myFlag, FragmentIntent.FLAG_NO_HISTORY)) {
//                    remove(ops, invoker).setAnim(0, invoker.getIntent().hideAnim);
//                } else {
                hide(ops, invoker);
//                }
            }
        } else {
            targetFragment.getIntent().setFlags(intent.getFlags());
            targetFragment.onNewIntent(intent);
        }

        return targetFragment;
    }

    private FnFragment bringToFront(Class cls, List<Op> ops) {
        if (getCurrentFragment() == null || getCurrentFragment().getClass() == cls) {
            return getCurrentFragment();
        }

        FnFragment fragment = mFragmentTask.findFragment(cls);
        if (fragment != null) {
            bringToFront(ops, fragment);
            hide(ops, getCurrentFragment());
        }

        return fragment;
    }

    public void finishTasks(final int... removeTaskIds) {
        Arrays.sort(removeTaskIds);

        List<Integer> ids = taskIds();
        ArrayList<Op> ops = new ArrayList<>();
        boolean hasRemoveAnim = false, includeCurrent = false;
        final int currentId = getTaskId(getCurrentFragment());

        for (Integer id : removeTaskIds) {
            List<FnFragment> fragments = getFragments(id);
            for (FnFragment fragment : fragments) {
                Op op = new Op(Op.OP_REMOVE, fragment);
                if(!hasRemoveAnim && op.exitAnim != 0){
                    hasRemoveAnim = true;
                }

                ops.add(op);
            }

            ids.remove(id);
            if(currentId == id){
                includeCurrent = true;
            }
        }

        FnFragment showFragment = null;
        if(includeCurrent && !ids.isEmpty()){
            List<FnFragment> fragments = null;
            for(int index = ids.size() - 1; index >= 0 && ((fragments == null || fragments.isEmpty())); index -- ){
                fragments = getFragments(ids.get(index));
            }

            showFragment = fragments != null && !fragments.isEmpty() ? fragments.get(fragments.size() - 1) : null;
        }

        if(showFragment != null){
            Op opShow = new Op(Op.OP_SHOW, showFragment);
            if(!hasRemoveAnim){
                opShow.clearAnim();
            }
            ops.add(opShow);
        }

        printOps(ops);
        tryCommit(ops);
    }

    public void finishTask(final FnFragment fragment) {
        if (!mIsRestoring) {
            if (fragment == null) {
                return;
            }
            final int removeTaskId = getTaskId(fragment);
            Trace.p(TAG, "Finish task:%s", removeTaskId);
            finishTasks(removeTaskId);
        } else {
            Trace.p(TAG, "Add pending call");
            addPendingCall(new Runnable() {
                @Override
                public void run() {
                    finishTask(fragment);
                }
            });
        }
    }

    public void finish(final FnFragment fragment) {
        if (mIsRestoring) {
            //TODO Ugly but simple...
            addPendingCall(new Runnable() {
                @Override
                public void run() {
                    finish(fragment);
                }
            });
            return;
        }

        if (fragment == null || (fragment instanceof OnBackPressListener && ((OnBackPressListener) fragment).onBackPressed())) {
            return;
        }

        ArrayList<Op> ops = new ArrayList<>();

        int fragmentSize = fragmentSize();
        if (fragmentSize == 0) {
            finishActivity();
            return;
        } else if (fragmentSize == 1){
            Op op = new Op(Op.OP_REMOVE, getCurrentFragment());
            op.clearAnim();
            ops.add(op);
        } else {
            final FnFragment currentFragment = getCurrentFragment();
            final SparseArray<ArrayList<FnFragment>> mFragmentTasks = mFragmentTask.copy();
            FnFragment vFragment = null;
            final int taskId = getTaskId(fragment);
            final int showingTaskId = getTaskId(currentFragment);

            List<FnFragment> task = mFragmentTasks.get(taskId);

            if (taskId == showingTaskId && task != null) {
                //如果删除的fragment所在的task正在显示，则选择一个fragment显示
                if (fragment == currentFragment) {
                    //如果删除的就是当前显示的fragment
                    if (task.size() > 1) {
                        //该task有至少2个fragment，则获取pre fragment
                        //***[S][R]
                        vFragment = mFragmentTask.getTailFragment(task, 1);//task.get(task.size() - 1);
                    } else {
                        //该task仅有一个fragment，取尾部的task显示
                        if (mFragmentTasks.size() > 1) {
                            List<FnFragment> showTask = null;
                            int index = mFragmentTasks.size() - 1;
                            int willShowTaskId;

                            do {
                                willShowTaskId = mFragmentTasks.keyAt(index);
                                index--;
                            } while (showingTaskId == willShowTaskId);

                            showTask = mFragmentTasks.get(willShowTaskId);
                            vFragment = mFragmentTask.getTailFragment(showTask, 0);
                        }
                    }
                } else {
                    //删除的不是当前显示的fragment,无需操作
                }
            }

            Op opRemove = remove(ops, fragment);

            Op opShow = null;
            if (vFragment != null) {
                opShow = show(ops, vFragment);
            }

            if (!fragment.isVisible() || mFragmentTask.almostEmpty()) {
                opRemove.clearAnim();
            }

            if(opShow != null && opRemove.exitAnim == 0){
                opShow.clearAnim();
            }
        }

        printOps(ops);
        tryCommit(ops);
    }


    private void finishActivity() {
        mActivity.finish();
    }

    private void addPendingCall(Runnable runnable){
        mPendingCalls.add(runnable);
    }

    public boolean isReady() {
        return !mIsActivitySavedInstanceState && !mIsRestoring;
    }

    private Op add(List<Op> ops, FnFragment fragment) {
        Op op = new Op(Op.OP_ADD, fragment);
        ops.add(op);
        return op;
    }

    private Op remove(List<Op> ops, FnFragment fragment) {
        Op op = new Op(Op.OP_REMOVE, fragment);
        ops.add(op);
        return op;

    }

    private Op hide(List<Op> ops, FnFragment fragment) {
        Op op = new Op(Op.OP_HIDE, fragment);
        ops.add(op);
        return op;
    }

    private Op show(List<Op> ops, FnFragment fragment) {
        Op op = new Op(Op.OP_SHOW, fragment);
        ops.add(op);
        return op;
    }

    private Op bringToFront(List<Op> ops, FnFragment fragment) {
        Op op = new Op(Op.OP_BRING_TO_FRONT, fragment);
        ops.add(op);
        return op;
    }


    private boolean tryCommit(ArrayList<Op> ops) {
        if (!isReady()) {
            Trace.p(TAG, "Called after onSavedInstance or is during restore, save ops");
            mPendingOps.add(new PendingOps(ops));
            return false;
        } else {
            mFragmentTask.commit(ops);
            return true;
        }
    }

    private void printOps(List<Op> ops) {
        StringBuilder sb = new StringBuilder("\n** Ops **");
        for (Op op : ops) {
            sb.append("\n   ").append(op);
        }
        sb.append("\n** Ops **");
        Trace.p(TAG, sb.toString());
    }

    private void printPendingOps(List<PendingOps> pendingOps) {
        if (pendingOps == null || pendingOps.isEmpty()) {
            Trace.p(TAG, "No pending ops");
            return;
        }

        int i = 0;
        for (PendingOps pendingOp : pendingOps) {
            if (pendingOp.ops != null) {
                printOps(pendingOp.ops);
            }
        }
    }


    void onActivityResumed() {
        mIsRestoring = mIsActivitySavedInstanceState = false;
        commitPendingOps();
        commitPendingCalls();
    }

    public void onBackPressed() {
        finish(getCurrentFragment());
    }

    public int fragmentSize() {
        if (mActivity == null) {
            return 0;
        }

        List<Fragment> fragments = mActivity.getSupportFragmentManager().getFragments();
        if (fragments == null) {
            return 0;
        }

        int i = 0;
        for (Fragment fragment : fragments) {
            if (fragment != null) {
                i++;
            }
        }
        return i;

    }

    private void commitPendingOps() {
        if (mPendingOps != null && !mPendingOps.isEmpty()) {
            Trace.p(TAG, "Commit pending transactions");
            printPendingOps(mPendingOps);
            for (PendingOps pendingOps : mPendingOps) {
                pendingOps.commit(this);
            }

            mPendingOps.clear();

        }
    }

    private void commitPendingCalls() {
        for (Runnable pendingCall : mPendingCalls) {
            pendingCall.run();
        }

        mPendingCalls.clear();
    }

    public int getTaskId(FnFragment fragment) {
        return mFragmentTask.getTaskId(fragment, INVALID_INT);
    }

    @Override
    @NonNull public List<Integer> taskIds() {
        return mFragmentTask.taskIds();
    }

    @Override
    @NonNull public List<FnFragment> getFragments(int taskId) {
        return Collections.unmodifiableList(mFragmentTask.getFragments(taskId));
    }

    static class PendingOps implements Serializable {
        ArrayList<Op> ops;

        PendingOps(ArrayList<Op> ops) {
            this.ops = ops;
            clearAnims();
        }

        private void clearAnims() {
            if (ops != null) {
                for (Op op : ops) {
                    op.setAnim(0, 0);
                }
            }
        }

        void commit(FragmentNavImpl fragmentNavV3) {
            fragmentNavV3.mFragmentTask.commit(ops);
        }
    }

    static class IdGenerator {
        private static long seed = 0;
        private static int offset = 0;

        synchronized static String gen() {
            long time = System.currentTimeMillis();
            if (time == seed) {
                offset++;
            } else {
                offset = 0;
            }

            seed = time;

            return genIdByTime(time, offset);
        }

        static String fromClass(Class cls) {
            return cls.getCanonicalName() + "_" + Integer.toHexString(cls.getCanonicalName().hashCode());
        }

        private static String genIdByTime(long time, int offset) {
            if (offset == 0) {
                return time + "";
            }

            return time + "_" + offset;
        }
    }


    public void saveState(Bundle bundle) {
        mFragmentTask.saveFragmentStates();

        mIsActivitySavedInstanceState = true;

        Trace.p(TAG, "Save state done");
    }

    /**
     * Must be called after {@link Activity#onCreate(Bundle)}
     *
     * @param savedInstanceState
     */
    public void restoreState(@NonNull Bundle savedInstanceState) {
        if (mIsRestoring) {
            mFragmentTask.restoreFragmentStates();
            mIsRestoring = false;

        }
    }

    @NonNull
    @Override
    public Handler getHandler() {
        return mHandler;
    }

    private Application.ActivityLifecycleCallbacks activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {

        private @Nullable FragmentNavImpl getFragmentNavImpl(Activity activity){
            if(activity == mActivity && activity instanceof FnActivity && ((FnActivity) activity).getFragmentNav() instanceof FragmentNavImpl){
                return (FragmentNavImpl) ((FnActivity) activity).getFragmentNav();
            }

            return null;
        }


        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            FragmentNavImpl fragmentNav = getFragmentNavImpl(activity);
            if(fragmentNav != null){
                fragmentNav.onActivityResumed();
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, final Bundle outState) {
            FragmentNavImpl fragmentNav = getFragmentNavImpl(activity);
            if(fragmentNav != null){
                fragmentNav.saveState(outState);
            }
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            if(activity == mActivity){
                mActivity.getApplication().unregisterActivityLifecycleCallbacks(this);
            }
        }
    };


    static class RequestCodeInfo implements Parcelable {
        private final static String KEY = IdGenerator.fromClass(RequestCodeInfo.class);
        String invokerId;
        int requestCode;

        RequestCodeInfo(String invokerId, int requestCode) {
            this.invokerId = invokerId;
            this.requestCode = requestCode;
        }

        private RequestCodeInfo(Parcel in) {
            invokerId = in.readString();
            requestCode = in.readInt();
        }

        String getInvokerId() {
            return invokerId;
        }

        public static final Creator<RequestCodeInfo> CREATOR = new Creator<RequestCodeInfo>() {
            @Override
            public RequestCodeInfo createFromParcel(Parcel in) {
                return new RequestCodeInfo(in);
            }

            @Override
            public RequestCodeInfo[] newArray(int size) {
                return new RequestCodeInfo[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(invokerId);
            dest.writeInt(requestCode);
        }

        void writeTo(Bundle bundle){
            if(bundle != null){
                bundle.putParcelable(KEY, this);
            }
        }

        static RequestCodeInfo readFrom(Bundle bundle){
            return bundle == null ? null : (RequestCodeInfo) bundle.getParcelable(KEY);
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof RequestCodeInfo){
                return !TextUtils.isEmpty(invokerId) && invokerId.equals(((RequestCodeInfo) obj).invokerId);
            }

            return super.equals(obj);
        }
    }
}
