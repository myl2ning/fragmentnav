package ray.easydev.fragmentnav

import android.os.Bundle
import android.os.Parcel
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import ray.easydev.fragmentnav.fragments.*
import ray.easydev.fragmentnav.fragments.FmBase.EMPTY_RESULT

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
    fun testSingleStartAndFinish(){
        waitActionPost()
        finishKt().run {
            waitActionPost()
            checkActivityDestroyed()
        }
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

    /**
     * ```java
     * action:
     *      [task0]:Fm01, Fm02
     *      [task1]:Fm11, Fm12 -->remove
     * expect:
     *      [task0]:Fm01, Fm02
     *```
     */
    @Test
    fun testFinishCurrentTask() {
        startFragmentKt<Fm12> (
                FragmentIntent(Fm02::class.java),
                FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK),
                FragmentIntent(Fm12::class.java)
        ).apply { waitActionPost() }

        finishTaskKt()
        waitActionPost()
        checkState(1, Fm02::class.java)
    }

    /**
     *```java
     * action:
     *      [task0]:Fm01, Fm02 -->remove
     *      [task1]:Fm11, Fm12
     * expect:
     *      [task1]:Fm11, Fm12
     *```
     *
     */
    @Test
    fun testFinishNotCurrentTask(){
        startFragmentKt<Fm12>(
                FragmentIntent(Fm02::class.java),
                FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK),
                FragmentIntent(Fm12::class.java)
        ).apply { waitActionPost() }.run {
            finishTasksKt(0)
            waitActionPost()
            current<Fm12>()
        }.run xx@{
            checkState(1, Fm12::class.java, 2)
            fragmentNav.taskIds().run {
                assertTrue(size == 1)
                assertTrue(get(0) == 1 && fragmentNav.getTaskId(this@xx) == get(0))
            }
        }
    }


    /**
     *```java
     * action:
     *      [task0]:Fm01, Fm02 -->remove
     *      [task1]:Fm11, Fm12 -->remove
     *      [task2]:Fm21, Fm22
     * expect:
     *      [task2]:Fm21, Fm22
     *
     * action1:
     *      start Fm31 in new task
     * expect:
     *      [task2]: Fm21, Fm22
     *      [task3]: Fm31
     *```
     *
     */
    @Test
    fun testFinishNotCurrentTasksThenStartNewTask(){
        startFragmentKt<Fm22>(
                FragmentIntent(Fm02::class.java),
                FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK),
                FragmentIntent(Fm12::class.java),
                FragmentIntent(Fm21::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK),
                FragmentIntent(Fm22::class.java)
        ).apply { waitActionPost() }.run {
            finishTasksKt(0, 1)
            waitActionPost()
            current<Fm22>()
        }.run check@{
            checkState(1, Fm22::class.java, 2)
            fragmentNav.taskIds().run {
                assertTrue(size == 1)
                assertTrue(get(0) == 2 && fragmentNav.getTaskId(this@check) == get(0))
                get(0)
            }
        }.run {
            startFragmentKt<Fm31>(FragmentIntent(Fm31::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK))
            waitActionPost()
            assertTrue(fragmentNav.getTaskId(current<Fm31>()) == (this + 1))
        }
    }

    /**
     *```java
     * action:
     *      [task0]:Fm01, Fm02 -->remove
     *      [task1]:Fm11, Fm12
     *      [task2]:Fm21, Fm22 -->remove
     * expect:
     *      [task1]:Fm11, Fm12
     *```
     *
     */
    @Test
    fun testFinishTasksWithCurrentTask(){
        startFragmentKt<Fm22>(
                FragmentIntent(Fm02::class.java),
                FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK),
                FragmentIntent(Fm21::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK),
                FragmentIntent(Fm22::class.java)
        ).apply { waitActionPost() }.run {
            finishTasksKt(0, 2)
            waitActionPost()
            current<Fm11>()
        }.run check@{
            checkState(1, Fm11::class.java, 1)
            fragmentNav.taskIds().run {
                assertTrue(size == 1)
                assertTrue(get(0) == 1 && fragmentNav.getTaskId(this@check) == get(0))
                get(0)
            }
        }
    }


    /**
     *```java
     * action:
     *      [task0]:Fm01, Fm02 -->remove
     *      [task1]:Fm11, Fm12 -->remove
     *      [task2]:Fm21, Fm22 -->remove
     * expect:
     *      activity finished
     *```
     *
     */
    @Test
    fun testFinishAllTasks(){
        startFragmentKt<Fm22>(
                FragmentIntent(Fm02::class.java),
                FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK),
                FragmentIntent(Fm21::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK),
                FragmentIntent(Fm22::class.java)
        ).apply { waitActionPost() }.run {
            finishTasksKt(0, 1, 2)
            waitActionPost()
            null
        }.run check@{
            checkActivityDestroyed()
        }
    }

    private fun checkActivityDestroyed(){
        assertTrue(activity.isDestroyed)
        assertTrue(fragmentNav.currentFragment == null)
        sysFragments.fragments.filter { it.isAdded }.run {
            assertTrue(isEmpty())
        }
    }

    /**
     *```java
     * action:
     *      [task0]:Fm01
     *      [task1]:Fm12 -->bring to front
     *      [task2]:Fm21, Fm22
     * expect:
     *      [task0]:Fm01
     *      [task2]:Fm21, Fm22, Fm12
     *```
     *
     */
    @Test
    fun testBringNotCurrentToFront() {
        val fm = startFragmentKt<Fm12>(FragmentIntent(Fm12::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK)).apply { waitActionPost() }

        startFragmentKt<Fm22>(
                FragmentIntent(Fm21::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK),
                FragmentIntent(Fm22::class.java)
        ).apply {
            waitActionPost()
        }.run {
            synchronized(FnTest::class.java){
                activity.runOnUiThread {
                    //BringToFront action will update the views' order, so must run on ui thread
                    startFragmentKt<Fm12>(FragmentIntent(Fm12::class.java).addFlag(FragmentIntent.FLAG_BRING_TO_FRONT))
                }
            }
            waitActionPost()
            current<Fm12>()
        }.run {
            waitActionPost()
            assertTrue(fragmentNav.taskIds().filter { it == 1 }.isEmpty())
            Assert.assertTrue(fm == this)
            checkTopView(view!!)
            checkState(2, Fm12::class.java)
        }
    }

    /**
     *```java
     * action:
     *      [task0]:Fm01, Fm02 -->bring to front, Fm03
     * expect:
     *      [task0]:Fm01, Fm03, Fm02
     *```
     *
     */
    @Test
    fun testBringNotCurrentToFrontInSameTask(){
        val fm = startFragmentKt<Fm02>(FragmentIntent(Fm02::class.java)).apply { waitActionPost() }

        startFragmentKt<Fm03>(
                FragmentIntent(Fm03::class.java)
        ).apply {
            waitActionPost()
        }.run {
            synchronized(FnTest::class.java){
                activity.runOnUiThread {
                    //BringToFront action will update the views' order, so must run on ui thread
                    startFragmentKt<Fm02>(FragmentIntent(Fm02::class.java).addFlag(FragmentIntent.FLAG_BRING_TO_FRONT))
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

    /**
     *```java
     * action:
     *      [task0]:Fm01, Fm02 -->bring to front in new task
     *      [task1]:Fm11, Fm12
     * expect:
     *      [task0]:Fm01
     *      [task1]:Fm11, Fm12
     *      [task2]:Fm02
     *```
     *
     */
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
                            .addFlag(FragmentIntent.FLAG_BRING_TO_FRONT or FragmentIntent.FLAG_NEW_TASK))
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
                    startFragmentKt<Fm12>(FragmentIntent(Fm12::class.java).addFlag(FragmentIntent.FLAG_BRING_TO_FRONT))
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
        assertTrue(fragmentNav.findFragment<Fm02>("1234") === null)
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

    @Test
    fun testStartFragmentsForResultInSameTask(){
        val result = createFragmentResult()
        startFragmentForResultKt<Fm11>(
                result.requestCode,
                FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK)
        ).run {
            waitActionPost()
        }

        startFragmentKt<Fm12>(
                FragmentIntent(Fm12::class.java)
        ).run {
            waitActionPost()
            setResult(result.resultCode, result.result)
            finishTask()
            waitActionPost()
            current<Fm01>()
        }.run {
                    assertTrue(result == fragmentResult)
                    checkState(1, Fm01::class.java)
                }
    }

    @Test
    fun testStartFragmentsForResultInDifferentTasks(){
        val result = createFragmentResult()
        startFragmentForResultKt<Fm11>(
                result.requestCode,
                FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK)
        ).run {
            waitActionPost()
        }

        startFragmentKt<Fm31>(
                FragmentIntent(Fm12::class.java),
                FragmentIntent(Fm21::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK),
                FragmentIntent(Fm22::class.java),
                FragmentIntent(Fm31::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK)
        ).run {
            waitActionPost()
            setResult(result.resultCode, result.result)
            finishTasksKt(1, 2, 3)
            waitActionPost()
            current<Fm01>()
        }.run {
                    assertTrue(result == fragmentResult)
                    checkState(1, Fm01::class.java)
                }
    }

    @Test
    fun testStartMultipleFragmentsForResult(){
        val result = createFragmentResult()
        startFragmentForResultKt<Fm32>(
                result.requestCode,
                FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK),
                FragmentIntent(Fm12::class.java),
                FragmentIntent(Fm21::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK),
                FragmentIntent(Fm22::class.java),
                FragmentIntent(Fm31::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK),
                FragmentIntent(Fm32::class.java)
        ).run {
            waitActionPost()
            setResult(result.resultCode, result.result)
            finish()
            waitActionPost()
            current<Fm31>().apply {
                Assert.assertTrue(fragmentResult == EMPTY_RESULT)
            }
        }.run {
            setResult(result.resultCode, result.result)
            finishTasksKt(1, 2, 3)
            waitActionPost()
            current<Fm01>()
        }.run {
                    assertTrue(result == fragmentResult)
                    checkState(1, Fm01::class.java)
                }
    }

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

    @Test
    fun testCommitPendingCalls(){
        startFragmentKt<Fm13>(
                FragmentIntent(Fm02::class.java),
                FragmentIntent(Fm11::class.java).addFlag(FragmentIntent.FLAG_NEW_TASK),
                FragmentIntent(Fm12::class.java),
                FragmentIntent(Fm13::class.java)

        ).apply {
            waitActionPost()
        }.run {
            (fragmentNav as FragmentNavImpl)
                    .apply {
                        //Test finishTask
                        mIsRestoring = true
                        finishTaskKt()
                        waitActionPost()
                        //Upon actions should not be commit
                        assertTrue(sysFragments.fragments.size == 5)
                        checkState(2, Fm13::class.java)

                        onActivityResumed()
                        waitActionPost()
                        assertTrue(sysFragments.fragments.size == 2)
                        checkState(1, Fm02::class.java)
                    }
        }.run {
            //Test finish
            mIsRestoring = true
            finishKt()
            waitActionPost()
            assertTrue(sysFragments.fragments.size == 2)
            checkState(1, Fm02::class.java)

            onActivityResumed()
            waitActionPost()
            checkState(1, Fm01::class.java)
        }


    }

    @Test
    fun testSaveAndRestoreOp(){
        val parcel = Parcel.obtain()
        Op().apply {
            op = Op.OP_ADD
            setAnim(123, 456)
            fragmentId = "abcxyz"
            writeToParcel(parcel, describeContents())
        }.run {
            val source = this
            parcel.setDataPosition(0)
            Op.CREATOR.createFromParcel(parcel).run {
                assertTrue(source.enterAnim == enterAnim)
                assertTrue(source.exitAnim == exitAnim)
                assertTrue(source.fragmentId == fragmentId)
                assertTrue(source.op == op)
            }
        }
    }

    //Test request code info save and restore
    @Test
    fun testRequestCodeInfoSR(){
        //Test parcel write and read
        val parcel = Parcel.obtain()
        FragmentNavImpl.RequestCodeInfo("invokerId", 123).apply {
            writeToParcel(parcel, describeContents())
            parcel.setDataPosition(0)
        }.run {
            assertTrue(FragmentNavImpl.RequestCodeInfo.CREATOR.createFromParcel(parcel) == this)
        }

        //Test bundle write and read
        val bundle = Bundle()
        FragmentNavImpl.RequestCodeInfo("invokerId", 123).apply {
            writeTo(bundle)
        }.run {
            assertTrue(FragmentNavImpl.RequestCodeInfo.readFrom(bundle) == this)
        }
    }

    @Test
    fun main(){

    }

}

fun waitActionPost() {
    Thread.sleep(800);
}