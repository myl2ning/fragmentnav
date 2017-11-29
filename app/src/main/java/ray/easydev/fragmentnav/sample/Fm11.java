package ray.easydev.fragmentnav.sample;

import android.view.View;

import ray.easydev.fragmentnav.FragmentIntent;


/**
 * Created by Ray on 2017/11/21.
 */

public class Fm11 extends FmBase {
    @Override
    public void onClick(View v) {
        super.onClick(v);
        startFragment(new FragmentIntent(Fm12.class));
    }
}
