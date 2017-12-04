package ray.easydev.fragmentnav

import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import ray.easydev.fragmentnav.fragments.Fm02
import ray.easydev.fragmentnav.fragments.Fm11
import ray.easydev.fragmentnav.fragments.Fm12
import ray.easydev.fragmentnav.fragments.Fm21

/**
 * Created by Ray on 2017/12/4.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class FnTest : BaseFmTest() {

    @Test
    fun testSingleStart() {
        startFragment(FragmentIntent(Fm11::class.java))
        waitActionPost()

        checkState(1, Fm11::class.java)
    }

    @Test
    fun testBatchStart() {
        val intent02 = FragmentIntent(Fm02::class.java)
        val intent11 = FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK)
        val intent12 = FragmentIntent(Fm12::class.java)
        val intent21 = FragmentIntent(Fm21::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK)
        startFragment(intent02, intent11, intent12, intent21)
        waitActionPost()

        checkState(3, Fm21::class.java)
    }

    @Test
    fun testFinish() {
        val intent02 = FragmentIntent(Fm02::class.java)
        val intent11 = FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK)
        val intent12 = FragmentIntent(Fm12::class.java)
        startFragment(intent02, intent11, intent12)
        waitActionPost()
        checkState(2, Fm12::class.java)

        finish()
        waitActionPost()
        checkState(2, Fm11::class.java)

        finish()
        waitActionPost()
        checkState(1, Fm02::class.java)
    }

    @Test
    fun testFinishTask() {
        val intent02 = FragmentIntent(Fm02::class.java)
        val intent11 = FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK)
        val intent12 = FragmentIntent(Fm12::class.java)
        startFragment(intent02, intent11, intent12)
        waitActionPost()

        finishTask()
        waitActionPost()
        checkState(1, Fm02::class.java)
    }

    @Test
    fun testBringNotCurrentToFront() {
        val fm = startFragment(FragmentIntent(Fm02::class.java))
        waitActionPost()

        val intent11 = FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK)
        val intent12 = FragmentIntent(Fm12::class.java)
        startFragment(intent11, intent12)
        waitActionPost()

        var fm1 : FnFragment? = null
        synchronized(FnTest::class.java){
            activity.runOnUiThread {
                //Bring to front will update the views' order, so must run on ui thread
                fm1 = startFragment(FragmentIntent(Fm02::class.java).addFlag(FragmentIntent.FLAG_BROUGHT_TO_FRONT))
            }
        }

        waitActionPost()
        checkTopView(fm.view!!)
        checkState(2, Fm02::class.java)
        Assert.assertTrue(fm == fm1)
    }

    @Test
    fun testBringNotCurrentToFrontInNewTask() {
        val fm = startFragment(FragmentIntent(Fm02::class.java))
        waitActionPost()

        val intent11 = FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK)
        val intent12 = FragmentIntent(Fm12::class.java)
        startFragment(intent11, intent12)
        waitActionPost()

        var fm1 : FnFragment? = null
        synchronized(FnTest::class.java){
            activity.runOnUiThread {
                //Bring to front will update the views' order, so must run on ui thread
                fm1 = startFragment(FragmentIntent(Fm02::class.java).addFlag(FragmentIntent.FLAG_BROUGHT_TO_FRONT or FragmentIntent.FLAG_NEW_TASK))
            }
        }

        waitActionPost()

        val topTask = fnFragments.topTask
        checkTopView(fm.view!!)
        Assert.assertTrue(message("top task size:%s", topTask.size),topTask.size == 1 && topTask[0] == fm)
        Assert.assertTrue(fm == fm1)
        checkState(3, Fm02::class.java)
    }

    @Test
    fun testBringCurrentToFront() {
        val fm = startFragment(FragmentIntent(Fm02::class.java))
        waitActionPost()

        val intent11 = FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK)
        val intent12 = FragmentIntent(Fm12::class.java)
        startFragment(intent11, intent12)
        waitActionPost()

        var fm1 : FnFragment? = null
        synchronized(FnTest::class.java){
            activity.runOnUiThread {
                //Bring to front will update the views' order, so must run on ui thread
                fm1 = startFragment(FragmentIntent(Fm12::class.java).addFlag(FragmentIntent.FLAG_BROUGHT_TO_FRONT))
            }
        }

        waitActionPost()
        checkState(2, Fm12::class.java)
    }

}

fun waitActionPost() {
    Thread.sleep(400);
}