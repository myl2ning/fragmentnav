package ray.easydev.fragmentnav.sample;

import android.app.Application;

import ray.easydev.fragmentnav.utils.Trace;

/**
 * Created by Ray on 2017/11/29.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Trace.setLogLevel(true, 10);
    }
}
