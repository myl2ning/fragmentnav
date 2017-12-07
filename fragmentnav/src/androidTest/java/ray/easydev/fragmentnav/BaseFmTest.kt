package ray.easydev.fragmentnav

import android.app.Instrumentation
import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.view.View
import android.view.ViewGroup
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import ray.easydev.fragmentnav.test.R
import java.lang.StringBuilder
import java.util.*

/**
 * Created by Ray on 2017/12/1.
 */

open class BaseFmTest {

    @Rule
    @JvmField
    var mActivityRule = ActivityTestRule(
            MainActivity::class.java)

    lateinit var activity: FragmentActivity
        private set

    lateinit var context: Context
        private set

    lateinit var fragmentManager: FragmentManager
    lateinit var fnFragments: FnFragments
        internal set
    lateinit var sysFragments: SysFragments
        internal set

    lateinit var fragmentNav: FragmentNav

    lateinit var instrumentation : Instrumentation

    @Before
    fun setup() {
        context = InstrumentationRegistry.getTargetContext()
        instrumentation = InstrumentationRegistry.getInstrumentation()
        activity = mActivityRule.activity
        fragmentManager = activity.supportFragmentManager

        fragmentNav = ((activity as FnActivity).fragmentNav)
        fnFragments = FnFragments(fragmentNav)
        sysFragments = SysFragments(fragmentManager)
    }

    fun<T : FnFragment> current() : T {
        return fragmentNav.currentFragment as T
    }

    fun<T : FnFragment> startFragmentForResultKt(reqCode: Int, intent: FragmentIntent): T {
        return fnFragments.currentFragment.startFragmentForResult(reqCode, intent) as T
    }

    fun<T : FnFragment> startFragmentKt(vararg intents: FragmentIntent): T {
        return fnFragments.currentFragment.startFragment(*intents) as T
    }

    fun finishKt(){
        fnFragments.currentFragment.finish()
    }

    fun finishTaskKt(){
        fnFragments.currentFragment.finishTask()
    }

    class FnFragments(var fragmentNav: FragmentNav) {

        val currentFragment: FnFragment
            get() = fragmentNav.currentFragment

        val fragments: List<Fragment>
            get() {
                val fragments = ArrayList<Fragment>()
                val ids = fragmentNav.taskIds()
                for (id in ids) {
                    fragments.addAll(fragmentNav.getFragments(id))
                }

                return fragments
            }

        val topTask: List<Fragment>
            get() {
                val ids = fragmentNav.taskIds()
                return fragmentNav.getFragments(ids.last())
            }
    }

    class SysFragments(var fragmentManager: FragmentManager) {

        val currentFragment: Fragment
            get() {
                val fragments = fragments
                Assert.assertTrue(message("Sys fragments' size is 0"), fragments.isNotEmpty())
                return fragments[fragments.size - 1]
            }

        val fragments: List<Fragment>
            get() = fragmentManager.fragments
    }

    internal fun checkOrder() {
        if (fnFragments.currentFragment === sysFragments.currentFragment) {
            val fragments0 = fnFragments.fragments
            val fragments1 = sysFragments.fragments

            if (fragments0.size == fragments1.size) {
                for (i in fragments0.indices) {
                    val fragment0 = fragments0[i]
                    val fragment1 = fragments1[i]

                    if (fragment0 != fragment1) {
                        Assert.fail()
                    }
                }

                return;
            }
        }

        return Assert.fail( "Current fragment ")
    }

    fun checkTask(taskSize: Int) {
        val fragmentNav = fnFragments.fragmentNav

        val taskIds = fragmentNav.taskIds()
        Assert.assertTrue(fragmentNav.taskIds().size == taskSize)

        val fragments = fragmentNav.getFragments(taskIds[taskIds.size - 1])
        Assert.assertTrue(!fragments.isEmpty())
        Assert.assertTrue(fragments.last() === fnFragments.currentFragment && fnFragments.currentFragment === sysFragments.currentFragment)
    }

    fun checkCurrent(cls: Class<out Fragment>) {
        var visibleCount = 0;
        val sb = StringBuilder()
        sysFragments.fragments.forEach {
            if(!it.isHidden) {
                visibleCount ++
                sb.append(it.javaClass.simpleName).append(" ")
            }
        }

        Assert.assertTrue("VisibleCount:$visibleCount  $sb", (visibleCount == 1))
        Assert.assertTrue((sysFragments.currentFragment::class.java == cls))
    }

    fun checkState(taskSize: Int, currFragment: Class<out Fragment>) {
        checkTask(taskSize)
        checkCurrent(currFragment)
        checkOrder()
    }

    fun checkTopView(view: View){
        val viewGroup = activity.findViewById<ViewGroup>(R.id.layout_main);
        Assert.assertTrue(view == viewGroup.getChildAt(viewGroup.childCount - 1))
    }

    companion object {
        internal val TAG: Class<*> = BaseFmTest::class.java

        fun message(format: String, vararg args: Any): String {
            return String.format(format, *args)
        }
    }

}
