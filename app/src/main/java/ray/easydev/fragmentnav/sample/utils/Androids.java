package ray.easydev.fragmentnav.sample.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Process;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ray.easydev.fragmentnav.log.Log;

import static android.content.pm.PackageManager.GET_SERVICES;

public class Androids {

    public static boolean isServiceProcess(Context c) {
        PackageManager packageManager = c.getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(c.getPackageName(), GET_SERVICES);
        } catch (Exception e) {
            android.util.Log.e("AndroidUtils", "Could not get package info for " + c.getPackageName(), e);
            return false;
        }

        String mainProcess = packageInfo.applicationInfo.processName;
        ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        String myPkgName = c.getPackageName();
        List<ActivityManager.RunningServiceInfo> runningServiceInfos = am.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo rsi : runningServiceInfos) {
            String pkgName = rsi.service.getPackageName();
            if (myPkgName.equals(pkgName)) {
                int myPid = Process.myPid();
                if (rsi.pid == myPid) {
                    return !mainProcess.equals(rsi.process);
                }
            }
        }

        return false;
    }

    public static boolean isMainProcess(Context c) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager am = ((ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE));
            List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
            if (processInfos == null) {
                return true;
            }
            String mainProcessName = c.getPackageName();
            int myPid = Process.myPid();
            for (ActivityManager.RunningAppProcessInfo info : processInfos) {
                if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                    return true;
                }
            }
            return false;
        } else {
            return !isServiceProcess(c);
        }
    }


    public static String colorText(String text, String color) {
        String fontColor = "<font color=" + color + ">%s</font>";
        return String.format(fontColor, text);
    }

    public static boolean isFragmentAlive(Fragment fragment) {
        return fragment != null && fragment.isAdded();
    }

    public static boolean isActivityDestoryed(Activity activity) {
        if (activity == null) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return activity.isDestroyed();
        } else {
            return activity.isFinishing();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T findView(Activity activity, int id) {
        return (T) activity.findViewById(id);
    }

    @SuppressWarnings("unchecked")
    public static <T> T findView(View view, int id) {
        return (T) view.findViewById(id);
    }

    public static int convertDpToPixel(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }


    public static int getStatusbarHeight(Context c) {
        int result = 0;
        int resourceId = c.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = c.getResources().getDimensionPixelSize(resourceId);
        } else {
            Log.e(Androids.class, "Can not get status bar");
        }
        return result;
    }

    private static String formatIpAddress(int ipAdress) {
        return (ipAdress & 0xFF) + "." +
                ((ipAdress >> 8) & 0xFF) + "." +
                ((ipAdress >> 16) & 0xFF) + "." +
                (ipAdress >> 24 & 0xFF);
    }

    public static <T> T inflate(Context c, int resId, ViewGroup parent) {
        return (T) LayoutInflater.from(c).inflate(resId, parent, false);
    }


    public static void execOnViewPreDraw(final View view, final Runnable runnable) {
        if (view != null && runnable != null) {
            view.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    view.getViewTreeObserver().removeOnPreDrawListener(this);
                    runnable.run();
                    return true;
                }
            });
        }
    }

    public static void setOnClickListener(View.OnClickListener l, View... views) {
        for (View view : views) {
            view.setOnClickListener(l);
        }
    }

    public static void setOnClickListener(Activity activity, View.OnClickListener l, int... viewsId) {
        for (int id : viewsId) {
            activity.findViewById(id).setOnClickListener(l);
        }
    }

    public static void setOnClickListener(View view, View.OnClickListener l, int... viewsId) {
        for (int id : viewsId) {
            view.findViewById(id).setOnClickListener(l);
        }
    }

    public static void hideKeyboard(Activity activity) {
        if (activity == null) {
            return;
        }
        InputMethodManager inputmanger = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputmanger.hideSoftInputFromWindow(activity.getWindow().peekDecorView().getWindowToken(), 0);
    }

    public static void hideKeyBoard(final View view) {
        view.post(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputmanger = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });

    }

    public static void showKeyBoard(final View view) {
        view.post(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputmanger = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputmanger.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        });

    }

    public static Point getScreenSize(Context c) {
        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        Point p = new Point();
        wm.getDefaultDisplay().getSize(p);

        return p;
    }

    public static int getStatusBarHeight() {
        return Resources.getSystem().getDimensionPixelSize(Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android"));
    }

    public static void setPivot(View view, float locationFactor) {
        view.setPivotX(view.getWidth() * locationFactor);
        view.setPivotY(view.getHeight() * locationFactor);
    }

    public static void setScale(View view, float scale) {
        view.setScaleX(scale);
        view.setScaleY(scale);
    }

    public static void longToast(Context c, String msg, Object... args) {
        if (c != null) {
            Toast.makeText(c, String.format(msg, args), Toast.LENGTH_LONG).show();
        }
    }

    public static void shortToast(Context c, String msg, Object... args) {
        if (c != null) {
            Toast.makeText(c, String.format(msg, args), Toast.LENGTH_SHORT).show();
        }
    }

    public static void resetViewProperty(View view) {
        view.setScaleX(1.0f);
        view.setScaleY(1.0f);
        view.setAlpha(1.0f);
        view.setTranslationX(0);
        view.setTranslationY(0);
        view.setRotation(0);
        view.setRotationX(0);
        view.setRotationY(0);
        view.animate().setStartDelay(0).setListener(null);
    }

    public static int[] getLocationOnScreen(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);

        return location;
    }

    public static int[] getLocationOnWindow(View view) {
        int[] location = new int[2];
        view.getLocationInWindow(location);

        return location;
    }

    public static Bitmap getDrawableAsBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        return null;
    }

    public static int getDrawableAsColor(Drawable drawable) {
        return 0;
    }

    public static class VPSetter {
        public VPSetter(View view) {

        }
    }

    /**
     * @param from
     * @param to
     * @return 0:TranslationX<br/>
     * 1:TranslationY<br/>
     * 2:ScaleX<br/>
     * 3:ScaleY
     */
    public static float[] getTransformation(View from, View to) {
        int[] sl = getLocationOnScreen(from);
        int[] dl = getLocationOnScreen(to);
        float sx = to.getWidth() / (float) from.getWidth();
        float sy = to.getHeight() / (float) from.getHeight();

        return new float[]{dl[0] - sl[0], dl[1] - sl[1], sx, sy};
    }

    public static View locateListViewItem(ListView listView, ItemViewLocate locate, Object compareObj) {
        for (int i = 0; i < listView.getChildCount(); i++) {
            View child = listView.getChildAt(i);
            if (locate.locate(child, compareObj)) {
                return child;
            }
        }

        return null;
    }

    public static interface ItemViewLocate {
        public boolean locate(View itemView, Object compareObj);
    }

    public static boolean checkPermissions(Context context, String... permissions) {
        String pkgName = context.getPackageName();
        PackageManager pm = context.getPackageManager();
        if (permissions != null) {
            for (String permission : permissions) {
                if (PackageManager.PERMISSION_GRANTED != pm.checkPermission(permission, pkgName)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean isAllPermissionsGranted(String[] permissions, int[] results) {
        return getGrantedPermission(permissions, results).size() != 0;
    }

    public static List<String> getGrantedPermission(String[] permissions, int[] results) {
        if (permissions == null || results == null || permissions.length != results.length) {
            return new ArrayList<String>(0);
        }

        List<String> result = new ArrayList<String>();

        for (int i = 0; i < permissions.length; i++) {
            if (results[i] == PackageManager.PERMISSION_GRANTED) {
                result.add(permissions[i]);
            }
        }

        return result;
    }
}
