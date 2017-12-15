package ray.easydev.fragmentnav.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import ray.easydev.fragmentnav.FnActivity;
import ray.easydev.fragmentnav.FnFragmentActivity;
import ray.easydev.fragmentnav.FragmentIntent;
import ray.easydev.fragmentnav.sample.fragments.FmEnter;
import ray.easydev.fragmentnav.utils.Trace;


/**
 * Created by Ray on 2017/11/21.
 */

public class MainActivity extends FnFragmentActivity implements FnActivity {
    static {
        Trace.setLogLevel(true, 10);
        FragmentIntent.getDefault().setAnim(R.anim.page_in, R.anim.page_out, R.anim.page_show, R.anim.page_hide);
    }

    public ViewGroup rootView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Trace.p(getClass(), "Has restore data:%s", (savedInstanceState != null));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rootView = (ViewGroup) findViewById(R.id.layout_main);
    }

    public void showViewOrder(){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < rootView.getChildCount(); i ++){
            sb.append(rootView.getChildAt(i).getTag()).append(", ");
        }

        Trace.p(getClass(), sb.toString());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("Hello", "World");
    }

    @Override
    public int getFragmentContainerId() {
        return R.id.layout_main;
    }

    @NonNull
    @Override
    public FragmentIntent[] getStartIntents() {
        return new FragmentIntent[]{
                new FragmentIntent(FmEnter.class).addFlag(FragmentIntent.FLAG_NO_ENTER_ANIMATION)
        };
    }
}
