package ray.easydev.fragmentnav;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.io.Serializable;

/**
 * Created by Ray on 2017/11/9.
 */

public class FragmentIntent implements Parcelable {
    public final static int FLAG_NO_ENTER_ANIMATION = Intent.FLAG_ACTIVITY_NO_USER_ACTION;
    public final static int FLAG_NO_EXIT_ANIMATION = Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS;
    public final static int FLAG_NO_ANIMATION = Intent.FLAG_ACTIVITY_NO_ANIMATION;
    public final static int FLAG_NEW_TASK = Intent.FLAG_ACTIVITY_NEW_TASK;
    public final static int FLAG_NO_HISTORY = Intent.FLAG_ACTIVITY_NO_HISTORY;

    public final static int FLAG_BROUGHT_TO_FRONT = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT;
    public final static int FLAG_SINGLE_TOP = Intent.FLAG_ACTIVITY_SINGLE_TOP;

    private int flags = 0;

    String invokerId;
    public int inAnim = R.anim.page_in, outAnim = R.anim.page_out, showAnim = R.anim.page_show, hideAnim = R.anim.page_hide;
    private String tag;

    private Class targetCls;
    private Bundle extras;

    public FragmentIntent(@NonNull Class<? extends Fragment> target, Bundle extras){
        this.targetCls = target;
        this.extras = extras != null ? new Bundle() : extras;
    }

    public FragmentIntent(@NonNull Class<? extends Fragment> target){
        this(target, new Bundle());
    }

    protected FragmentIntent(Parcel in) {
        flags = in.readInt();
        inAnim = in.readInt();
        outAnim = in.readInt();
        showAnim = in.readInt();
        hideAnim = in.readInt();
        invokerId = in.readString();
        tag = in.readString();
    }

    public static final Creator<FragmentIntent> CREATOR = new Creator<FragmentIntent>() {
        @Override
        public FragmentIntent createFromParcel(Parcel in) {
            return new FragmentIntent(in);
        }

        @Override
        public FragmentIntent[] newArray(int size) {
            return new FragmentIntent[size];
        }
    };

    public @NonNull
    String getInvokerId(){
        return invokerId == null ? "" : invokerId;
    }

    public Class getTargetCls() {
        return targetCls;
    }

    public int getFlags() {
        return flags;
    }

    public FragmentIntent addFlag(int flag){
        flags |= flag;
        return this;
    }

    public FragmentIntent setFlags(int flags){
        this.flags = flags;
        return this;
    }

    public @NonNull
    String getTag() {
        return tag == null ? "" : tag;
    }

    public FragmentIntent setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public FragmentIntent setAnim(int inAnim, int outAnim, int showAnim, int hideAnim){
        this.inAnim = inAnim;
        this.outAnim = outAnim;
        this.inAnim = inAnim;
        this.outAnim = outAnim;
        return this;
    }

    public FragmentIntent putExtra(String key, int value){
        extras.putInt(key, value);
        return this;
    }

    public FragmentIntent putExtra(String key, String value){
        extras.putString(key, value);
        return this;
    }

    public FragmentIntent putExtra(String key, long value){
        extras.putLong(key, value);
        return this;
    }

    public FragmentIntent putExtra(String key, Double value){
        extras.putDouble(key, value);
        return this;
    }

    public FragmentIntent putExtra(String key, Float value){
        extras.putFloat(key, value);
        return this;
    }

    public FragmentIntent putExtra(String key, Parcelable value){
        extras.putParcelable(key, value);
        return this;
    }

    public FragmentIntent putExtra(String key, Serializable value){
        extras.putSerializable(key, value);
        return this;
    }

    public @NonNull
    Bundle getExtras(){
        return extras;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(flags);
        dest.writeInt(inAnim);
        dest.writeInt(outAnim);
        dest.writeInt(showAnim);
        dest.writeInt(hideAnim);
        dest.writeString(getInvokerId());
        dest.writeString(getTag());
    }

    public void write(Bundle bundle){
        if(bundle != null){
            bundle.putParcelable("_" + FragmentIntent.class.getCanonicalName(), this);
        }
    }

    public @NonNull
    static FragmentIntent read(Bundle bundle){
        FragmentIntent result = null;
        if(bundle != null){
            result = bundle.getParcelable("_" + FragmentIntent.class.getCanonicalName());
        }

        if(result == null){
            throw new NullPointerException("Can not find framgent intent");
        }

        return result;
    }
}
