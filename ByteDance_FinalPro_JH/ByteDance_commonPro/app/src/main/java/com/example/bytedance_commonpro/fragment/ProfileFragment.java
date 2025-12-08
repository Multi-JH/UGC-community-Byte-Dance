package com.example.bytedance_commonpro.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bytedance_commonpro.R;
import com.example.bytedance_commonpro.adapter.WorksAdapter;
import com.example.bytedance_commonpro.model.Work;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
/**
 * ProfileFragment类
 * 表示个人主页
 * 包含个人信息以及作品列表
 */
public class ProfileFragment extends Fragment {

    private ImageView profileAvatar;
    private TextView profileName;
    private TextView profileId;
    private TextView profileSignature;
    private TextView followingCount;
    private TextView followersCount;
    private TextView likesCount;
    private Button editProfileBtn;
    private ImageView ivScan;
    private ImageView ivSettings;
    private RecyclerView worksGrid;

    // 适配器
    private WorksAdapter worksAdapter;
    private List<Work> workList = new ArrayList<>();
    /**
     * 生命周期（onCreateView） 创建视图
     * 当Fragment需要绘制其UI时调用
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        //初始化所有UI组件
        initViews(view);
        //设置用户基本信息
        setupUserData();
        //设置作品网格视图
        setupWorksGrid();
        //设置点击事件监听器
        setupClickListeners();
        return view;
    }
    /**
     * initViews()
     * 初始化所有UI组件
     * 通过findViewById方法将XML布局中的组件绑定到Java变量
     */
    private void initViews(View view) {
        profileAvatar = view.findViewById(R.id.profile_avatar);
        profileName = view.findViewById(R.id.profile_name);
        profileId = view.findViewById(R.id.profile_id);
        profileSignature = view.findViewById(R.id.profile_signature);
        followingCount = view.findViewById(R.id.profile_following_count);
        followersCount = view.findViewById(R.id.profile_followers_count);
        likesCount = view.findViewById(R.id.profile_likes_count);
        editProfileBtn = view.findViewById(R.id.profile_edit_btn);
        ivScan = view.findViewById(R.id.iv_scan);
        ivSettings = view.findViewById(R.id.iv_settings);
        worksGrid = view.findViewById(R.id.profile_works_grid);
    }
    /**
     * setupUserData()
     * 设置用户基本数据
     * 模拟用户信息并显示到UI上
     */
    private void setupUserData() {
        // 模拟用户数据
        String avatarUrl = "https://vcg00.cfp.cn/creative/vcg/800/new/VCG211183291730.jpg";
        String name = "金豪";
        String userId = "Bytedance_123456";
        String signature = "热爱生活，分享美好时刻~";
        int following = 9;
        int followers = 99;
        int likes = 9999;

        //使用Glide加载头像
        Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.avatar_placeholder)
                .circleCrop()
                .into(profileAvatar);

        profileName.setText(name);
        profileId.setText("抖音号：" + userId);
        profileSignature.setText(signature);
        followingCount.setText(String.valueOf(following));
        followersCount.setText(String.valueOf(followers));
        likesCount.setText(String.valueOf(likes));
    }
    /**
     * setupWorksGrid()
     * 设置作品网格
     * 配置RecyclerView的布局管理器、适配器，并加载数据
     */
    private void setupWorksGrid() {
        //设置网格布局，每行2个
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        worksGrid.setLayoutManager(layoutManager);
        //创建适配器
        worksAdapter = new WorksAdapter(workList, work -> {
            Toast.makeText(getContext(), "点击作品：" + work.getTitle(), Toast.LENGTH_SHORT).show();
        });
        worksGrid.setAdapter(worksAdapter);
        loadWorksData();
    }
    /**
     * loadWorksData()
     * 加载作品数据
     * 模拟从服务器获取作品数据
     */
    private void loadWorksData() {

        //模拟作品数据
        List<Work> newWorkList = new ArrayList<>();
        Random random = new Random();
        int temp = random.nextInt(15);
        for (int i = 1; i <= temp; i++) {
            newWorkList.add(new Work(
                    String.valueOf(i),
                    "https://picsum.photos/200/300?random=" + i,
                    random.nextInt(9999),
                    "作品标题 " + i,
                    "2024-01-" + (i < 10 ? "0" + i : i)
            ));
        }

        // 清空并重新添加数据
        workList.clear();
        workList.addAll(newWorkList);

        // 通知适配器数据已更新
        if (worksAdapter != null) {
            worksAdapter.notifyDataSetChanged();
        } else {
            Log.e("ProfileFragment", "worksAdapter 为 null!");
        }
    }
    /**
     * setupClickListeners()
     * 设置点击事件监听器
     * 为UI组件添加点击事件处理
     */
    private void setupClickListeners() {
        // 编辑资料按钮
        editProfileBtn.setOnClickListener(v -> {
            Toast.makeText(getContext(), "编辑资料", Toast.LENGTH_SHORT).show();
        });

        // 头像点击
        profileAvatar.setOnClickListener(v -> {
            Toast.makeText(getContext(), "点击头像，可以更换头像", Toast.LENGTH_SHORT).show();
        });
    }
}