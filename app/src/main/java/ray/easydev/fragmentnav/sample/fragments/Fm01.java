package ray.easydev.fragmentnav.sample.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Ray on 2017/11/21.
 */

public class Fm01 extends FmBase {

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(getArguments().containsKey(KEY_STRING)){
            Toast.makeText(getContext(), "Extra found:" + getArguments().get(KEY_STRING), Toast.LENGTH_SHORT).show();
        }
    }

}
