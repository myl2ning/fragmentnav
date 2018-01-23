package ray.easydev.fragmentnav.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import ray.easydev.fragmentnav.FnFragmentActivity;
import ray.easydev.fragmentnav.FragmentIntent;
import ray.easydev.fragmentnav.sample.fragments.FmEnter;
import ray.easydev.fragmentnav.log.Log;


/**
 * Created by Ray on 2017/11/21.
 */

public class MainActivity extends FnFragmentActivity {
    static {
        Log.setLogLevel(true, 10);
        //Configure the default fragment animations
        FragmentIntent.getDefault().setAnim(
                R.anim.page_start, //animation for start a fragment
                R.anim.page_finish, //animation for finish a fragment
                R.anim.page_show, //animation for show the previous fragment when finish a fragment
                R.anim.page_hide //animation for hide the fragment when start a new fragment
        );
    }

    public ViewGroup rootView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rootView = (ViewGroup) findViewById(R.id.fragment_container);
    }

    public void showViewOrder(){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < rootView.getChildCount(); i ++){
            sb.append(rootView.getChildAt(i).getTag()).append(", ");
        }

        Log.p(getClass(), sb.toString());
    }

    @Override
    public int getFragmentContainerId() {
        return R.id.fragment_container;
    }

    @NonNull
    @Override
    public FragmentIntent[] getStartIntents() {
        return new FragmentIntent[]{
                new FragmentIntent(FmEnter.class, getIntent().getExtras())
                        //Disable the start animation to avoid the conflict with the activity's start animation
                        .addFlag(FragmentIntent.FLAG_NO_START_ANIMATION)
        };
    }
}
