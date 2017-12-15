package ray.easydev.fragmentnav

import android.os.Parcel
import android.os.Parcelable
import org.junit.Assert.assertTrue
import org.junit.Test
import ray.easydev.fragmentnav.fragments.Fm01
import java.io.Serializable

/**
 * Created by Ray on 2017/12/12.
 */
class FragmentIntentTest : BaseFmTest() {

    @Test
    fun testSaveAndRestore(){
        val psExtra = PSVo().apply {
            string = "HelloParcelable"
            int = 123456
        }

        val parcel = Parcel.obtain()

        FragmentIntent(Fm01::class.java).run {
            flags = FragmentIntent.FLAG_NEW_TASK
            addFlag(FragmentIntent.FLAG_BROUGHT_TO_FRONT)
            tag = "This is tag"
            invokerId = "invokerId"
            setAnim(123, 124, 125, 126)

            putExtra("IntExtra", 1)
            putExtra("LongExtra", 1024L)
            putExtra("DoubleExtra", 1024.1024)
            putExtra("FloatExtra", 1024.1025f)
            putExtra("StringExtra", "HelloFn")
            putExtra("ParcelExtra", psExtra as Parcelable)
            putExtra("SerializeExtra", psExtra as Serializable)

            writeToParcel(parcel, describeContents())
            extras
        }.run xx@{
            parcel.setDataPosition(0)
            FragmentIntent.CREATOR.createFromParcel(parcel).run {
                //because extras == fragment.argument and argument will be saved by fragment, so extras will not be saved into bundle
                extras = this@xx
                assertTrue(flags == (FragmentIntent.FLAG_NEW_TASK or FragmentIntent.FLAG_BROUGHT_TO_FRONT))
                assertTrue("This is tag" == tag)
                assertTrue("invokerId" == invokerId)
                assertTrue(inAnim == 123)
                assertTrue(outAnim == 124)
                assertTrue(showAnim == 125)
                assertTrue(hideAnim == 126)
                assertTrue(targetCls == Fm01::class.java)

                extras.run {
                    assertTrue(getInt("IntExtra") == 1)
                    assertTrue(getLong("LongExtra") == 1024L)
                    assertTrue(getDouble("DoubleExtra") == 1024.1024)
                    assertTrue(getFloat("FloatExtra") == 1024.1025f)
                    assertTrue(getString("StringExtra") == "HelloFn")
                    assertTrue(getParcelable<PSVo>("ParcelExtra") == psExtra)
                    assertTrue(getSerializable("SerializeExtra") == psExtra)
                }

            }
        }



    }

    @Test
    fun testSetDefault(){
        FragmentIntent.getDefault().apply {
            setAnim(123, 124, 125, 126)
            flags = (FragmentIntent.FLAG_NEW_TASK or FragmentIntent.FLAG_BROUGHT_TO_FRONT)
            putExtra("StringExtra", "HelloFn")
            putExtra("LongExtra", 1024L)
        }.run {
            val src = this
            FragmentIntent().run {
                addFlag(FragmentIntent.FLAG_NO_ANIMATION)
                putExtra("IntExtra", 1)

                assertTrue(inAnim == src.inAnim && outAnim == src.outAnim && showAnim == src.showAnim && hideAnim == hideAnim)
                assertTrue(flags == (src.flags or FragmentIntent.FLAG_NO_ANIMATION))
                assertTrue(extras.getString("StringExtra") == src.extras.getString("StringExtra"))
                assertTrue(extras.getLong("LongExtra") == src.extras.getLong("LongExtra"))
                assertTrue(extras.getInt("IntExtra") == 1)
            }
        }
    }

    @Test
    fun testSetAnim(){
        FragmentIntent().apply {
            setAnim(123, 124, 0, 0);
            assertTrue(inAnim == showAnim)
            assertTrue(outAnim == hideAnim)

            setAnim(123, 124, 125, 126)
            assertTrue(inAnim == 123)
            assertTrue(outAnim == 124)
            assertTrue(showAnim == 125)
            assertTrue(hideAnim == 126)
        }
    }
}

class PSVo() : Parcelable, Serializable {
    constructor(`in`: Parcel) : this(){
        `in`.run {
            string = readString()
            int = readInt()
        }
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.apply {
            writeString(string)
            writeInt(int)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    var string = ""
    var int = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<PSVo> = object : Parcelable.Creator<PSVo> {
            override fun createFromParcel(`in`: Parcel): PSVo {
                return PSVo(`in`)
            }

            override fun newArray(size: Int): Array<PSVo?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if(other is PSVo){
            return string == other.string && int == other.int
        }
        return super.equals(other)
    }

}