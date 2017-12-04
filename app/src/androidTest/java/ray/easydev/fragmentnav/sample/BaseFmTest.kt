package ray.easydev.fragmentnav.sample

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import junit.framework.Assert
import org.junit.Before
import org.junit.Rule
import ray.easydev.fragmentnav.BaseFmTest
import ray.easydev.fragmentnav.FnActivity
import ray.easydev.fragmentnav.FnFragment
import ray.easydev.fragmentnav.FragmentIntent
import ray.easydev.fragmentnav.FragmentNav
import java.util.*

/**
 * Created by Ray on 2017/12/1.
 */

open class BaseFmTest {

    @Rule
    @JvmField
    public var mActivityRule = ActivityTestRule(
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

    @Before
    fun setup() {
        context = InstrumentationRegistry.getTargetContext()
        activity = mActivityRule.activity
        fragmentManager = activity.supportFragmentManager

        fnFragments = FnFragments((activity as FnActivity).fragmentNav)
        sysFragments = SysFragments(fragmentManager)
    }

    internal fun startFragment(vararg intents: FragmentIntent) {
        fnFragments.currentFragment.startFragment(*intents)
    }

    fun finish(){
        fnFragments.currentFragment.finish()
    }

    fun finishTask(){
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
                    fragments.addAll(fragmentNav.getFragments(id!!))
                }

                return fragments
            }
    }

    class SysFragments(var fragmentManager: FragmentManager) {

        val currentFragment: Fragment
            get() {
                val fragments = fragments
                Assert.assertTrue(message("Sys fragments' size is 0"), fragments.size > 0)
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

    internal fun checkTask(taskSize: Int) {
        val fragmentNav = fnFragments.fragmentNav

        val taskIds = fragmentNav.taskIds()
        Assert.assertTrue(fragmentNav.taskIds().size == taskSize)

        val fragments = fragmentNav.getFragments(taskIds[taskIds.size - 1])
        Assert.assertTrue(!fragments!!.isEmpty())
        Assert.assertTrue(fragments.last() === fnFragments.currentFragment && fnFragments.currentFragment === sysFragments.currentFragment)
    }

    fun checkCurrent(cls: Class<out Fragment>) {
        var visibleCount = 0;
        sysFragments.fragments.forEach {
            if(it.isVisible) visibleCount ++
        }

        Assert.assertTrue((visibleCount == 1) && (sysFragments.currentFragment::class.java == cls))
    }

    internal fun checkState(taskSize: Int, currFragment: Class<out Fragment>) {
        checkTask(taskSize)
        checkCurrent(currFragment)
        checkOrder()
    }

    companion object {
        internal val TAG: Class<*> = BaseFmTest::class.java

        fun message(format: String, vararg args: Any): String {
            return String.format(format, *args)
        }
    }

}
