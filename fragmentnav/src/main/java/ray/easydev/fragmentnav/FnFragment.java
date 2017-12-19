package ray.easydev.fragmentnav;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import ray.easydev.fragmentnav.log.Log;

/**
 * Created by Ray on 2017/11/21.
 */
public class FnFragment extends Fragment {
    public final static int INVALID_INT = -1;
    public final static int NO_ANIM = 0;

    private final static String _ARG_FRAGMENT_ID = FragmentNavImpl.IdGenerator.fromClass(FnFragment.class) + " fnId";

    private FragmentIntent mIntent;
    private Integer mResultCode = null;
    private Object mResultData;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if(!(getActivity() instanceof  FnActivity)){
            FnUtils.criticalError("FnFragment must run in FnActivity");
        }

        if(savedInstanceState != null){
            //在所有Fragment onCreate之前还原，这样可以保证FragmentNav在Fragment过程中的onCreate时可用
            ((FnActivity) getActivity()).getFragmentNav().restoreState(savedInstanceState);
        }
        super.onCreate(savedInstanceState);
    }

    public @NonNull
    FragmentNav getFragmentNav(){
        return ((FnActivity) getActivity()).getFragmentNav();
    }

    public void onFragmentResult(int requestCode, int resultCode, Object data){

    }

    public void onNewIntent(FragmentIntent intent){

    }

    public @NonNull FnFragment startFragment(FragmentIntent... intents){
        return getFragmentNav().startFragment(this, intents);
    }

    public @NonNull FnFragment startFragmentForResult(int requestCode, FragmentIntent intent){
        return getFragmentNav().startFragmentForResult(this, requestCode, intent);
    }

    public void finish(){
        getFragmentNav().finish(this);
    }

    public void finishTask(){
        getFragmentNav().finishTask(this);
    }

    public void setResult(int resultCode, Object result){
//        if(result instanceof Parcelable){
//
//        } else if (result instanceof Serializable){
//
//        }

        mResultCode = resultCode;
        mResultData = result;
    }

    public @NonNull
    FragmentIntent getIntent(){
        if(mIntent == null){
            mIntent = FragmentIntent.read(getArguments());
        }

        return mIntent;
    }

    public Integer getResultCode(){
        return mResultCode;
    }

    public Object getResultData(){
        return mResultData;
    }

    void setIntent(@NonNull FragmentIntent intent){
        if(getArguments() != null){
            intent.write(getArguments());
        }

        mIntent = intent;
    }

    /**
     * Clear the exit anim for this fragment, notice this method will clear the visible fragment
     * @param anim
     * @return
     */
    public FnFragment setFinishAnim(int anim){
        getIntent().outAnim = anim;
        return this;
    }

    public FnFragment overrideShowHideAnimThisTime(int showAnim, int hideAnim){
        getIntent().tempShowAnim = showAnim;
        getIntent().tempHideAnim = hideAnim;
        return this;
    }

    public String getFnId(){
        if(getArguments() != null){
            return getArguments().getString(_ARG_FRAGMENT_ID, "");
        }

        return "";
    }

    void setFnId(String id) {
        if (getArguments() != null) {
            getArguments().putString(_ARG_FRAGMENT_ID, id);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
//        Trace.p(getClass(), "onHiddenChanged:%s", hidden);
        if(mOnResumeCalled) onRunStateChanged(!hidden);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        Trace.p(getClass(), "onViewCreated");
    }

    @Override
    public void onResume() {
        super.onResume();
        mOnResumeCalled = true;
//        Trace.p(getClass(), "onResume  isVisible:%s, isHidden:%s", isVisible(), isHidden());

        if(isAdded() && !isHidden() && getView() != null && getView().getVisibility() == View.VISIBLE) onRunStateChanged(true);
    }

    @Override
    public void onPause() {
        super.onPause();
//        Trace.p(getClass(), "onPause  isVisible:%s, isHidden:%s", isVisible(), isHidden());
        onRunStateChanged(false);
    }

    private void onRunStateChanged(boolean visible){
        if(visible && !mIsVisible){
            mIsVisible = true;
            onForeground();
        } else if(mIsVisible){
            mIsVisible = false;
            onBackground();
        }
    }

    private boolean mOnResumeCalled, mIsVisible;

    protected void onForeground(){
        Log.p("VIV", "[%s]onForeground", getClass().getSimpleName());
    }

    protected void onBackground(){
        Log.p("VIV", "[%s]onBackground", getClass().getSimpleName());
    }
}
