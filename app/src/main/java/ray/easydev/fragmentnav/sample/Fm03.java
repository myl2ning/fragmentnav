package ray.easydev.fragmentnav.sample;

import android.support.v4.app.FragmentManager;
import android.view.View;

import java.lang.reflect.Field;
import java.util.List;

import ray.easydev.fragmentnav.FragmentIntent;
import ray.easydev.fragmentnav.utils.Trace;

/**
 * Created by Ray on 2017/11/21.
 */

public class Fm03 extends FmBase {
    @Override
    public void onClick(View v) {
        super.onClick(v);
//        ViewGroup vg = (ViewGroup) getView().getParent();
//        final View view0 = vg.getChildAt(0);
//        View view1 = vg.getChildAt(1);
//
//        Trace.p(getClass(), view1 == getView());
//        vg.removeView(view0);
//        vg.addView(view0);
//
//        view0.setVisibility(View.VISIBLE);
//        view0.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.page_in));

//        vg.postDelayed(new Runnable() {
//            @Override
//            public void run() {
////                startFragment(new FragmentIntent(Fm01.class).addFlag(FragmentIntent.FLAG_BROUGHT_TO_FRONT));
//                view0.setVisibility(View.VISIBLE);
//                view0.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.page_in));
//            }
//        }, 500);

        startFragment(new FragmentIntent(Fm01.class).addFlag(FragmentIntent.FLAG_BROUGHT_TO_FRONT));
//
    }

    private void showFragmentsOrder(){
        FragmentManager fmgr = getFragmentManager();
        try{
            showList(fmgr.getFragments());

            Field list = FragmentManager.class.getField("mAdded");

        } catch (Exception e){

        }
    }

    private void showList(List<?> list){
        StringBuilder sb = new StringBuilder();
        for (Object o : list) {
             sb.append(o.getClass().getSimpleName()).append(", ");
        }

        Trace.p("ViewOrder", "fmOrder:", sb.toString());
    }
}
