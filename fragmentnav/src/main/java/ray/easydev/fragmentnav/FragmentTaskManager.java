package ray.easydev.fragmentnav;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ray.easydev.fragmentnav.utils.LogLevel;
import ray.easydev.fragmentnav.utils.Trace;

import static ray.easydev.fragmentnav.FnUtils.INVALID_INT;
import static ray.easydev.fragmentnav.FnUtils.criticalError;
import static ray.easydev.fragmentnav.FnUtils.hasBit;
import static ray.easydev.fragmentnav.FnUtils.safeGetArguments;

/**
 * Created by Ray on 2017/11/23.
 */

@LogLevel(LogLevel.V)
class FragmentTaskManager {
    private final static Class TAG = FragmentTaskManager.class;

    private final static String _ARG_TASK_ID = "_arg_fragment_task_id_";
    private final static String _ARG_FRAGMENT_INDEX = "_arg_fragment_index";

    private SparseArray<ArrayList<FnFragment>> mFragmentTasks = new SparseArray<>();
    private FragmentNavImpl fragmentNav;
    private FnFragment mCurrFragment;
    private int mContainerViewId;

    FragmentTaskManager(FragmentNavImpl fragmentNav, int containerViewId) {
        this.fragmentNav = fragmentNav;
        mContainerViewId = containerViewId;
    }

    SparseArray<ArrayList<FnFragment>> actual(){
        return mFragmentTasks;
    }

    SparseArray<ArrayList<FnFragment>> copy(){
        SparseArray<ArrayList<FnFragment>> copy = new SparseArray<>(mFragmentTasks.size());
        int size = mFragmentTasks.size();
        for(int i = 0; i < size; i ++){
            copy.put(mFragmentTasks.keyAt(i), new ArrayList<>(mFragmentTasks.valueAt(i)));
        }

        return copy;
    }

    void commit(ArrayList<Op> ops) {
        if (ops == null || ops.isEmpty()) {
            return;
        }

        final FragmentTransaction transaction = beginTransaction();

        FnFragment showFragment = getCurrentFragment();
        final SparseArray<ArrayList<FnFragment>> fragmentTasks = mFragmentTasks;
        final SparseArray<ArrayList<FnFragment>> copy = copy();

        FnFragment lastRemoved = null;
        for (Op op : ops) {
            final FnFragment fragment = op.fragment;
            if (fragment == null) {
                continue;
            }

            setCustomAnimations(transaction, op.enterAnim, op.exitAnim);
            switch (op.op) {
                case Op.OP_ADD: {
                    ArrayList<FnFragment> task;
                    FragmentIntent fragmentIntent = fragment.getIntent();
                    if (hasBit(fragmentIntent.getFlags(), FragmentIntent.FLAG_NEW_TASK)) {
                        task = createTask();
                    } else {
                        int taskIndex = getTaskIndexByFnId(fragmentIntent.invokerId);
                        task = taskIndex < 0 ? null : fragmentTasks.valueAt(taskIndex);
                    }

                    if (task == null) criticalError("Can not find task");

                    task.add(fragment);
                    showFragment = fragment;
                    transaction.add(mContainerViewId, fragment, fragmentIntent.getTag());
                }

                break;
                case Op.OP_BRING_TO_FRONT:{
                    FragmentIntent fragmentIntent = fragment.getIntent();
                    final int taskId = getTaskId(fragment, INVALID_INT);
                    final int currTaskId = getTaskId(getCurrentFragment(), INVALID_INT);

                    ArrayList<FnFragment> fragments;
                    ArrayList<FnFragment> currFragments;
                    if (!hasBit(fragmentIntent.getFlags(), FragmentIntent.FLAG_NEW_TASK)) {
                        fragments = fragmentTasks.get(taskId);
                        if (taskId != currTaskId) {
                            currFragments = fragmentTasks.get(currTaskId);
                        } else {
                            currFragments = fragments;
                        }
                    } else {
                        //
                        fragments = fragmentTasks.get(taskId);
                        currFragments = createTask();
                    }

                    fragments.remove(fragment);
                    currFragments.add(fragment);
                    if (fragments.size() == 0) {
                        fragmentTasks.remove(taskId);
                    }

                    doBring(fragment);
                }

                case Op.OP_SHOW:
                    transaction.show(fragment);
                    showFragment = fragment;
                    break;
                case Op.OP_REMOVE: {
                    int taskIndex = getTaskIndexByFnId(fragment.getFnId());

                    List<FnFragment> task = taskIndex < 0 ? null : fragmentTasks.valueAt(taskIndex);
                    if (taskIndex == INVALID_INT || task == null) {
                        fragmentTasks.removeAt(taskIndex);
                        continue;
                    }
                    lastRemoved = fragment;
                    transaction.remove(fragment);
                    task.remove(fragment);

                    if (task.size() == 0) {
                        fragmentTasks.removeAt(taskIndex);
                    }
                }
                break;

                case Op.OP_HIDE:
                    transaction.hide(fragment);
                    break;
            }
        }

        if(lastRemoved != null) Trace.p(TAG, "Last remove:%s", lastRemoved.getClass().getSimpleName());
        try{
            //If all fragments are removed from task, just finish activity and let activity destroy the fragments
            if(!isEmpty()){
                transaction.commit();
            } else {
                showFragment = null;
            }

            setCurrentFragment(showFragment);
            setFragmentResult(lastRemoved, showFragment);
        } catch (Exception e){
            Trace.p(TAG, e);
            set(copy);
        }

        if(isEmpty()){
            finishActivity();
        }
        printTask();
    }

    private void finishActivity() {
        fragmentNav.getActivity().finish();
        Trace.p(TAG, "***** finish activity *****");
    }

    private void setFragmentResult(@Nullable FnFragment from, @Nullable FnFragment receiver){
        if ((from != null) && (receiver != null) && from.getResultCode() != null) {
            FragmentNavImpl.RequestCodeInfo requestCodeInfo = FragmentNavImpl.RequestCodeInfo.readFrom(from.getArguments());
            if (requestCodeInfo != null && requestCodeInfo.getInvokerId().equals(receiver.getFnId())) {
                receiver.onFragmentResult(requestCodeInfo.requestCode, from.getResultCode(), from.getResultData());
            }
        }
    }

    List<Integer> taskIds(){
        ArrayList<Integer> ids = new ArrayList<>(mFragmentTasks.size());
        int size = mFragmentTasks.size();
        for(int i = 0; i < size; i ++){
            ids.add(mFragmentTasks.keyAt(i));
        }

        return ids;
    }

    ArrayList<FnFragment> getFragments(int taskId){
        return mFragmentTasks.get(taskId);
    }


    private FragmentTransaction beginTransaction() {
        return fragmentNav.getActivity().getSupportFragmentManager().beginTransaction();
    }

    private void setCustomAnimations(FragmentTransaction transaction, int enter, int exit) {
        if (!fragmentNav.isReady()) {
            transaction.setCustomAnimations(0, 0);
        } else {
            transaction.setCustomAnimations(enter, exit);
        }
    }

    private void doBring(Fragment fragment){
        bringFragmentToFrontInFragmantManager(fragment);
        bringViewToFront(fragment);
    }

    private void bringFragmentToFrontInFragmantManager(Fragment fragment){
//        FragmentManagerImpl.moveFragmentToExpectedState
        try {
            Field field = fragment.getFragmentManager().getClass().getDeclaredField("mAdded");
            field.setAccessible(true);
            List<Fragment> fragments = (List<Fragment>) field.get(fragment.getFragmentManager());
            fragments.remove(fragment);
            fragments.add(fragment);
        } catch (Exception e){
            Trace.p(getClass(), "Reorder fragment in fragmentManager fail:%s", e);
        }
    }

    private void bringViewToFront(Fragment fragment) {
        final View view = fragment.getView();
        if(view != null){
            ViewGroup viewGroup = (ViewGroup) view.getParent();
            if (viewGroup != null && viewGroup.getChildAt(viewGroup.getChildCount() - 1) != fragment.getView()) {
                viewGroup.removeView(view);
                viewGroup.addView(view);
            }
        }

    }


    private void setCurrentFragment(FnFragment fragment) {
        mCurrFragment = fragment;
    }

    public FnFragment getCurrentFragment() {
        return mCurrFragment;
    }

    private int getTaskIndexByFnId(String fragmentId) {
        for (int i = 0; i < mFragmentTasks.size(); i++) {
            List<FnFragment> fragments = mFragmentTasks.valueAt(i);
            for (FnFragment fragment : fragments) {
                if (fragmentId.equals(fragment.getFnId())) {
                    return i;
                }
            }
        }

        return INVALID_INT;
    }

    public boolean hasFragment(Class cls) {
        int size = mFragmentTasks.size();
        for (int i = 0; i < size; i++) {
            List<FnFragment> fragments = mFragmentTasks.valueAt(i);
            for (FnFragment fragment : fragments) {
                if (fragment.getClass() == cls) {
                    return true;
                }
            }
        }

        return false;
    }

    boolean almostEmpty() {
        return mFragmentTasks.size() == 1 && mFragmentTasks.valueAt(0).size() == 1;
    }

    private boolean isEmpty() {
        return mFragmentTasks.size() == 0 || (mFragmentTasks.size() == 1 && mFragmentTasks.valueAt(0).size() == 0);
    }

//    public List<FnFragment> lastTask() {
//        List<FnFragment> fragments = null;
//        if (mFragmentTasks.size() == 0) {
//            return createTask();
//        } else {
//            fragments = mFragmentTasks.valueAt(mFragmentTasks.size() - 1);
//        }
//
//        return fragments;
//    }

    private ArrayList<FnFragment> createTask() {
        ArrayList<FnFragment> fragments = new ArrayList<>();
        int key = 0;
        if (mFragmentTasks.size() != 0) {
            key = mFragmentTasks.keyAt(mFragmentTasks.size() - 1) + 1;
        }
        mFragmentTasks.put(key, fragments);
        return fragments;
    }

    private List<FnFragment> getTailTask(int offset) {
        return (mFragmentTasks.size() - offset) == 0 ? null : mFragmentTasks.valueAt(mFragmentTasks.size() - 1 - offset);
    }

    public FnFragment getTailFragment(List<FnFragment> fragments, int offset) {
        return (fragments == null || (fragments.size() - offset) == 0) ? null : fragments.get(fragments.size() - offset - 1);
    }

    public <T extends FnFragment> T findFragment(Class<T> cls) {
        int size = mFragmentTasks.size();
        for (int i = 0; i < size; i++) {
            List<FnFragment> fragments = mFragmentTasks.valueAt(i);
            for (FnFragment fragment : fragments) {
                if (fragment != null && fragment.getClass() == cls) {
                    return (T) fragment;
                }
            }
        }

        return null;
    }

    public <T extends FnFragment> T findFragment(@NonNull String id){
        int size = mFragmentTasks.size();
        for (int i = 0; i < size; i++) {
            List<FnFragment> fragments = mFragmentTasks.valueAt(i);
            for (FnFragment fragment : fragments) {
                if (fragment != null && id.equals(fragment.getFnId())) {
                    return (T) fragment;
                }
            }
        }

        return null;
    }

    public int getTaskId(FnFragment fragment, int defVal) {
        for (int i = 0; i < mFragmentTasks.size(); i++) {
            List<FnFragment> fragments = mFragmentTasks.valueAt(i);
            if (fragments.contains(fragment)) {
                return mFragmentTasks.keyAt(i);
            }
        }

        return defVal;
    }

    void saveFragmentStates() {
        int size = mFragmentTasks.size();
        for (int i = 0; i < size; i++) {
            int key = mFragmentTasks.keyAt(i);
            List<FnFragment> fragments = mFragmentTasks.valueAt(i);
            int j = 0;
            for (FnFragment fragment : fragments) {
                Bundle bundle = fragment.getArguments();

                bundle.putInt(_ARG_TASK_ID, key);
                bundle.putInt(_ARG_FRAGMENT_INDEX, j);

                j++;
            }
        }
    }

    void restoreFragmentStates() {
        if(mFragmentTasks== null || mFragmentTasks.size() == 0){
            List<Fragment> fragmentList = fragmentNav.getActivity().getSupportFragmentManager().getFragments();
            Trace.p(TAG, "开始还原FragmentTask=>total fragments:%s", fragmentList == null ? "null" : fragmentList.size());
            mFragmentTasks = new SparseArray<>();
            if (fragmentList != null) {
                for (Fragment fragment : fragmentList) {
                    if (fragment instanceof FnFragment) {
                        Bundle bundle = safeGetArguments((FnFragment) fragment);
                        int taskId = bundle.getInt(_ARG_TASK_ID, -1);
                        if (taskId >= 0) {
                            ArrayList<FnFragment> task = mFragmentTasks.get(taskId);
                            if (task == null) {
                                task = new ArrayList<>();
                                mFragmentTasks.put(taskId, task);
                            }

                            task.add((FnFragment) fragment);
                        }
                    }
                }

                Comparator<FnFragment> comparator = new Comparator<FnFragment>() {
                    @Override
                    public int compare(FnFragment l, FnFragment r) {
                        int indexL = safeGetArguments(l).getInt(_ARG_FRAGMENT_INDEX, -1);
                        int indexR = safeGetArguments(r).getInt(_ARG_FRAGMENT_INDEX, -1);
                        return indexL - indexR;
                    }
                };

                for (int i = 0; i < mFragmentTasks.size(); i++) {
                    List<FnFragment> task = mFragmentTasks.valueAt(i);
                    Collections.sort(task, comparator);
                }

                if (mFragmentTasks.size() > 0) {
                    setCurrentFragment(getTailFragment(getTailTask(0), 0));
                }
            }

            Trace.p(TAG, "还原结束");
            printTask();
        }
    }

    private void set(SparseArray<ArrayList<FnFragment>> tasks){
        mFragmentTasks = tasks;
    }


    public void printTask(){
        Trace.p(TAG, taskLog());
    }

    private String taskLog() {
        final SparseArray<ArrayList<FnFragment>> mFragmentTasks = actual();
        StringBuilder sb = new StringBuilder("\n** Task **");
        sb.append("\n   Current:").append((getCurrentFragment() == null ? "NULL" : getCurrentFragment().getClass().getSimpleName())).append("\n");
        for (int i = 0; i < mFragmentTasks.size(); i++) {
            List<FnFragment> task = mFragmentTasks.valueAt(i);
            sb.append("   ").append(i).append("#").append(mFragmentTasks.keyAt(i)).append("=>[");
            if (task.size() == 0) {
                sb.append("######Task Is Empty######");
            }
            for (int j = 0; j < task.size(); j++) {
                FnFragment fragment = task.get(j);
                sb.append(j == 0 ? "" : ", ").append(fragment.getClass().getSimpleName());
            }
            sb.append("]\n");
        }
        sb.append("** Task **\n");

        return sb.toString();
    }
}
