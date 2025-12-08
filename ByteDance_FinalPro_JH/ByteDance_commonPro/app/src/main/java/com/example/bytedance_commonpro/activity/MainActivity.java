package com.example.bytedance_commonpro.activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.bytedance_commonpro.R;
import com.example.bytedance_commonpro.fragment.HomeFragment;
import com.example.bytedance_commonpro.fragment.FriendsFragment;
import com.example.bytedance_commonpro.fragment.MessageFragment;
import com.example.bytedance_commonpro.fragment.ProfileFragment;
import com.example.bytedance_commonpro.manager.MusicManager;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "app_lifecycle";
    private static final String KEY_LAST_CLOSE_TIME = "last_close_time";
    private static final long COLD_START_THRESHOLD = 30000;
    private TextView tabHome, tabFriends, tabMessage, tabProfile;
    private ImageView tabCamera;
    private int currentPosition = -1;
    private static final String TAG_HOME = "home";
    private static final String TAG_FRIENDS = "friends";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_PROFILE = "profile";
    private HomeFragment homeFragment;
    private FriendsFragment friendsFragment;
    private MessageFragment messageFragment;
    private ProfileFragment profileFragment;
    private FragmentManager fragmentManager;

    private long lastClickTime = 0;
    private static final long DOUBLE_CLICK_TIME_DELTA = 500;

    private OnHomeTabClickListener homeTabClickListener;

    public interface OnHomeTabClickListener {
        void onHomeTabDoubleClick();
    }

    public void setOnHomeTabClickListener(OnHomeTabClickListener listener) {
        this.homeTabClickListener = listener;
    }
    /**
     * 生命周期（onCreate）
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //冷启动检测
        if (isColdStart()) {
            MusicManager.getInstance(this).resetOnColdStart();
        }
        //初始化视图组件
        initViews();
        //设置Fragment实例
        setupFragments();
        //设置底部导航监听
        setupBottomNavigation();
        //默认显示首页
        switchToFragment(0);
    }
    /**
     * 生命周期（onDestroy）
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //移除所有回调
        if (homeTabClickListener != null) {
            homeTabClickListener = null;
        }
        //如果是正常退出，记录关闭时间
        if (isFinishing()) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putLong(KEY_LAST_CLOSE_TIME, System.currentTimeMillis()).apply();
        }

        homeFragment = null;
        friendsFragment = null;
        messageFragment = null;
        profileFragment = null;
    }
    /**
     * initViews()初始化所有视图组件
     */
    private void initViews() {
        tabHome = findViewById(R.id.tab_home);
        tabFriends = findViewById(R.id.tab_friends);
        tabCamera = findViewById(R.id.tab_camera);
        tabMessage = findViewById(R.id.tab_message);
        tabProfile = findViewById(R.id.tab_profile);
        fragmentManager = getSupportFragmentManager();
    }
    /**
     * 创建Fragment实例
     */
    private void setupFragments() {
        homeFragment = new HomeFragment();
        friendsFragment = new FriendsFragment();
        messageFragment = new MessageFragment();
        profileFragment = new ProfileFragment();
        // 设置首页双击监听器
        setOnHomeTabClickListener(new OnHomeTabClickListener() {
            @Override
            public void onHomeTabDoubleClick() {
                // 触发首页刷新
                if (homeFragment != null && homeFragment.isAdded()) {
                    homeFragment.refreshHomeContent();
                }
            }
        });
    }
    /**
     * 设置底部导航点击监听器
     */
    private void setupBottomNavigation() {
        tabHome.setOnClickListener(new View.OnClickListener() {
            //根据首页标签点击不同方式实现不同的行为
            @Override
            public void onClick(View v) {
                handleHomeTabClick();
            }
        });
        tabFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToFragment(1);
            }
        });
        tabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });
        tabMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToFragment(3);
            }
        });
        tabProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToFragment(4);
            }
        });
    }
    /**
     * 处理首页标签点击事件实现单击回滚顶部、双击刷新
     */
    private void handleHomeTabClick() {
        long clickTime = System.currentTimeMillis();

        if (currentPosition == 0) {
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                //双击：触发刷新
                triggerHomeTabDoubleClick();
                //重置，避免连续三次点击触发两次双击
                lastClickTime = 0;
            } else {
                //单击则滚动到顶部
                if (homeFragment != null && homeFragment.isAdded()) {
                    homeFragment.scrollToTop();
                }
            }
        } else {
            //不是当前页面，切换到首页
            switchToFragment(0);
        }
        lastClickTime = clickTime;
    }
    /**
     * 触发首页标签双击事件
     */
    private void triggerHomeTabDoubleClick() {
        if (homeTabClickListener != null) {
            homeTabClickListener.onHomeTabDoubleClick();
        }
        //添加按钮点击放大的动画
        tabHome.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(100)
                .withEndAction(() -> tabHome.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(100)
                        .start())
                .start();
    }
    /**
     * 切换到指定位置的Fragment
     */
    private void switchToFragment(int position) {
        Log.d("MainActivity", "切换Fragment，位置: " + position);
        if (currentPosition == position) {
            return;
        }

        //Fragment的事务管理是为了确保操作的原子性，实现界面状态一致
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        hideCurrentFragment(transaction);
        showFragment(transaction, position);
        updateBottomNavigationState(position);
        transaction.commit();
        currentPosition = position;
    }
    /**
     * 隐藏当前显示的Fragment
     */
    private void hideCurrentFragment(FragmentTransaction transaction) {
        Fragment currentFragment = getCurrentFragment();
        //检查Fragment不为null且已添加到FragmentManager中实现隐藏
        if (currentFragment != null && currentFragment.isAdded()) {
            transaction.hide(currentFragment);
        }
    }
    /**
     * 显示指定位置的Fragment
     */
    private void showFragment(FragmentTransaction transaction, int position) {
        Fragment fragment = getFragmentByPosition(position);
        String tag = getTagByPosition(position);
        if (fragment.isAdded()) {
            //如果Fragment已经添加，直接显示
            transaction.show(fragment);
        } else {
            //如果Fragment未添加，添加到容器中
            transaction.add(R.id.fragment_container, fragment, tag);
        }
    }
    /**
     * 根据位置获取对应的Fragment
     */
    private Fragment getFragmentByPosition(int position) {
        switch (position) {
            case 0:
                return homeFragment;
            case 1:
                return friendsFragment;
            case 3:
                return messageFragment;
            case 4:
                return profileFragment;
            default:
                return homeFragment;
        }
    }
    /**
     * 根据位置获取对应的标签，用于进行添加到容器之中，与容器进行关联
     */
    private String getTagByPosition(int position) {
        switch (position) {
            case 0:
                return TAG_HOME;
            case 1:
                return TAG_FRIENDS;
            case 3:
                return TAG_MESSAGE;
            case 4:
                return TAG_PROFILE;
            default:
                return TAG_HOME;
        }
    }
    /**
     * 获取当前显示的Fragment
     */
    private Fragment getCurrentFragment() {
        if (currentPosition == -1) {
            return null;
        }
        return getFragmentByPosition(currentPosition);
    }
    /**
     * 更新底部导航的选中状态
     */
    private void updateBottomNavigationState(int position) {
        int unselectedColor = ContextCompat.getColor(this, R.color.bottom_navigation_unselected_color);
        int selectedColor = ContextCompat.getColor(this, R.color.bottom_navigation_selected_color);
        //重置所有文本为未选中状态
        tabHome.setTextColor(unselectedColor);
        tabFriends.setTextColor(unselectedColor);
        tabMessage.setTextColor(unselectedColor);
        tabProfile.setTextColor(unselectedColor);

        tabHome.setTypeface(null, Typeface.NORMAL);
        tabFriends.setTypeface(null, Typeface.NORMAL);
        tabMessage.setTypeface(null, Typeface.NORMAL);
        tabProfile.setTypeface(null, Typeface.NORMAL);
        //设置选中项
        switch (position) {
            case 0:
                tabHome.setTextColor(selectedColor);
                tabHome.setTypeface(null, Typeface.BOLD);
                break;
            case 1:
                tabFriends.setTextColor(selectedColor);
                tabFriends.setTypeface(null, Typeface.BOLD);
                break;
            case 3:
                tabMessage.setTextColor(selectedColor);
                tabMessage.setTypeface(null, Typeface.BOLD);
                break;
            case 4:
                tabProfile.setTextColor(selectedColor);
                tabProfile.setTypeface(null, Typeface.BOLD);
                break;
        }
    }
    /**
     * Toast提示打开相机功能
     */
    private void openCamera() {
        Toast.makeText(this, "打开相机功能", Toast.LENGTH_SHORT).show();
    }
    /**
     * 检测是否为冷启动
     * 规则：上次关闭时间超过30秒或者关闭时间为0（初次安装）认为是冷启动
     */
    private boolean isColdStart() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long lastCloseTime = prefs.getLong(KEY_LAST_CLOSE_TIME, 0);
        long currentTime = System.currentTimeMillis();
        //如果上次关闭时间为0（首次安装）或超过阈值则是冷启动
        boolean isColdStart = lastCloseTime == 0 || (currentTime - lastCloseTime) > COLD_START_THRESHOLD;
        return isColdStart;
    }
}