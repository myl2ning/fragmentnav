package ray.easydev.fragmentnav.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ray.easydev.fragmentnav.FnActivity;
import ray.easydev.fragmentnav.FragmentIntent;
import ray.easydev.fragmentnav.FragmentNav;
import ray.easydev.fragmentnav.FragmentNavHelper;
import ray.easydev.fragmentnav.sample.fragments.Fm02;
import ray.easydev.fragmentnav.utils.Trace;

/**
 * Created by Ray on 2017/12/15.
 */

public class MainActivity1 extends FragmentActivity implements FnActivity {
    static {
        Trace.setLogLevel(true, 10);
    }

    FragmentNav fragmentNav;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Trace.p(getClass(), "*** Has restore data:%s", (savedInstanceState != null));

        fragmentNav = FragmentNavHelper.createBeforeSuperOnCreate(this, R.id.layout_main, savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        getSupportFragmentManager().beginTransaction().add(R.id.layout_main, new MyFm()).commit();

        if(savedInstanceState == null){
            fragmentNav.startFragment(null, new FragmentIntent(Fm02.class));
        }
    }

    @NonNull
    @Override
    public FragmentNav getFragmentNav() {
        return fragmentNav;
    }


    public static class MyFm extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fm_enter, container, false);
        }
    }
}

