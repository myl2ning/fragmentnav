package ray.easydev.fragmentnav;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.io.Serializable;

import static ray.easydev.fragmentnav.FnUtils.hasBit;

/**
 * Created by Ray on 2017/11/24.
 */

class Op implements Parcelable, Serializable {
    final static int OP_ADD = 1;
    final static int OP_REMOVE = 2;
    final static int OP_SHOW = 3;
    final static int OP_HIDE = 4;
    final static int OP_ATTACH = 5;
    final static int OP_DETACH = 6;

    final static int OP_BRING_TO_FRONT = 7;

    transient FnFragment fragment;

    int op;
    int enterAnim, exitAnim;
    String fragmentId;

    Op(int op, FnFragment fragment) {
        this.op = op;
        this.fragment = fragment;
        fragmentId = fragment.getFnId();

        initAnim(fragment);
    }

    protected Op(Parcel in) {
        op = in.readInt();
        enterAnim = in.readInt();
        exitAnim = in.readInt();
        fragmentId = in.readString();
    }

    public static final Creator<Op> CREATOR = new Creator<Op>() {
        @Override
        public Op createFromParcel(Parcel in) {
            return new Op(in);
        }

        @Override
        public Op[] newArray(int size) {
            return new Op[size];
        }
    };

    private void initAnim(FnFragment fragment) {
        FragmentIntent intent = fragment.getIntent();
        if (!hasBit(intent.getFlags(), FragmentIntent.FLAG_NO_ANIMATION)) {
            boolean hasEnterAnim = !hasBit(intent.getFlags(), FragmentIntent.FLAG_NO_ENTER_ANIMATION), hasExitAnim = !hasBit(intent.getFlags(), FragmentIntent.FLAG_NO_EXIT_ANIMATION);

            switch (op) {
                case OP_ADD:
                    setAnim(hasEnterAnim ? intent.inAnim : 0, 0);
                    break;
                case OP_REMOVE:
                    if (fragment.isVisible() && hasExitAnim) {
                        setAnim(0, intent.outAnim);
                    }
                    break;
                case OP_SHOW:
                    setAnim(intent.showAnim, 0);
                    break;
                case OP_HIDE:
                    if (fragment.isVisible()) {
                        setAnim(0, intent.hideAnim);
                    }
                    break;
                case OP_BRING_TO_FRONT:
                    setAnim(intent.inAnim, 0);
                    break;
            }
        }
    }

    Op setAnim(int enterAnim, int exitAnim) {
        this.enterAnim = enterAnim;
        this.exitAnim = exitAnim;
        return this;
    }

    void clearAnim(){
        enterAnim = exitAnim = 0;
    }

    @Override
    public String toString() {
        if (fragment == null && TextUtils.isEmpty(fragmentId)) {
            return "";
        }
        StringBuilder sb = new StringBuilder(fragment != null ? fragment.getClass().getSimpleName() : ("FnId:" + fragmentId)).append(" ");
        switch (op) {
            case OP_ADD:
                sb.append("ADD");
                break;
            case OP_REMOVE:
                sb.append("REMOVE");
                break;
            case OP_SHOW:
                sb.append("SHOW");
                break;
            case OP_HIDE:
                sb.append("HIDE");
                break;
            case OP_ATTACH:
                sb.append("ATTACH");
                break;
            case OP_DETACH:
                sb.append("DETACH");
                break;
            case OP_BRING_TO_FRONT:
                sb.append("BRING_TO_FRONT");
                break;
        }
        sb.append(" Anim:").append(enterAnim == 0 ? "NO-" : "YES-").append(exitAnim == 0 ? "NO" : "YES");
        if(fragment != null){
            sb.append(" IS VISIBLE:").append(fragment.isVisible());
        }

        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(op);
        dest.writeInt(enterAnim);
        dest.writeInt(exitAnim);
        dest.writeString(fragmentId);
    }
}
