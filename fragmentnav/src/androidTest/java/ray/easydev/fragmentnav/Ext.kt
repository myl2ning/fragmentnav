package ray.easydev.fragmentnav

import android.util.SparseArray


/**
 * Created by Ray on 2017/12/6.
 */
fun<T> SparseArray<T>.comparesTo(other: SparseArray<T>) : Boolean {
    if(size() != other.size()){
        return false
    }

    for(i in 0 until size()){
        if(keyAt(i) != other.keyAt(i) || valueAt(i) != other.valueAt(i)){
            return false
        }
    }

    return true
}
