package ray.easydev.fragmentnav.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.ViewGroup;

import ray.easydev.fragmentnav.FnActivity;
import ray.easydev.fragmentnav.FragmentIntent;
import ray.easydev.fragmentnav.FragmentNav;
import ray.easydev.fragmentnav.FragmentNavHelper;
import ray.easydev.fragmentnav.utils.Trace;


/**
 * Created by Ray on 2017/11/21.
 */

public class MainActivity extends FragmentActivity implements FnActivity {
    FragmentNav fragmentNav;

    public ViewGroup rootView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        fragmentNav = FragmentNavHelper.createBeforeSuperOnCreate(this, R.id.layout_main, savedInstanceState);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rootView = (ViewGroup) findViewById(R.id.layout_main);

        if(savedInstanceState == null){
            fragmentNav.startFragment(null, new FragmentIntent(Fm01.class).addFlag(FragmentIntent.FLAG_NO_ENTER_ANIMATION));
        }
    }


    public void printViewInfo(){
        Trace.p(getClass(), rootView.getChildCount());
    }


    @Override
    public void onBackPressed() {
        FragmentNavHelper.onBackPressed(getFragmentNav());
    }

    @NonNull
    @Override
    public FragmentNav getFragmentNav() {
        return fragmentNav;
    }
}
