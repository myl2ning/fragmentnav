package ray.easydev.fragmentnav.sample.kt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Button
import ray.easydev.fragmentnav.sample.FmBase
import ray.easydev.fragmentnav.sample.R

/**
 * Created by Ray on 2017/12/7.
 */
class FnTestAnim : FmBase() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fm_test_anim, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view?.findViewById<Button>(R.id.btn)?.setOnClickListener({
            val block1 = view.findViewById<View>(R.id.block1)
            val block2 = view.findViewById<View>(R.id.block2)

            block1.startAnimation(getAnim())
            block2.startAnimation(getAnim())

            (view as ViewGroup).removeView(block1)
        })

    }

    fun getAnim() : Animation {
        return TranslateAnimation(0f, 0f, 0f, 100f ).apply {
            duration = 3000
        }
    }
}