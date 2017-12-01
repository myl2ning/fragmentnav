package ray.easydev.fragmentnav.sample;

import junit.framework.Assert;

import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.*;

/**
 * Created by Ray on 2017/12/1.
 */

public class FmTest extends BaseFmTest {

    @Test
    public void test(){
        Assert.assertTrue(getFnFragments().getCurrentFragment() == getSysFragments().getCurrentFragment());
        onView(withId(0));
    }
}
