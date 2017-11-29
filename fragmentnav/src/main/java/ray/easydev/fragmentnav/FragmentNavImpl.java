package ray.easydev.fragmentnav;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
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

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import ray.easydev.fragmentnav.utils.LogLevel;
import ray.easydev.fragmentnav.utils.Trace;

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

    private FragmentTask mFragmentTask;

    private int mContainerId;
    private boolean mIsActivitySavedInstanceState, mIsRestoring;

    FragmentNavImpl(@NonNull FragmentActivity activity, int resId, @Nullable Bundle savedInstanceState) {
        activity.getApplication().registerActivityLifecycleCallbacks(activityLifecycleCallbacks);

        mIsRestoring = savedInstanceState != null;
        mFragmentTask = new FragmentTask(this);
        mActivity = activity;
        mContainerId = resId;
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

    public int getViewContainerId() {
        return mContainerId;
    }

    @Override
    public FragmentTask getFragmentTask() {
        return mFragmentTask;
    }

    private FnFragment getCurrentFragment() {
        return mFragmentTask.getCurrentFragment();
    }

    public @NonNull FnFragment startFragmentForResult(@Nullable FnFragment invoker, int requestCode, @NonNull FragmentIntent... intents){
        return innerStartFragment(invoker, requestCode, intents);
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
        if (cls == null || !FnFragment.class.isAssignableFrom(cls)) {
            throw new RuntimeException("The fragment cls is null or invalid:" + cls);
        }

        if (invoker == null) {
            intent.addFlag(FragmentIntent.FLAG_NEW_TASK);
        } else {
            intent.invokerId = invoker.getFnId();
        }

        FnFragment targetFragment;
        final int tFlag = intent.getFlags();
        if (!hasBit(tFlag, FragmentIntent.FLAG_BROUGHT_TO_FRONT)
                || (targetFragment = bringToFront(cls, ops)) == null) {

            int myFlag = invoker == null ? 0 : invoker.getIntent().getFlags();

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
                if (hasBit(myFlag, FragmentIntent.FLAG_NO_HISTORY)) {
                    remove(ops, invoker).setAnim(0, invoker.getIntent().hideAnim);
                } else {
                    hide(ops, invoker);
                }
            }
        } else {
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

    public void finishTask(final int... removeTaskIds) {
        final SparseArray<ArrayList<FnFragment>> fragmentTasks = mFragmentTask.copy();
        final int taskSize = fragmentTasks.size();
        if (taskSize <= 1) {
            //如果仅有一个task，则直接finish activity
            finishActivity();
            return;
        }

        int removeTaskId = removeTaskIds[0];
        List<FnFragment> task = fragmentTasks.get(removeTaskId);
        if (task == null) {
            return;
        }

        ArrayList<Op> ops = new ArrayList<>();
        FnFragment tail = null, vFragment = null;

        boolean hasRemoveAnim = false;
        if (taskSize > 0) {
            tail = null;
            int j = 1;
            for (FnFragment fragment : task) {
                if (j == task.size()) {
                    tail = fragment;
                }

                Op op = remove(ops, fragment);
                if(!hasRemoveAnim && op.exitAnim != 0){
                    hasRemoveAnim = true;
                }

                j++;
            }

            if (taskSize > 1) {
                //存在至少2个Task队列
                //即将显示的task的index
                int showTaskIndex = 0;

                int showingTaskId = getTaskId(getCurrentFragment());
                int showingTaskIndex = fragmentTasks.indexOfKey(showingTaskId);
                int removeTaskIndex = fragmentTasks.indexOfKey(removeTaskId);

                if (removeTaskIndex == showingTaskIndex) {
                    //删除的task与正在显示的task是同一个
                    if (removeTaskIndex == taskSize - 1) {
                        //删除的task是队尾的task，获取pre task并且显示
                        //***[S][R]
                        showTaskIndex = taskSize - 2;
                    } else {
                        //删除的task不是队尾的task，获取队尾task显示
                        //***[R][S]
                        showTaskIndex = taskSize - 1;
                    }
                    List<FnFragment> showTask = fragmentTasks.valueAt(showTaskIndex);
                    vFragment = mFragmentTask.getTailFragment(showTask, 0);
                    if (vFragment != null) {
                        Op op = show(ops, vFragment);
                        if(!hasRemoveAnim || getCurrentFragment() == null || !getCurrentFragment().isVisible()){
                            op.clearAnim();
                        }
                    }
                }
            }
        }

        printOps(ops);
        if (tryCommit(ops)) {
            setFragmentResult(tail, vFragment);

            if (fragmentTasks.size() == 0) {
                //必须放在commit之后
                finishActivity();
            }
        }
    }

    public void finishTask(final FnFragment fragment) {
        if (!mIsRestoring) {
            if (fragment == null) {
                return;
            }
            final int removeTaskId = getTaskId(fragment);
            Trace.p(TAG, "Finish task:%s", removeTaskId);
            finishTask(removeTaskId);
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
            //TODO ugly...
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

        if (fragmentSize() <= 1) {
            //如果仅有一个fragment，则直接finish activity
            finishActivity();
            return;
        }

        final FnFragment currentFragment = getCurrentFragment();
        final SparseArray<ArrayList<FnFragment>> mFragmentTasks = mFragmentTask.copy();
        FnFragment vFragment = null;
        final int taskId = getTaskId(fragment);
        final int showingTaskId = getTaskId(currentFragment);

        List<FnFragment> task = mFragmentTasks.get(taskId);
        ArrayList<Op> ops = new ArrayList<>();

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

        printOps(ops);
        if (tryCommit(ops)) {
            if (empty()) {
                finishActivity();
            } else {
                setFragmentResult(fragment, vFragment);
            }
        }
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


    private Op attach(List<Op> ops, FnFragment fragment) {
        Op op = new Op(Op.OP_ATTACH, fragment);
        ops.add(op);
        return op;
    }

    private Op detach(List<Op> ops, FnFragment fragment) {
        Op op = new Op(Op.OP_DETACH, fragment);
        ops.add(op);
        return op;
    }

    private boolean tryCommit(ArrayList<Op> ops) {
        Trace.p(TAG, "tryCommit:%s, %s", mIsActivitySavedInstanceState, mIsRestoring);
        if (!isReady()) {
            Trace.p(TAG, "Called after onSavedInstance or is during restore, save ops");
            mPendingOps.add(new PendingOps(ops));

            if (mIsActivitySavedInstanceState) {
                savePendingOps(mPendingOps);
            }

            return false;
        } else {
            mFragmentTask.commit(ops);
            return true;
        }
    }

    private void setFragmentResult(FnFragment removed, FnFragment willShow) {
        if ((removed != null) && (willShow != null) && removed.getResultCode() != null) {
            RequestCodeInfo requestCodeInfo = RequestCodeInfo.readFrom(removed.getArguments());
            if (requestCodeInfo != null && requestCodeInfo.getInvokerId().equals(willShow.getFnId())) {
                willShow.onFragmentResult(requestCodeInfo.requestCode, removed.getResultCode(), removed.getResultData());
            }
        }
    }

    private void printOps(List<Op> ops) {
        StringBuilder sb = new StringBuilder();
        for (Op op : ops) {
            sb.append("\n").append(op);
        }
        Trace.p(TAG, sb.toString());
    }

    private void printPendingOps(List<PendingOps> pendingOps) {
        if (pendingOps == null || pendingOps.isEmpty()) {
            Trace.p(TAG, "No pending ops");
            return;
        }

        int i = 0;
        for (PendingOps pendingOp : pendingOps) {
            System.out.println("PendingOps" + (i++) + ":");
            if (pendingOp.ops != null) {
                printOps(pendingOp.ops);
            }
        }
    }


    public void onActivityResumed() {
        mIsRestoring = mIsActivitySavedInstanceState = false;

        if (pendingOpsRestoreHelper != null)
            pendingOpsRestoreHelper.deleteSavedFile(getActivity().getApplicationContext());
        commitPendingOps();
        commitPendingCalls();
    }

    public void onBackPressed() {
        finish(getCurrentFragment());
    }

    private boolean empty() {
        return mFragmentTask.empty();
    }

    private int fragmentSize() {
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

    private int getTaskId(FnFragment fragment) {
        return mFragmentTask.getTaskId(fragment, -1);
    }

    public static class PendingOps implements Parcelable, Serializable {
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

        PendingOps(Parcel in) {
            ops = in.createTypedArrayList(Op.CREATOR);
        }

        public static final Creator<PendingOps> CREATOR = new Creator<PendingOps>() {
            @Override
            public PendingOps createFromParcel(Parcel in) {
                return new PendingOps(in);
            }

            @Override
            public PendingOps[] newArray(int size) {
                return new PendingOps[size];
            }
        };

        public void commit(FragmentNavImpl fragmentNavV3) {
            fragmentNavV3.mFragmentTask.commit(ops);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeTypedList(ops);
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

    private void savePendingOps(ArrayList<PendingOps> pendingOps) {
        if (pendingOpsRestoreHelper != null) {
            pendingOpsRestoreHelper.save(getActivity().getApplicationContext(), pendingOps);
            printPendingOps(pendingOps);
        }
    }


    //由于保存未启动的fragment的状态比较麻烦，故app还原时commit pending ops功能暂不开放
    private PendingOpsRestoreHelper pendingOpsRestoreHelper;// = new PendingOpsRestoreHelper();

    public void saveState(Bundle bundle) {
        mFragmentTask.saveFragmentStates();
        if (pendingOpsRestoreHelper != null)
            pendingOpsRestoreHelper.onSaveInstanceState(this, bundle);

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

            if (pendingOpsRestoreHelper != null)
                mPendingOps = pendingOpsRestoreHelper.restore(this, savedInstanceState);

            mIsRestoring = false;

        }
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

    static class PendingOpsRestoreHelper {
        private final static String SAVED_TIMES = IdGenerator.fromClass(PendingOpsRestoreHelper.class) + "_SAVED_TIMES";
        private final static String FRAGMENTNAV_ID = IdGenerator.fromClass(PendingOpsRestoreHelper.class) + "_FRAGMENTNAV_ID";

        private String mFragmentNavId;
        private int mSavedTimes = 0;

        void onSaveInstanceState(FragmentNavImpl fragmentNavV3, Bundle outState) {
            if (mFragmentNavId == null) {
                mFragmentNavId = Integer.toHexString(fragmentNavV3.hashCode());
            }

            outState.putString(FRAGMENTNAV_ID, mFragmentNavId);
            outState.putInt(SAVED_TIMES, ++mSavedTimes);

            Trace.p(getClass(), "Update pending ops save id:%s", getSaveId());
        }

        ArrayList<PendingOps> restore(@NonNull FragmentNavImpl fragmentNav, Bundle bundle) {
            mFragmentNavId = bundle.getString(FRAGMENTNAV_ID, mFragmentNavId);
            mSavedTimes = bundle.getInt(SAVED_TIMES, mSavedTimes);

            Trace.p(getClass(), "Restore pending ops with id:%s", getSaveId());

            long time = System.currentTimeMillis();
            File saveFile = getSaveFile(fragmentNav.getActivity().getApplicationContext(), getSaveId());
            ObjectInputStream ois = null;
            try {
                if (saveFile.isFile()) {
                    ois = new ObjectInputStream(new FileInputStream(saveFile));
                    ArrayList<PendingOps> pendingOps = (ArrayList<PendingOps>) ois.readObject();
                    for (PendingOps pendingOp : pendingOps) {
                        if (pendingOp.ops != null) {
                            for (Op op : pendingOp.ops) {
                                op.fragment = fragmentNav.mFragmentTask.findFragment(op.fragmentId);
                            }
                        }
                    }

                    return pendingOps;
                }
            } catch (Exception e) {
                //ignore
                Trace.e(getClass(), e);
            } finally {
                Trace.p(getClass(), "Restore pending ops used:%sms", (System.currentTimeMillis() - time));
                slientClose(ois);
            }

            return new ArrayList<>(0);

        }


        void save(@NonNull Context c, @NonNull ArrayList<PendingOps> pendingOps) {
            Trace.p(getClass(), "Save pending ops with id:%s", getSaveId());
            if (!pendingOps.isEmpty()) SaveTask.save(c, getSaveId(), pendingOps);
        }

        String getSaveId() {
            return mFragmentNavId + "_" + mSavedTimes;
        }

        void deleteSavedFile(Context c) {
            if (!TextUtils.isEmpty(mFragmentNavId) && mSavedTimes > 0) {
                File file = getSaveFile(c, getSaveId());
                if (!file.delete()) {
                    file.deleteOnExit();
                }
            }
        }

        static File getSaveFile(Context c, String fileName) {
            return new File(c.getCacheDir(), fileName);
        }

        private static class SaveTask extends AsyncTask<Object, Void, Boolean> {

            public static void save(@NonNull Context c, @NonNull String fileName, @NonNull ArrayList<PendingOps> ops) {
                new SaveTask().execute(c, fileName, ops);
            }

            @Override
            protected Boolean doInBackground(Object[] objs) {
                ObjectOutputStream oos = null;
                try {
                    Context context = (Context) objs[0];
                    String saveId = (String) objs[1];
                    ArrayList<PendingOps> pendingOps = (ArrayList<PendingOps>) objs[2];

                    File file = getSaveFile(context, saveId);
                    if (file.isFile() || file.createNewFile()) {
                        FileOutputStream fos = new FileOutputStream(file);
                        oos = new ObjectOutputStream(fos);
                        oos.writeObject(pendingOps);
                        oos.flush();
                    }

                    return true;
                } catch (Exception e) {
                    //ignore
                } finally {
                    slientClose(oos);
                }

                return false;
            }
        }

        private static void slientClose(Closeable closeable) {
            try {
                if (closeable != null) closeable.close();
            } catch (Exception e) {
                //ignore
            }
        }
    }

    static class RequestCodeInfo implements Parcelable {
        private final static String KEY = IdGenerator.fromClass(RequestCodeInfo.class);
        String invokerId;
        int requestCode;

        public RequestCodeInfo(String invokerId, int requestCode) {
            this.invokerId = invokerId;
            this.requestCode = requestCode;
        }

        private RequestCodeInfo(Parcel in) {
            invokerId = in.readString();
            requestCode = in.readInt();
        }

        public String getInvokerId() {
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
