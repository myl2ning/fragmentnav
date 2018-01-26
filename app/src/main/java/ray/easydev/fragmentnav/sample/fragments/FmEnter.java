package ray.easydev.fragmentnav.sample.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import ray.easydev.fragmentnav.FnFragment;
import ray.easydev.fragmentnav.FragmentIntent;
import ray.easydev.fragmentnav.log.Log;
import ray.easydev.fragmentnav.sample.Consts;
import ray.easydev.fragmentnav.sample.R;
import ray.easydev.fragmentnav.sample.utils.Androids;
import ray.easydev.fragmentnav.sample.utils.Utils;


/**
 * Created by Ray on 2017/11/21.
 */

public class FmEnter extends FnFragment implements Consts {
    final static int REQUEST_CODE = 9528;

    List<Item> items = new ArrayList<>();
    {
        items.add(new Item("startFragmentWithExtras"));
        items.add(new Item("startWithNewAnim"));
        items.add(new Item("startInNewTask"));
        items.add(new Item("FinishTask"));
        items.add(new Item("startSingleFragmentForResult"));
        items.add(new Item("startFragmentsForResult"));
        items.add(new Item("batchStart"));
        items.add(new Item("bringToFront"));
    }

    private static Handler mHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initHandler();
    }

    private void initHandler(){
        if(mHandler != null) return;
        try {
            Field field = FragmentActivity.class.getDeclaredField("mHandler");
            field.setAccessible(true);
            mHandler = (Handler) field.get(getActivity());

        } catch (Exception e) {
            mHandler = new Handler();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fm_enter, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView listView = (ListView) view.findViewById(R.id.listView);
        listView.setAdapter(new Adapter());

    }

    public final void startFragmentWithExtras(){
        //Create a fragment intent with a string extra
        FragmentIntent fragmentIntent = new FragmentIntent(Fm01.class).
                putExtra(KEY_STRING, "StringExtra").setAnim(R.anim.page_start, R.anim.page_finish, R.anim.page_show, R.anim.page_hide );
        //Start the fragment
        startFragment(fragmentIntent);

    }

    public final void startWithNewAnim(){
        //Animation override must be called before any action
        overrideShowHideAnimThisTime(R.anim.page_insert, R.anim.page_delete);

        FragmentIntent fragmentIntent = new FragmentIntent(Fm01.class)
                .setAnim(R.anim.page_insert, R.anim.page_delete, R.anim.page_show, R.anim.page_hide);
        startFragment(fragmentIntent);

        setNextAction(
                fragmentIntent.getExtras(),
                FmBase.Action.START,
                new FragmentIntent(Fm12.class)
                        .addFlag(FragmentIntent.FLAG_NEW_TASK)
        );
    }

    public final void startInNewTask(){
        startFragment(new FragmentIntent(Fm11.class).addFlag(FragmentIntent.FLAG_NEW_TASK));
    }

    public final void batchStart(){
        FragmentIntent intent11 = new FragmentIntent(Fm11.class).addFlag(FragmentIntent.FLAG_NEW_TASK);
        FragmentIntent intent12 = new FragmentIntent(Fm12.class);
        FragmentIntent intent21 = new FragmentIntent(Fm21.class).addFlag(FragmentIntent.FLAG_NEW_TASK);

        startFragment(intent11, intent12, intent21);
    }

    public void FinishTask(){
        FragmentIntent intent11 = new FragmentIntent(Fm11.class).addFlag(FragmentIntent.FLAG_NEW_TASK);
        FragmentIntent intent12 = new FragmentIntent(Fm12.class);
        FragmentIntent intent21 = new FragmentIntent(Fm21.class).addFlag(FragmentIntent.FLAG_NEW_TASK);
        FragmentIntent intent22 = new FragmentIntent(Fm22.class);
        startFragment(intent11, intent12, intent21, intent22);

        setNextAction(intent22.getExtras(), FmBase.Action.FINISH_MY_TASK);
    }

    public void startSingleFragmentForResult(){
        FragmentIntent intent01 = new FragmentIntent(Fm01.class);
        startFragmentForResult(REQUEST_CODE, intent01);
        setNextAction(intent01.getExtras(), FmBase.Action.FINISH_WITH_RESULT);
    }

    public void startFragmentsForResult(){
        FragmentIntent intent01 = new FragmentIntent(Fm01.class).addFlag(FragmentIntent.FLAG_NEW_TASK);
        FragmentIntent intent21 = new FragmentIntent(Fm21.class).addFlag(FragmentIntent.FLAG_NEW_TASK);
        FragmentIntent intent23 = new FragmentIntent(Fm23.class);
        startFragmentForResult(REQUEST_CODE, intent01, intent21, intent23);

        setNextAction(intent21.getExtras(), FmBase.Action.FINISH_WITH_RESULT);
        setNextAction(intent23.getExtras(), FmBase.Action.FINISH_WITH_RESULT);
    }

    public void bringToFront(){
        FragmentIntent intent01 = new FragmentIntent(Fm01.class).addFlag(FragmentIntent.FLAG_NEW_TASK);
        FragmentIntent intent21 = new FragmentIntent(Fm21.class).addFlag(FragmentIntent.FLAG_NEW_TASK);
        FragmentIntent intent23 = new FragmentIntent(Fm23.class);
        startFragment(intent01, intent21, intent23);

        setNextAction(
                intent23.getExtras(),
                FmBase.Action.BRING_TO_FRONT,
                new FragmentIntent(Fm01.class).addFlag(FragmentIntent.FLAG_BRING_TO_FRONT)
        );
    }


    private Bundle setNextAction(Bundle bundle, FmBase.Action action, FragmentIntent... intents){
        bundle.putSerializable(KEY_ACTION, action);
        bundle.putParcelableArray(KEY_ACTION_ARG, intents);
        return bundle;
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Object data) {
        super.onFragmentResult(requestCode, resultCode, data);
        Log.p(getClass(), "onFragmentResult:%s, %s, %s", requestCode, resultCode, data);
        Androids.shortToast(getContext(), "onFragmentResult:\nRequestCode=>%s\nResultCode=>%s\nData=>%s", requestCode, resultCode, data);
    }


    private void printAdded(){
        try {
            Field field = getFragmentManager().getClass().getDeclaredField("mAdded");
            field.setAccessible(true);
            List<Fragment> fragments = (List<Fragment>) field.get(getFragmentManager());
            Log.p("mAdded", Utils.joinCollections(fragments, ", "));

        } catch (Exception e){

        }
    }

    private void printActive(){
        try {
            Field field = getFragmentManager().getClass().getDeclaredField("mActive");
            field.setAccessible(true);
            SparseArray<Fragment>  active = (SparseArray<Fragment>) field.get(getFragmentManager());
            System.out.println();


            Method method = getFragmentManager().getClass().getDeclaredMethod("getActiveFragments");
            method.setAccessible(true);
            List<Fragment> fragments = (List<Fragment>) method.invoke(getFragmentManager());
            Log.p("mActive", Utils.joinCollections(fragments, ", "));
        } catch (Exception e){

        }
    }

    void checkAnimRunningState(){
        checkAnimRunningState(getFragmentManager());
    }

    public static void checkAnimRunningState(final FragmentManager fragmentManager){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Fragment> fragments = fragmentManager.getFragments();
                for (Fragment fragment : fragments) {
                    if(fragment != null && fragment.getView() != null){
                        Animation animation = fragment.getView().getAnimation();
                        Log.p(fragment.getClass(), "Anim is running:%s" , animation != null && animation.hasStarted() && !animation.hasEnded());
                    } else {
                        Log.p(fragment == null ? "NULL_FRAGMENT" : fragment.getClass().getSimpleName(), "View is null" );
                    }
                }

            }
        }, 50);
    }

    class Adapter extends BaseAdapter implements View.OnClickListener {

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view == null){
                view = LayoutInflater.from(getContext()).inflate(R.layout.row_item, viewGroup, false);
                view.setOnClickListener(this);
            }

            Item item = (Item) getItem(i);
            TextView textView = (TextView) view.findViewById(R.id.tv_text);
            textView.setText(item.text);
            view.setTag(item);

            return view;
        }

        @Override
        public void onClick(View view) {
            try{
                Item item = (Item) view.getTag();
                Method method = FmEnter.class.getMethod(item.method);
                method.invoke(FmEnter.this);
            } catch (Exception e){
                Toast.makeText(getActivity(), "call method failed", Toast.LENGTH_SHORT).show();
            }

        }
    }

    public static final class Item {
        String text;
        String method;

        public Item(@NonNull String text, @NonNull String method) {
            this.text = text;
            this.method = method;
        }

        public Item(@NonNull String text) {
            this(text, text);
        }

        public String getText() {
            return text;
        }

        public @NonNull String getMethod() {
            return method;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof Item){
                return getMethod().equals(((Item) obj).getMethod());
            }
            return super.equals(obj);
        }
    }
}


