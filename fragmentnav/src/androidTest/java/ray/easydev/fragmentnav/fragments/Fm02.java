package ray.easydev.fragmentnav.fragments;

import android.view.View;

/**
 * Created by Ray on 2017/11/21.
 */

public class Fm02 extends FmBase {
    @Override
    public void onClick(View v) {
        super.onClick(v);
        setResult(123, null);
        setFinishAnim(NO_ANIM).finish();
    }
}
