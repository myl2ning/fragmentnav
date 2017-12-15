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

import java.util.List;

import ray.easydev.fragmentnav.FnFragment;
import ray.easydev.fragmentnav.FragmentIntent;
import ray.easydev.fragmentnav.sample.Consts;
import ray.easydev.fragmentnav.sample.R;
import ray.easydev.fragmentnav.sample.utils.Androids;
import ray.easydev.fragmentnav.utils.Trace;

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
                showFragmentState();
                tvLog.append("\n\n ***** onNewIntent Invoked *****");
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fm_fn_test, container, false);
        childView.setTag(getClass().getSimpleName());
        ((TextView) childView.findViewById(R.id.tv_title)).setText(getClass().getSimpleName());
        tvLog = childView.findViewById(R.id.tv_log);
        tvLog.setMovementMethod(new ScrollingMovementMethod());

        Button actionBtn = childView.findViewById(R.id.test_btn);
        Action action = (Action) getArguments().getSerializable(KEY_ACTION);
        if(action != null){
            actionBtn.setText(action.text);
        } else {
            actionBtn.setText("Back To Enter");
        }

        actionBtn.setOnClickListener(this);

        childView.post(new Runnable() {
            @Override
            public void run() {
                showFragmentState();
            }
        });

        Androids.setOnClickListener(childView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.btn_goback:
                        finish();
                        break;
                    case R.id.test_btn2:
                        openPermissionSettnig();
                        break;
                }

            }
        }, R.id.btn_goback, R.id.test_btn2);
        return childView;
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
        List<Fragment> fragmentList = getFragmentManager().getFragments();
        StringBuilder sb = new StringBuilder("ViewsCount:").append(((ViewGroup) getActivity().findViewById(R.id.layout_main)).getChildCount());
        sb.append(" FragmentsSize:").append(fragmentSize());
        for (Fragment fragment : fragmentList) {
            if(fragment != null){
                sb.append("\n");
                sb.append("[").append("Task#").append(getFragmentNav().getTaskId((FnFragment) fragment)).append("]")
                        .append(fragment.getClass().getSimpleName())
                        .append(" HIDDEN:").append(fragment.isHidden())
                        .append(" VISIBLE:").append(fragment.isVisible());
            }
        }

        Trace.p("FragmentsState", sb.toString());
        tvLog.setText(sb.toString());
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
                    if(intents != null){
                        startFragment(intents);
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
        if(getFragmentNav().getTaskId(this) == 0){
            finish();
        } else {
            List<Integer> ids = getFragmentNav().taskIds();
            ids.remove(Integer.valueOf(0));
            int[] ints = new int[ids.size()];

            int i= 0;
            for (Integer id : ids) {
                ints[i] = id;
                i ++;
            }

            getFragmentNav().finishTasks(ints);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }


    enum Action {
        START("Start Fragment"),
        FINISH("Finish"),
        FINISH_MY_TASK("Finish My Task"),
        FINISH_TASKS("Finish Tasks");
        String text;

        Action(String text) {
            this.text = text;
        }
    }
}
