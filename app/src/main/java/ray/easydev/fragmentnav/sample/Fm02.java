package ray.easydev.fragmentnav.sample;

import android.view.View;

import ray.easydev.fragmentnav.FragmentIntent;

/**
 * Created by Ray on 2017/11/21.
 */

public class Fm02 extends FmBase {
    @Override
    public void onClick(View v) {
        super.onClick(v);
        FragmentIntent intent11 = new FragmentIntent(Fm11.class).addFlag(FragmentIntent.FLAG_NEW_TASK);
        FragmentIntent intent12 = new FragmentIntent(Fm12.class);
        startFragment(intent11, intent12);
    }
}
