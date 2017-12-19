package ray.easydev.fragmentnav;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import ray.easydev.fragmentnav.fragments.Fm01;
import ray.easydev.fragmentnav.test.R;
import ray.easydev.fragmentnav.log.Log;

/**
 * Created by Ray on 2017/11/21.
 */

public class TestMainActivity extends FnFragmentActivity implements FnActivity {
    static {
        Log.setLogLevel(false, 10);
    }

    public ViewGroup rootView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rootView = (ViewGroup) findViewById(R.id.layout_main);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public int getFragmentContainerId() {
        return R.id.layout_main;
    }

    @NonNull
    @Override
    public FragmentIntent[] getStartIntents() {
        return new FragmentIntent[]{
                new FragmentIntent(Fm01.class).addFlag(FragmentIntent.FLAG_NO_START_ANIMATION)
        };
    }
}
