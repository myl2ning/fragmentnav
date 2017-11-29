package ray.easydev.fragmentnav.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import ray.easydev.fragmentnav.utils.Trace;


/**
 * Created by Ray on 2017/11/21.
 */

public class Fm12 extends FmBase {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            Trace.p(getClass(), "Do finish task onCreate");
//            finishTask();
//            startFragment(new FragmentIntent(Fm21.class));
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        setFinishAnim(NO_ANIM).finishTask();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
