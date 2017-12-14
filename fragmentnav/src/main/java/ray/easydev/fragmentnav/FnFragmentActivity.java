package ray.easydev.fragmentnav;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Ray on 2017/12/14.
 */

public abstract class FnFragmentActivity extends FragmentActivity implements FnActivity {
    private FragmentNav mFragmentNav;
    private Bundle mSavedInstanceState;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mFragmentNav = FragmentNavHelper.createBeforeSuperOnCreate(this, getFragmentContainerId(), savedInstanceState);
        mSavedInstanceState = savedInstanceState;
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        launchFragments();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        launchFragments();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        launchFragments();
    }

    private void launchFragments(){
        if(mSavedInstanceState == null){
            mFragmentNav.startFragment(null, getStartIntents());
        }

        mSavedInstanceState = null;
    }

    @Override
    public void onBackPressed() {
        FragmentNavHelper.onBackPressed(getFragmentNav());
    }

    @NonNull
    @Override
    public FragmentNav getFragmentNav() {
        return mFragmentNav;
    }

    public abstract int getFragmentContainerId();

    public @NonNull abstract FragmentIntent[] getStartIntents();
}
