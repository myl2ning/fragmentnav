package ray.easydev.fragmentnav.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ray.easydev.fragmentnav.FragmentIntent;
import ray.easydev.fragmentnav.utils.Trace;


/**
 * Created by Ray on 2017/11/21.
 */

public class Fm01 extends FmBase {
    @Override
    public void onClick(View v) {
        super.onClick(v);
//        startFragment(new FragmentIntent(Fm02.class));
//        testBatchStart(); //测试批量启动
        testFinishTask();
//        testStartForResult();
//        testStartSingle();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        if(isVisible()){
//            getView().post(new Runnable() {
//                @Override
//                public void run() {
//                    startFragment(new FragmentIntent(Fm03.class));
//                }
//            });
//        }
    }

    private void testFinishTask(){
        FragmentIntent intent02 = new FragmentIntent(Fm02.class);
        FragmentIntent intent11 = new FragmentIntent(Fm11.class).addFlag(FragmentIntent.FLAG_NEW_TASK);
        FragmentIntent intent12 = new FragmentIntent(Fm12.class);
        getFragmentNav().startFragment(this, intent02, intent11, intent12);
    }

    private void testBatchStart(){
        FragmentIntent intent02 = new FragmentIntent(Fm02.class);
        FragmentIntent intent11 = new FragmentIntent(Fm11.class).addFlag(FragmentIntent.FLAG_NEW_TASK);
        FragmentIntent intent12 = new FragmentIntent(Fm12.class);
        FragmentIntent intent21 = new FragmentIntent(Fm21.class).addFlag(FragmentIntent.FLAG_NEW_TASK);

        getFragmentNav().startFragment(this, intent02, intent11, intent12, intent21);
    }

    private void testStartForResult(){
        FragmentIntent intent11 = new FragmentIntent(Fm11.class).addFlag(FragmentIntent.FLAG_NEW_TASK);
        FragmentIntent intent13 = new FragmentIntent(Fm13.class);
        getFragmentNav().startFragmentForResult(this, 1, intent11, intent13);
    }

    private void testStartSingle(){
        startFragment(new FragmentIntent(Fm02.class));
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Object data) {
        super.onFragmentResult(requestCode, resultCode, data);
        Trace.p(getClass(), "onFragmentResult:%s, %s, %s", requestCode, resultCode, data);
    }
}
