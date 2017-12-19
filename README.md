# FragmentNav - Start and manage fragment simpler



FragmentNav可以让你像启动activity一样方便启动的fragment，基于Task的管理方式让你可以方便的按需求来一次性启动/结束多个fragment。



## 功能

- 基于Task的管理方式
- 支持批量启动/结束fragments
- 支持BringToFront操作
- 自动保存在Activity.onSaveInstanceState之后执行的commit操作以及当App还原时自动执行相应操作
- 支持startFragmentForResult操作



## 准备

1、创建一个Activity继承FnFragmentActivity或FnAppCompatActivity，以继承FnFragmentActivity为例:

##### MainActivity.java

```java
public class MainActivity extends FnFragmentActivity {
    static {
        //Configure the default fragment animations
        FragmentIntent.getDefault().setAnim(
          R.anim.page_start, //animation for start a fragment
          R.anim.page_finish, //animation for finish a fragment
          R.anim.page_show, //animation for show the previous fragment when finish a fragment
          R.anim.page_hide //animation for hide the fragment when start a new fragment
        );
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public int getFragmentContainerId() {
      	//The id of ViewGroup that contains fragments
        return R.id.fragment_container;
    }

    @NonNull
    @Override
    public FragmentIntent[] getStartIntents() {
      	//Configure the enter fragments
        return new FragmentIntent[]{
                new FragmentIntent(FmEnter.class)
                  //Disable the start animation to avoid the conflict with the activity's start animation
                  .addFlag(FragmentIntent.FLAG_NO_START_ANIMATION)
        };
    }
}
```

#### activity_main.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
             android:id="@+id/fragment_container"
             android:layout_width="match_parent"
             android:layout_height="match_parent"/>

```



2、创建一个Fragment继承FnFragment

```java
class BaseFragment extends FnFragment {
  
}
```

**然后就可以愉快的使用FragmentNav了😀**



## 用法

### 启动一个fragment

```java
 //Create a fragment intent with a string extra
 FragmentIntent fragmentIntent = new FragmentIntent(Fm01.class).
                putExtra(KEY_STRING, "StringExtra");
 //Start the fragment
 startFragment(fragmentIntent);
```



### 结束单个fragment

```java
//类似结束activity，调用finish方法即可结束当前fragment
finish();
```



### 批量启动fragment

```java
//Create a intent to start fragment11 in new task
FragmentIntent intent11 = new FragmentIntent(Fm11.class)
  	.addFlag(FragmentIntent.FLAG_NEW_TASK);
//This fragment will be started in the same task with fragment11
FragmentIntent intent12 = new FragmentIntent(Fm12.class);

//This intent describe fragment21 will be started in another new task
FragmentIntent intent21 = new FragmentIntent(Fm21.class)
     .addFlag(FragmentIntent.FLAG_NEW_TASK);

//Start fragments, only fragment21's start animation will be played
startFragment(intent11, intent12, intent21);
```



###结束fragment task

```java
//Finish the task current fragment belongs
finishTask();

//Finish the tasks by task id
getFragmentNav().finishTasks(0, 1, 2)
```



### BringToFront

```java
//Bring fragment02 to front if it exists in tasks, else start a new one
startFragment(new FragmentIntent(Fm02.class).addFlag(FragmentIntent.FLAG_BRING_TO_FRONT));
```



## Todo

- Activity.onSaveInstanceState后commit的操作持久化到本地以支持App还原时执行

- 支持NO_HISTORY方式启动

- More (you tell me😀)...

  ​


## Versions

##### Version 0.1.0 `17121901`
- first working prototype

