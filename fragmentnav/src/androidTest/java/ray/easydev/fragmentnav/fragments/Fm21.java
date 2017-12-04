package ray.easydev.fragmentnav.fragments;

import android.view.View;

import ray.easydev.fragmentnav.FragmentIntent;


/**
 * Created by Ray on 2017/11/21.
 */

public class Fm21 extends FmBase {

    @Override
    public void onClick(View v) {
        super.onClick(v);
        startFragment(new FragmentIntent(Fm01.class).addFlag(FragmentIntent.FLAG_BROUGHT_TO_FRONT).addFlag(FragmentIntent.FLAG_NEW_TASK));

    }
}
