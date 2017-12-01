package ray.easydev.fragmentnav.sample;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ray.easydev.fragmentnav.FnActivity;
import ray.easydev.fragmentnav.FragmentNav;

/**
 * Created by Ray on 2017/12/1.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class BaseFmTest {
    final static Class TAG = BaseFmTest.class;

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    private FragmentActivity mActivity;
    private FragmentManager fragmentManager;
    private Context mContext;

    FnFragments fnFragments;
    SysFragments sysFragments;

    @Before
    public void setup(){
        mContext = InstrumentationRegistry.getTargetContext();
        mActivity = mActivityRule.getActivity();
        fragmentManager = mActivity.getSupportFragmentManager();

        fnFragments = new FnFragments(((FnActivity) mActivity).getFragmentNav());
        sysFragments = new SysFragments(fragmentManager);
    }

    public FragmentActivity getActivity(){
        return mActivity;
    }

    public Context getContext(){
        return mContext;
    }

    public FnFragments getFnFragments() {
        return fnFragments;
    }

    public SysFragments getSysFragments() {
        return sysFragments;
    }

    public static String message(String format, Object... args){
        return String.format(format, args);
    }

    static class FnFragments {
        FragmentNav fragmentNav;

        FnFragments(FragmentNav fragmentNav){
            this.fragmentNav = fragmentNav;
        }

        Fragment getCurrentFragment(){
            return fragmentNav.getCurrentFragment();
        }

        List<Fragment> getFragments(){
            List<Fragment> fragments = new ArrayList<>();
            Collection<Integer> ids = fragmentNav.taskIds();
            for (Integer id : ids) {
                fragments.addAll(fragmentNav.getFragments(id));
            }

            return fragments;
        }
    }

    static class SysFragments {
        FragmentManager fragmentManager;

        SysFragments(FragmentManager fragmentManager){
            this.fragmentManager = fragmentManager;
        }

        Fragment getCurrentFragment(){
            List<Fragment> fragments = getFragments();
            Assert.assertTrue(message("Sys fragments' size is 0"), fragments.size() > 0);
            return fragments.get(fragments.size() - 1);
        }

        List<Fragment> getFragments(){
            try {
                Field field = fragmentManager.getClass().getDeclaredField("mAdded");
                field.setAccessible(true);
                return (List<Fragment>) field.get(fragmentManager);
            } catch (Exception e){

            }

            return new ArrayList<>(0);
        }
    }


}
