package ray.easydev.fragmentnav.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Created by Ray on 2017/11/21.
 */

public class Fm22 extends FmBase {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        getFragmentNav().finishTasks(0, 1, 2);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getFragmentNav().finishTasks(0, 1, 2);

    }
}
