package ray.easydev.fragmentnav.sample

import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import junit.framework.Assert
import org.junit.Test
import org.junit.runner.RunWith
import ray.easydev.fragmentnav.BaseFmTest
import ray.easydev.fragmentnav.FragmentIntent
import ray.easydev.fragmentnav.waitActionPost

/**
 * Created by Ray on 2017/12/4.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class FmTestKt : BaseFmTest(){

    @Test
    fun testSingleStart(){
        startFragment(FragmentIntent(Fm11::class.java))
        waitForAnimEnd()

        checkState(1, Fm11::class.java)
    }

    @Test
    fun testBatchStart(){
        val intent02 = FragmentIntent(Fm02::class.java)
        val intent11 = FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK)
        val intent12 = FragmentIntent(Fm12::class.java)
        val intent21 = FragmentIntent(Fm21::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK)
        startFragment(intent02, intent11, intent12, intent21)
        waitForAnimEnd()

        checkState(3, Fm21::class.java)
    }

    @Test
    fun testFinish(){
        val intent02 = FragmentIntent(Fm02::class.java)
        val intent11 = FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK)
        val intent12 = FragmentIntent(Fm12::class.java)
        startFragment(intent02, intent11, intent12)
        waitForAnimEnd()
        checkState(2, Fm12::class.java)

        finish()
        waitForAnimEnd()
        checkState(2, Fm11::class.java)

        finish()
        waitForAnimEnd()
        checkState(1, Fm02::class.java)
    }

    @Test
    fun testFinishTask(){

    }

    @Test
    fun testBringToFront(){
        val fm = startFragment(FragmentIntent(ray.easydev.fragmentnav.fragments.Fm02::class.java))
        waitActionPost()

        val intent11 = FragmentIntent(ray.easydev.fragmentnav.fragments.Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK)
        val intent12 = FragmentIntent(ray.easydev.fragmentnav.fragments.Fm12::class.java)
        startFragment(intent11, intent12)
        waitActionPost()

        val fm1 = startFragment(FragmentIntent(ray.easydev.fragmentnav.fragments.Fm02::class.java).addFlag(FragmentIntent.FLAG_BROUGHT_TO_FRONT))
        waitActionPost()
        checkState(2, ray.easydev.fragmentnav.fragments.Fm02::class.java)
        Assert.assertTrue(fm == fm1)
    }

}

fun waitForAnimEnd(){
    Thread.sleep(300);
}