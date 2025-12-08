package com.example.bytedance_commonpro.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bytedance_commonpro.R;
import com.example.bytedance_commonpro.adapter.FriendsAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * FriendsFragment类
 * 显示用户的好友列表，支持下拉刷新和消息状态管理
 */
public class FriendsFragment extends Fragment {

    private RecyclerView friendsRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FriendsAdapter friendsAdapter;
    private List<Friend> friendList = new ArrayList<>();
    private SharedPreferences sharePreferences;

    /**
     * Friend模型 内部类
     * 代表一个好友的基本信息，包括ID、姓名、头像和是否有新消息
     */
    public static class Friend {
        private String id;
        private String name;
        private String avatarUrl;
        private boolean hasNewMessage;

        public Friend(String id, String name, String avatarUrl) {
            this.id = id;
            this.name = name;
            this.avatarUrl = avatarUrl;
            this.hasNewMessage = false;
        }

        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getAvatarUrl() {
            return avatarUrl;
        }
        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
        public boolean isHasNewMessage() {
            return hasNewMessage;
        }
        public void setHasNewMessage(boolean hasNewMessage) {
            this.hasNewMessage = hasNewMessage;
        }
    }
    /**
     * 生命周期（onCreateView） 创建视图
     * 当Fragment需要绘制其用户界面时调用
     * @param inflater LayoutInflater对象，用于将XML布局文件转换为View对象
     * @param container 父容器，Fragment的UI应附加到此容器
     * @param savedInstanceState 如果Fragment正在重新创建，则包含之前保存的状态
     * @return 返回Fragment的视图
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //将fragment_friends.xml布局文件转换为View对象
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        //初始化视图
        friendsRecyclerView = view.findViewById(R.id.friends_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);

        //初始化SharedPreferences
        sharePreferences = requireContext().getSharedPreferences("share_prefs", 0);

        //设置RecyclerView
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        friendsAdapter = new FriendsAdapter(friendList);
        friendsRecyclerView.setAdapter(friendsAdapter);

        //设置下拉刷新
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadFriendsData();
            swipeRefreshLayout.setRefreshing(false);
        });

        //加载朋友数据
        loadFriendsData();

        return view;
    }
    /**
     * 生命周期（onResume）恢复Fragment
     * 当Fragment对用户可见时调用
     * 用于更新UI状态
     */
    @Override
    public void onResume() {
        super.onResume();
        //每次进入页面时更新小红点状态
        updateMessageStatus();
    }
    /**
     * loadFriendsData() 加载朋友数据
     */
    private void loadFriendsData() {
        //清空列表
        friendList.clear();

        friendList.add(new Friend("1", "金豪", "https://lf3-static.bytednsdoc.com/obj/eden-cn/219eh7pbyphrnuvk/college_training_camp/avatars/avatar_54.jpg"));
        friendList.add(new Friend("2", "小金豪", "https://lf3-static.bytednsdoc.com/obj/eden-cn/219eh7pbyphrnuvk/college_training_camp/avatars/avatar_100.jpg"));
        friendList.add(new Friend("3", "大金豪", "https://lf3-static.bytednsdoc.com/obj/eden-cn/219eh7pbyphrnuvk/college_training_camp/avatars/avatar_171.jpg"));
        friendList.add(new Friend("4", "大小金豪", "https://lf3-static.bytednsdoc.com/obj/eden-cn/219eh7pbyphrnuvk/college_training_camp/avatars/avatar_75.jpg"));
        friendList.add(new Friend("5", "小大金豪", "https://lf3-static.bytednsdoc.com/obj/eden-cn/219eh7pbyphrnuvk/college_training_camp/avatars/avatar_23.jpg"));
        friendList.add(new Friend("6", "豪", "https://lf3-static.bytednsdoc.com/obj/eden-cn/219eh7pbyphrnuvk/college_training_camp/avatars/avatar_172.jpg"));

        //更新消息状态
        updateMessageStatus();

        //通知适配器更新
        if (friendsAdapter != null) {
            friendsAdapter.notifyDataSetChanged();
        }
    }
    /**
     * updateMessageStatus() 更新消息状态
     * 从SharedPreferences中读取每个朋友是否有新消息
     * 并更新friendList中对应的hasNewMessage字段
     */
    private void updateMessageStatus() {
        //从SharedPreferences检查每个朋友是否有新消息
        for (Friend friend : friendList) {
            boolean hasNewMessage = sharePreferences.getBoolean("new_msg_" + friend.getId(), false);
            friend.setHasNewMessage(hasNewMessage);
        }
        //更新UI，显示或隐藏小红点
        if (friendsAdapter != null) {
            friendsAdapter.notifyDataSetChanged();
        }
    }
}