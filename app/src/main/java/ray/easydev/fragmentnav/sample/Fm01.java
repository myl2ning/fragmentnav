package ray.easydev.fragmentnav.sample;

import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import ray.easydev.fragmentnav.FragmentIntent;
import ray.easydev.fragmentnav.sample.utils.Utils;
import ray.easydev.fragmentnav.utils.Trace;


/**
 * Created by Ray on 2017/11/21.
 */

public class Fm01 extends FmBase {

    @Override
    public void onNewIntent(FragmentIntent intent) {
        super.onNewIntent(intent);
        getView().postDelayed(new Runnable() {
            @Override
            public void run() {
                Trace.p(getClass(), "onNewIntent:%s", Utils.joinCollections(getFragmentManager().getFragments(), ", "));
                printAdded();
                printActive();
            }
        }, 500);
    }


    private void printAdded(){
        try {
            Field field = getFragmentManager().getClass().getDeclaredField("mAdded");
            field.setAccessible(true);
            List<Fragment> fragments = (List<Fragment>) field.get(getFragmentManager());
            Trace.p("mAdded", Utils.joinCollections(fragments, ", "));

        } catch (Exception e){

        }
    }

    private void printActive(){
        try {
            Field field = getFragmentManager().getClass().getDeclaredField("mActive");
            field.setAccessible(true);
            SparseArray<Fragment>  active = (SparseArray<Fragment>) field.get(getFragmentManager());
            System.out.println();


            Method method = getFragmentManager().getClass().getDeclaredMethod("getActiveFragments");
            method.setAccessible(true);
            List<Fragment> fragments = (List<Fragment>) method.invoke(getFragmentManager());
            Trace.p("mActive", Utils.joinCollections(fragments, ", "));
        } catch (Exception e){

        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        startFragment(new FragmentIntent(Fm02.class));
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

    private void testBatchStart1(){
        FragmentIntent intent02 = new FragmentIntent(Fm02.class);
        FragmentIntent intent11 = new FragmentIntent(Fm11.class).addFlag(FragmentIntent.FLAG_NEW_TASK);
        FragmentIntent intent12 = new FragmentIntent(Fm12.class);
        FragmentIntent intent21 = new FragmentIntent(Fm21.class).addFlag(FragmentIntent.FLAG_NEW_TASK);

        getFragmentNav().startFragment(this, intent11, intent12, intent21);
    }

    private void testBringToFront(){
        FragmentIntent intent02 = new FragmentIntent(Fm02.class);
        FragmentIntent intent21 = new FragmentIntent(Fm21.class).addFlag(FragmentIntent.FLAG_NEW_TASK);
        FragmentIntent intent23 = new FragmentIntent(Fm23BTF.class);
        getFragmentNav().startFragment(this, intent02, intent21, intent23);
    }
    @Override
    public void onFragmentResult(int requestCode, int resultCode, Object data) {
        super.onFragmentResult(requestCode, resultCode, data);
        Trace.p(getClass(), "onFragmentResult:%s, %s, %s", requestCode, resultCode, data);
    }

}


