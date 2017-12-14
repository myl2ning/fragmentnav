package ray.easydev.fragmentnav.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ray.easydev.fragmentnav.FnFragment;
import ray.easydev.fragmentnav.test.R;

/**
 * Created by Ray on 2017/11/21.
 */

public class FmBase extends FnFragment {

    public final static FragmentResult EMPTY_RESULT = new FragmentResult();
    private FragmentResult fragmentResult;

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Object data) {
        super.onFragmentResult(requestCode, resultCode, data);
        fragmentResult = new FragmentResult(requestCode, resultCode, data);
    }

    public @NonNull FragmentResult getFragmentResult() {
        return fragmentResult == null ? EMPTY_RESULT  : fragmentResult;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fm_fn_test, container, false);
        childView.setTag(getClass().getSimpleName());
        ((TextView) childView.findViewById(R.id.tv_log)).setText(getClass().getSimpleName());

        int fragmentSize = getFragmentNav().fragmentSize();
        childView.setBackgroundColor(fragmentSize % 2 == 0 ? 0xff6cad74 : 0xff3399dd);

        return childView;
    }



    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public static class FragmentResult {
        public int requestCode, resultCode;
        public Object result;

        public FragmentResult(int requestCode, int resultCode, Object result){
            this.requestCode = requestCode;
            this.resultCode = resultCode;
            this.result = result;
        }

        FragmentResult(){}

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof FragmentResult){
                FragmentResult out = (FragmentResult) obj;

                return requestCode == out.requestCode && resultCode == out.resultCode
                        && result == out.result;
            }
            return super.equals(obj);
        }
    }
}
