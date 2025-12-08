package com.example.bytedance_commonpro.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bytedance_commonpro.R;
import com.example.bytedance_commonpro.fragment.FriendsFragment;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {
    private List<FriendsFragment.Friend> friendList;
    private Context context;
    public FriendsAdapter(List<FriendsFragment.Friend> friendList) {
        this.friendList = friendList;
    }
    /**
     * onCreateViewHolder() 获取上下文，并加载item_friend.xml布局
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(view);
    }
    /**
     * onBindViewHolder() 获取当前位置的数据并进行数据绑定
     * 添加点击事件，实现显示/隐藏小红点并持久化
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendsFragment.Friend friend = friendList.get(position);
        //设置朋友姓名
        holder.tvName.setText(friend.getName());
        //加载头像
        Glide.with(context)
                .load(friend.getAvatarUrl())
                .placeholder(R.drawable.avatar_placeholder)
                .circleCrop()
                .into(holder.ivAvatar);
        //显示/隐藏小红点
        holder.ivRedDot.setVisibility(friend.isHasNewMessage() ? View.VISIBLE : View.GONE);

        //点击事件，清除小红点
        holder.itemView.setOnClickListener(v -> {
            //清除小红点
            friend.setHasNewMessage(false);
            notifyItemChanged(position);

            //保存到SharedPreferences
            context.getSharedPreferences("share_prefs", 0)
                    .edit()
                    .putBoolean("new_msg_" + friend.getId(), false)
                    .apply();
        });
    }
    /**
     * getItemCount() 返回数据总数
     */
    @Override
    public int getItemCount() {
        return friendList.size();
    }
    /**
     * ViewHolder 静态内部类：缓存视图引用，避免频繁调用findViewById
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        ImageView ivRedDot;
        TextView tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            ivRedDot = itemView.findViewById(R.id.iv_red_dot);
            tvName = itemView.findViewById(R.id.tv_name);
        }
    }
}