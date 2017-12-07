package ray.easydev.fragmentnav.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ray.easydev.fragmentnav.FnFragment;
import ray.easydev.fragmentnav.MainActivity;
import ray.easydev.fragmentnav.test.R;
import ray.easydev.fragmentnav.utils.Androids;
import ray.easydev.fragmentnav.utils.Trace;

/**
 * Created by Ray on 2017/11/21.
 */

public class FmBase extends FnFragment implements View.OnClickListener {

    public final static FragmentResult EMPTY_RESULT = new FragmentResult();
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if(savedInstanceState != null){
//            XTrace.p(getClass(), "FragmentSize:%s", getFragmentNav().fragmentSize());
//        }
    }

    private FragmentResult fragmentResult;

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Object data) {
        super.onFragmentResult(requestCode, resultCode, data);
        fragmentResult = new FragmentResult(requestCode, resultCode, data);
    }

    public @NonNull FragmentResult getFragmentResult() {
        return fragmentResult == null ? EMPTY_RESULT  : fragmentResult;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fm_fn_test, container, false);
        childView.setTag(getClass().getSimpleName());
        ((TextView) childView.findViewById(R.id.tv_log)).setText(getClass().getSimpleName());

        Androids.setOnClickListener(childView, this, R.id.test_btn);
        Androids.setOnClickListener(childView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.test_btn1:
                        showFragmentState();
                        break;
                    case R.id.test_btn2:
                        openPermissionSettnig();
                        break;
                }

            }
        }, R.id.test_btn1, R.id.test_btn2);
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

    private void showFragmentState(){
        List<Fragment> fragmentList = getFragmentManager().getFragments();
        StringBuilder sb = new StringBuilder("ViewsCount:").append(getActualActivity().rootView.getChildCount());
        sb.append(" FragmentsSize:").append(fragmentSize());
        for (Fragment fragment : fragmentList) {
            if(fragment != null){
                sb.append("\n");
                sb.append(fragment.getClass().getSimpleName()).append(" HIDDEN:").append(fragment.isHidden())
                        .append(" VISIBLE:").append(fragment.isVisible());
            }
        }

        Trace.p("FragmentsState", sb.toString());
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
    }


    public MainActivity getActualActivity(){
        return (MainActivity) getActivity();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public static class FragmentResult {
        public int requestCode, resultCode;
        public Object result;

        public FragmentResult(int requestCode, int resultCode, Object result){
            this.requestCode = requestCode;
            this.resultCode = resultCode;
            this.result = result;
        }

        FragmentResult(){}

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof FragmentResult){
                FragmentResult out = (FragmentResult) obj;

                return requestCode == out.requestCode && resultCode == out.resultCode
                        && result == out.result;
            }
            return super.equals(obj);
        }
    }
}
