package ray.easydev.fragmentnav

import android.os.Bundle
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import ray.easydev.fragmentnav.fragments.*

/**
 * Created by Ray on 2017/12/4.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class FnTest : BaseFmTest() {

    @Test
    fun testSingleStart() {
        startFragmentKt<Fm11>(FragmentIntent(Fm11::class.java))
        waitActionPost()

        checkState(1, Fm11::class.java)
    }

    @Test
    fun testBatchStart() {
        startFragmentKt<Fm21>(
                FragmentIntent(Fm02::class.java),
                FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK),
                FragmentIntent(Fm12::class.java),
                FragmentIntent(Fm21::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK)
        ).let {
            waitActionPost()
            checkState(3, Fm21::class.java)
        }

    }

    @Test
    fun testFinish() {
        startFragmentKt<Fm12>(
                FragmentIntent(Fm02::class.java),
                FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK),
                FragmentIntent(Fm12::class.java)
        ).apply {
            waitActionPost()
        }

        checkState(2, Fm12::class.java)

        finishKt()
        waitActionPost()
        checkState(2, Fm11::class.java)

        finishKt()
        waitActionPost()
        checkState(1, Fm02::class.java)
    }

    @Test
    fun testFinishTask() {
        startFragmentKt<Fm12> (
                FragmentIntent(Fm02::class.java),
                FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK),
                FragmentIntent(Fm12::class.java)
        ).apply { waitActionPost() }

        finishTaskKt()
        waitActionPost()
        checkState(1, Fm02::class.java)
    }

    @Test
    fun testBringNotCurrentToFront() {
        val fm = startFragmentKt<Fm02>(FragmentIntent(Fm02::class.java)).apply { waitActionPost() }

        startFragmentKt<Fm12>(
                FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK),
                FragmentIntent(Fm12::class.java)
        ).apply {
            waitActionPost()
        }.run {
            synchronized(FnTest::class.java){
                activity.runOnUiThread {
                    //BringToFront action will update the views' order, so must run on ui thread
                    startFragmentKt<Fm02>(FragmentIntent(Fm02::class.java).addFlag(FragmentIntent.FLAG_BROUGHT_TO_FRONT))
                }
            }
            waitActionPost()
            current<Fm02>()
        }.run {
            waitActionPost()
            Assert.assertTrue(fm == this)
            checkTopView(view!!)
            checkState(2, Fm02::class.java)
        }
    }

    @Test
    fun testBringNotCurrentInSameTaskToFront(){
        val fm = startFragmentKt<Fm02>(FragmentIntent(Fm02::class.java)).apply { waitActionPost() }

        startFragmentKt<Fm03>(
                FragmentIntent(Fm03::class.java)
        ).apply {
            waitActionPost()
        }.run {
            synchronized(FnTest::class.java){
                activity.runOnUiThread {
                    //BringToFront action will update the views' order, so must run on ui thread
                    startFragmentKt<Fm02>(FragmentIntent(Fm02::class.java).addFlag(FragmentIntent.FLAG_BROUGHT_TO_FRONT))
                }
            }
            waitActionPost()
            current<Fm02>()
        }.run {
            waitActionPost()
            Assert.assertTrue(fm == this)
            checkTopView(view!!)
            checkState(1, Fm02::class.java)
        }
    }

    @Test
    fun testBringNotCurrentToFrontInNewTask() {
        val fm = startFragmentKt<Fm02>(FragmentIntent(Fm02::class.java)).apply { waitActionPost() }

        startFragmentKt<Fm12>(
                FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK),
                FragmentIntent(Fm12::class.java)
        ).apply {
            waitActionPost()
        }.run {
            synchronized(FnTest::class.java){
                activity.runOnUiThread {
                    //BringToFront action will update the views' order, so must run on ui thread
                    startFragmentKt<Fm02>(FragmentIntent(Fm02::class.java)
                            .addFlag(FragmentIntent.FLAG_BROUGHT_TO_FRONT or FragmentIntent.FLAG_NEW_TASK))
                }
            }
            waitActionPost()
            current<Fm02>()
        }.run xx@{
            Assert.assertTrue(fm == this)
            fnFragments.topTask.run {
                Assert.assertTrue("top task size:$size, ${last()::class.java}",size == 1 && last() == this@xx)
            }
            checkTopView(view!!)
            checkState(3, Fm02::class.java)
        }
    }

    @Test
    fun testBringCurrentToFront() {
        startFragmentKt<Fm02>(FragmentIntent(Fm02::class.java)).apply { waitActionPost() }

        startFragmentKt<Fm12>(
                FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK),
                FragmentIntent(Fm12::class.java)
        ).apply {
            waitActionPost()
        }.run {
            synchronized(FnTest::class.java){
                activity.runOnUiThread {
                    //Bring to front will update the views' order, so must run on ui thread
                    startFragmentKt<Fm12>(FragmentIntent(Fm12::class.java).addFlag(FragmentIntent.FLAG_BROUGHT_TO_FRONT))
                }
            }

            waitActionPost()
            current<Fm12>()
        }.run {
            checkState(2, Fm12::class.java)
        }
    }

    @Test
    fun testFindFragmentById(){
        val fm02 = startFragmentKt<Fm02>(FragmentIntent(Fm02::class.java))
        waitActionPost()

        val intent11 = FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK)
        val intent12 = FragmentIntent(Fm12::class.java)
        startFragmentKt<Fm12>(intent11, intent12)
        waitActionPost()

        assertTrue(fragmentNav.findFragment<Fm02>(fm02.fnId) === fm02)
    }

    @Test
    fun testHasFragment(){
        startFragmentKt<Fm02>(FragmentIntent(Fm02::class.java))
        waitActionPost()

        current<FnFragment>().let {
            assertTrue(Fm02::class.java == it::class.java)
            val intent11 = FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK)
            val intent12 = FragmentIntent(Fm12::class.java)
            startFragmentKt<Fm12>(intent11, intent12)
        }
        waitActionPost()

        assertTrue(fragmentNav.hasFragment(Fm02::class.java))
        assertFalse(fragmentNav.hasFragment(Fm22::class.java))
    }

    @Test
    fun testStartFragmentForResultInSameTask(){
        val result = createFragmentResult()
        startFragmentForResultKt<Fm02>(
                result.requestCode,
                FragmentIntent(Fm02::class.java)
        ).apply {
            waitActionPost()
        }.run {
            setResult(result.resultCode, result.result)
            finishKt()
            waitActionPost()
            current<Fm01>()
        }.run {
            assertTrue(result == fragmentResult)
            checkState(1, Fm01::class.java)
        }
    }

    @Test
    fun testStartFragmentInOtherTaskForResult(){
        val result = createFragmentResult()

        startFragmentForResultKt<Fm12>(
                result.requestCode,
                FragmentIntent(Fm12::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK)
        ).run {
            waitActionPost()
            current<Fm12>()
        }.run {
            setResult(result.resultCode, result.result)
            finishTaskKt()
            waitActionPost()
            current<Fm01>()
        }.run {
            assertTrue(result == fragmentResult)
            checkState(1, Fm01::class.java)
        }
    }

//    @Test
//    fun testStartFragmentInOtherTaskForResult1(){
//        val result = createFragmentResult();
//        startFragmentForResultKt<Fm13>(
//                result.requestCode
//                , FragmentIntent(Fm02::class.java)
//                , FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK)
//                , FragmentIntent(Fm12::class.java)
//                , FragmentIntent(Fm13::class.java)
//        ).run {
//            waitActionPost()
//            //这儿setResult除非能在一个commit内回到startFragmentForResult的初始页面，否则result是不会被传递回初始页面的
//            setResult(result.resultCode, result.result)
//            finishKt()
//            waitActionPost()
//            current<Fm12>()
//        }.run { //current Fm12
//            assertTrue(fragmentResult == FmBase.EMPTY_RESULT)
//            finishTaskKt()
//            waitActionPost()
//            current<Fm02>()
//        }.run {
//            finish()
//            waitActionPost()
//            current<Fm01>()
//        }.run {
//            assertTrue(fragmentResult == FmBase.EMPTY_RESULT)
//            checkState(1, Fm01::class.java)
//        }
//    }

    private fun createFragmentResult(): FmBase.FragmentResult {
        return FmBase.FragmentResult(1, 2, "FragmentResultOK");
    }

    @Test
    fun testSaveAndRestore(){
        startFragmentKt<Fm12>(
                FragmentIntent(Fm02::class.java),
                FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK),
                FragmentIntent(Fm12::class.java)
        ).apply {
            waitActionPost()
        }

        assertTrue((fragmentNav as FragmentNavImpl).mFragmentTask
                .copy()
                .run {
                    comparesTo(fragmentNav.run {
                        val bundle = Bundle()
                        saveState(bundle)
                        (this as FragmentNavImpl).mFragmentTask.actual().clear()
                        this.mIsRestoring = true


                        restoreState(bundle)
                        this.mFragmentTask.actual()
                    })
                })

        checkState(2, Fm12::class.java)
    }

    @Test
    fun testCommitAfterSaveInstance(){
        (fragmentNav as FragmentNavImpl).run {
            mIsActivitySavedInstanceState = true
            startFragmentKt<Fm12>(
                    FragmentIntent(Fm02::class.java),
                    FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK),
                    FragmentIntent(Fm12::class.java)
            )

            onActivityResumed()
            waitActionPost()
        }

        assertTrue(sysFragments.fragments.size == 4)
        checkState(2, Fm12::class.java)
    }

}

fun waitActionPost() {
    Thread.sleep(400);
}