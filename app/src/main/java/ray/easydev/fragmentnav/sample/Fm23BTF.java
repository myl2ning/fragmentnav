package ray.easydev.fragmentnav.sample;

import android.view.View;

import ray.easydev.fragmentnav.FragmentIntent;
import ray.easydev.fragmentnav.utils.Trace;

/**
 * Created by Ray on 2017/11/30.
 */

public class Fm23BTF extends FmBase {
    @Override
    public void onClick(View v) {
        super.onClick(v);
        startFragment(new FragmentIntent(Fm01.class).addFlag(FragmentIntent.FLAG_BROUGHT_TO_FRONT));
    }

    @Override
    public void onNewIntent(FragmentIntent intent) {
        super.onNewIntent(intent);
        Trace.p(getClass(), "onNewIntent:%s", intent);
    }
}
