package ray.easydev.fragmentnav.sample.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.List;

import ray.easydev.fragmentnav.FnFragment;
import ray.easydev.fragmentnav.FnUtils;
import ray.easydev.fragmentnav.FragmentIntent;
import ray.easydev.fragmentnav.FragmentNav;
import ray.easydev.fragmentnav.sample.Consts;
import ray.easydev.fragmentnav.sample.R;
import ray.easydev.fragmentnav.sample.utils.Androids;

/**
 * Created by Ray on 2017/11/21.
 */

public class FmBase extends FnFragment implements View.OnClickListener, Consts {

    private TextView tvLog;

    @Override
    public void onNewIntent(FragmentIntent intent) {
        super.onNewIntent(intent);
        getView().post(new Runnable() {
            @Override
            public void run() {
                updateTitle(getView());
                showFragmentState();
                tvLog.append("\n\n" + logTitle("onNewIntent"));
            }
        });
    }

    private String logTitle(String title){
        return "******* " + title + " *******\n";
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fm_base, container, false);
        childView.setTag(getClass().getSimpleName());
        updateTitle(childView);
        tvLog = (TextView) childView.findViewById(R.id.tv_log);
        tvLog.setMovementMethod(new ScrollingMovementMethod());

        Button actionBtn = childView.findViewById(R.id.test_btn);
        Action action = (Action) getArguments().getSerializable(KEY_ACTION);
        if(action != null){
            actionBtn.setText(action.text);
        } else {
            actionBtn.setText("Back To Enter");
        }

        actionBtn.setOnClickListener(this);
        Androids.setOnClickListener(childView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.menu_back:
                        finish();
                        break;
                    case R.id.menu_to_setting:
                        openPermissionSettnig();
                        break;
                }

            }
        }, R.id.menu_to_setting, R.id.menu_back);
        return childView;
    }

    @Override
    protected void onForeground() {
        super.onForeground();
        if(getView() != null){
            getView().post(new Runnable() {
                @Override
                public void run() {
                    showFragmentState();
                }
            });
        }
    }

    private void updateTitle(View view){
        if(view != null){
            ((TextView) view.findViewById(R.id.tv_title)).setText(getClass().getSimpleName() + "[Task" + getTaskId() + "]");
        }
    }

    private void openPermissionSettnig(){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(uri);
        startActivity(intent);
    }

    protected void showFragmentState(){
        StringBuilder sb = new StringBuilder(logTitle("Fragments State") + "TaskSize:").append(getFragmentNav().taskIds().size());
        sb.append(" FragmentsSize:").append(fragmentSize());
        for (Integer id : getFragmentNav().taskIds()) {
            List<FnFragment> fragments = getFragmentNav().getFragments(id);
            sb.append("\n");
            sb.append("[").append("Task#").append(id).append("]:");
            for (FnFragment fragment : fragments) {
                sb.append("\n        [")
                        .append(fragment.getClass().getSimpleName()).append("]")
                        .append(" VISIBLE:").append(fragment.isVisible());
            }
        }
//        for (Fragment fragment : fragmentList) {
//            if(fragment != null){
//                sb.append("\n");
//                sb.append("[").append("Task#").append(getFragmentNav().getTaskId((FnFragment) fragment)).append("]").append("\n")
//                        .append(fragment.getClass().getSimpleName())
//                        .append(" HIDDEN:").append(fragment.isHidden())
//                        .append(" VISIBLE:").append(fragment.isVisible());
//            }
//        }

        tvLog.setText(sb.toString());
        if(getArguments().containsKey(KEY_STRING)){
            tvLog.append("\n\n" + logTitle("Extras") + getArguments().getString(KEY_STRING) + "\n");
        }
    }

    private List<Fragment> getFragmentsInFragmentManager(){
        try {
            Field field = getFragmentManager().getClass().getDeclaredField("mAdded");
            field.setAccessible(true);
            return (List<Fragment>) field.get(getFragmentManager());
        } catch (Exception e){
            return getFragmentManager().getFragments();
        }
    }


    private int fragmentSize() {

        List<Fragment> fragments = getFragmentManager().getFragments();
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

    @Override
    public void onClick(View v) {
        execAction();
    }

    private void execAction(){
        Action action = (Action) getArguments().getSerializable(KEY_ACTION);
        if(action != null){
            FragmentIntent[] intents = (FragmentIntent[]) getArguments().getParcelableArray(KEY_ACTION_ARG);
            switch (action){
                case START:
                case BRING_TO_FRONT:
                    if(intents != null){
                        startFragment(intents);
                    }
                    break;
                case FINISH_WITH_RESULT:{
                    setResult(RESULT_OK, "FragmentResultFrom[" + getClass().getSimpleName() + "]");
                    toFmEnter();
                }
                break;
                case FINISH:
                    finish();
                    break;
                case FINISH_MY_TASK:
                    finishTask();
                    break;
                case FINISH_TASKS:
                    break;
            }
        } else {
            toFmEnter();
        }
    }

    private void toFmEnter(){
        FnFragment enter = getFragmentNav().findFragment(FmEnter.class, 0);
        if(enter == null) return;
        if(enter.getTaskId() == getTaskId()){
            finish();
        } else {
            FragmentNav fragmentNav = getFragmentNav();
            List<Integer> ids = fragmentNav.taskIds();
            ids.remove(enter.getTaskId());
            fragmentNav.finishTasks(FnUtils.toIntArray(ids.toArray(new Integer[ids.size()])));
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }


    enum Action {
        START("Start Fragment"),
        BRING_TO_FRONT("BringToFront"),
        FINISH("Finish"),
        FINISH_WITH_RESULT("Back FmEnter With Result"),
        FINISH_MY_TASK("Finish My Task"),
        FINISH_TASKS("Finish Tasks");
        String text;

        Action(String text) {
            this.text = text;
        }
    }
}
