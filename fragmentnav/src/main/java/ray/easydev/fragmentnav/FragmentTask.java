package ray.easydev.fragmentnav;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.SparseArray;
import android.view.ViewGroup;

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
public class FragmentTask {
    private final static Class TAG = FragmentTask.class;

    private final static String _ARG_TASK_ID = "_arg_fragment_task_id_";
    private final static String _ARG_FRAGMENT_INDEX = "_arg_fragment_index";

    private SparseArray<ArrayList<FnFragment>> mFragmentTasks = new SparseArray<>();
    private FragmentNav fragmentNav;
    private FnFragment mCurrFragment;

    FragmentTask(FragmentNav fragmentNav) {
        this.fragmentNav = fragmentNav;
    }

    private SparseArray<ArrayList<FnFragment>> actual(){
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

    public void commit(ArrayList<Op> ops) {
        if (ops == null) {
            return;
        }

        final FragmentTransaction transaction = beginTransaction();

        FnFragment showFragment = getCurrentFragment();
        final SparseArray<ArrayList<FnFragment>> fragmentTasks = mFragmentTasks;
        final SparseArray<ArrayList<FnFragment>> copy = copy();

        for (Op op : ops) {
            FnFragment fragment = op.fragment;
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
                    transaction.add(fragmentNav.getViewContainerId(), fragment);
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

                    bringViewToFront(fragment);
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

                case Op.OP_ATTACH:
                    transaction.attach(fragment);
                    break;
                case Op.OP_DETACH:
                    transaction.detach(fragment);
                    break;
            }
        }

        try{
            transaction.commit();
            setCurrentFragment(showFragment);
        } catch (Exception e){
            set(copy);
        }

        printTask();
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

    private void bringViewToFront(FnFragment fragment) {
        ViewGroup viewGroup = (ViewGroup) fragmentNav.getActivity().findViewById(fragmentNav.getViewContainerId());
        if (fragment.getView() != null && viewGroup != null && viewGroup.getChildAt(viewGroup.getChildCount() - 1) != fragment.getView()) {
            viewGroup.removeView(fragment.getView());
            viewGroup.addView(fragment.getView());
        }
    }


    private void setCurrentFragment(FnFragment fragment) {
        mCurrFragment = fragment;
    }

    public boolean hasCurrentFragment() {
        return mCurrFragment != null && mCurrFragment.isAdded();
    }

    public FnFragment getCurrentFragment() {
        return mCurrFragment;
    }

    public int getTaskIndexByFnId(String fragmentId) {
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

    public boolean almostEmpty() {
        return mFragmentTasks.size() == 1 && mFragmentTasks.valueAt(0).size() == 1;
    }

    public boolean empty() {
        return mFragmentTasks.size() == 0 || (mFragmentTasks.size() == 1 && mFragmentTasks.valueAt(0).size() == 0);
    }

    public List<FnFragment> lastTask() {
        List<FnFragment> fragments = null;
        if (mFragmentTasks.size() == 0) {
            return createTask();
        } else {
            fragments = mFragmentTasks.valueAt(mFragmentTasks.size() - 1);
        }

        return fragments;
    }

    private ArrayList<FnFragment> createTask() {
        ArrayList<FnFragment> fragments = new ArrayList<>();
        int key = 0;
        if (mFragmentTasks.size() != 0) {
            key = mFragmentTasks.keyAt(mFragmentTasks.size() - 1) + 1;
        }
        mFragmentTasks.put(key, fragments);
        return fragments;
    }

    public List<FnFragment> getTailTask(int offset) {
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

    public FnFragment removeFramentInTask(int taskId, Class cls) {
        if (taskId > mFragmentTasks.size()) {
            return null;
        }

        List<FnFragment> task = mFragmentTasks.get(taskId);
        int i = task.size();
        FnFragment result = null;
        while ((--i) >= 0) {
            FnFragment fragment = task.get(i);
            if (fragment != null && fragment.getClass() == cls) {
                result = fragment;
                break;
            }

        }

        task.remove(result);
        return result;
    }

    public void removeFramentInTask(int taskId, FnFragment fragment) {
        if (taskId > mFragmentTasks.size()) {
            return;
        }
        List<FnFragment> task = mFragmentTasks.get(taskId);
        task.remove(fragment);
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
        StringBuilder sb = new StringBuilder("\n** Start **");
        sb.append("\nCurrent [").append((getCurrentFragment() == null ? "NULL" : getCurrentFragment().getClass().getSimpleName())).append("]\n");
        for (int i = 0; i < mFragmentTasks.size(); i++) {
            List<FnFragment> task = mFragmentTasks.valueAt(i);
            sb.append("").append(i).append("#").append(mFragmentTasks.keyAt(i)).append("=>[");
            if (task.size() == 0) {
                sb.append("######Task Is Empty######");
            }
            for (int j = 0; j < task.size(); j++) {
                FnFragment fragment = task.get(j);
                sb.append(j == 0 ? "" : ", ").append(fragment.getClass().getSimpleName());
            }
            sb.append("]\n");
        }
        sb.append("** End **\n");

        return sb.toString();
    }
}
