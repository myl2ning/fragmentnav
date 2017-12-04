package ray.easydev.fragmentnav.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import ray.easydev.fragmentnav.FragmentIntent;


/**
 * Created by Ray on 2017/11/21.
 */

public class Fm12 extends FmBase {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        startFragment(new FragmentIntent(Fm02.class).addFlag(FragmentIntent.FLAG_BROUGHT_TO_FRONT | FragmentIntent.FLAG_NEW_TASK));
    }

}
