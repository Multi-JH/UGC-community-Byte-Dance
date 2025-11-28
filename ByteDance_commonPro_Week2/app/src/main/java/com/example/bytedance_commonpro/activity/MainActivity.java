package com.example.bytedance_commonpro.activity;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.bytedance_commonpro.R;
import com.example.bytedance_commonpro.fragment.HomeFragment;
import com.example.bytedance_commonpro.fragment.FriendsFragment;
import com.example.bytedance_commonpro.fragment.MessageFragment;
import com.example.bytedance_commonpro.fragment.ProfileFragment;

public class MainActivity extends AppCompatActivity {
    private TextView tabHome, tabFriends, tabMessage, tabProfile;// 底部导航项
    private ImageView tabCamera;
    private int currentPosition = -1; // 当前选中的位置
    private static final String TAG_HOME = "home";// Fragment标签
    private static final String TAG_FRIENDS = "friends";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_PROFILE = "profile";
    private HomeFragment homeFragment; // Fragment实例
    private FriendsFragment friendsFragment;
    private MessageFragment messageFragment;
    private ProfileFragment profileFragment;
    private FragmentManager fragmentManager;// Fragment管理器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);//关联界面，同时要在manifest声明
        setContentView(R.layout.activity_main);//初始化界面后执行的内容
        initViews();// 初始化视图组件
        setupFragments();// 设置Fragment实例
        setupBottomNavigation();// 设置底部导航监听
        switchToFragment(0);// 默认显示首页
    }


    /**
     * 初始化所有视图组件
     */
    private void initViews() {
        tabHome = findViewById(R.id.tab_home);// 初始化底部导航视图
        tabFriends = findViewById(R.id.tab_friends);
        tabCamera = findViewById(R.id.tab_camera);
        tabMessage = findViewById(R.id.tab_message);
        tabProfile = findViewById(R.id.tab_profile);
        fragmentManager = getSupportFragmentManager();// 初始化Fragment管理器
    }

    /**
     * 创建Fragment实例
     */
    private void setupFragments() {
        homeFragment = new HomeFragment();
        friendsFragment = new FriendsFragment();
        messageFragment = new MessageFragment();
        profileFragment = new ProfileFragment();
    }

    /**
     * 设置底部导航点击监听器
     */
    private void setupBottomNavigation() {
        // 设置首页点击监听
        tabHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToFragment(0);
            }
        });
        // 设置朋友点击监听
        tabFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToFragment(1);
            }
        });
        // 设置相机点击监听
        tabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });
        // 设置消息点击监听
        tabMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToFragment(3);
            }
        });
        // 设置我的点击监听
        tabProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToFragment(4);
            }
        });
    }

    /**
     * 切换到指定位置的Fragment
     */
    private void switchToFragment(int position) {
        // 如果点击的是当前位置，不处理
        if (currentPosition == position) {
            return;
        }
        // FragmentTransaction 是为了安全地管理 Fragment 的添加、移除、替换和其他操作
        // 将多个 Fragment 操作打包成一个整体进行隐藏、显示，避免界面状态不一致，实现操作原子性
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
        //得到当前的Fragment
        Fragment currentFragment = getCurrentFragment();
        //检查Fragment不为null且已添加到FragmentManager中
        if (currentFragment != null && currentFragment.isAdded()) {
            //隐藏而非销毁，保持原有界面状态（如滚动位置、输入内容等）
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
            // 如果Fragment已经添加，直接显示
            transaction.show(fragment);
        } else {
            // 如果Fragment未添加，添加到容器中
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
        return getFragmentByPosition(currentPosition);
    }

    /**
     * 更新底部导航的选中状态
     */
    private void updateBottomNavigationState(int position) {
        // 获取主题感知的颜色（自动适配深色/浅色）
        int unselectedColor = ContextCompat.getColor(this, R.color.bottom_bar_item_color);
        int selectedColor = ContextCompat.getColor(this, R.color.bottom_bar_item_color);

        // 重置所有文本为未选中状态
        tabHome.setTextColor(unselectedColor);
        tabFriends.setTextColor(unselectedColor);
        tabMessage.setTextColor(unselectedColor);
        tabProfile.setTextColor(unselectedColor);

        // 清除所有加粗
        tabHome.setTypeface(null, Typeface.NORMAL);
        tabFriends.setTypeface(null, Typeface.NORMAL);
        tabMessage.setTypeface(null, Typeface.NORMAL);
        tabProfile.setTypeface(null, Typeface.NORMAL);

        // 设置选中项
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
     * 打开相机功能
     */
    private void openCamera() {
        // 这里实现打开相机的逻辑
        // 可以是启动系统相机，或者打开自定义相机页面
        Toast.makeText(this, "打开相机功能", Toast.LENGTH_SHORT).show();


    }

}