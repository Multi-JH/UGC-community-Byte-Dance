package com.example.bytedance_commonpro.adapter;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.bytedance_commonpro.R;
import com.example.bytedance_commonpro.model.Work;

import java.util.List;

/**
 * 作品列表适配器，用于在RecyclerView中显示作品缩略图
 * 适用于个人主页、话题页等需要展示作品列表的场景
 */
public class WorksAdapter extends RecyclerView.Adapter<WorksAdapter.ViewHolder> {

    private List<Work> workList;
    private OnItemClickListener listener;

    /**
     * 点击事件监听器接口
     * 用于回调作品项目的点击事件
     */
    public interface OnItemClickListener {
        void onItemClick(Work work);
    }
    /**
     * WorksAdapter()
     * 构造函数
     */
    public WorksAdapter(List<Work> workList, OnItemClickListener listener) {
        this.workList = workList;
        this.listener = listener;
    }
    /**
     * onCreateViewHolder()
     * 创建ViewHolder实例
     * 当RecyclerView需要新的视图项时调用
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_work, parent, false);
        return new ViewHolder(view);
    }
    /**
     * onBindViewHolder()
     * 绑定数据到ViewHolder
     * 将指定位置的作品数据绑定到ViewHolder的视图上
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Work work = workList.get(position);

        // 添加日志
        Log.d("WorksAdapter1", "绑定数据 position: " + position +
                ", 标题: " + work.getTitle() +
                ", 封面URL: " + work.getCoverUrl());

        //加载封面图片
        Glide.with(holder.itemView.getContext())
                .load(work.getCoverUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.avatar_placeholder) // 添加错误占位符
                .centerCrop()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        Log.e("WorksAdapter", "图片加载失败: " + work.getCoverUrl(), e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        Log.d("WorksAdapter", "图片加载成功: " + work.getCoverUrl());
                        return false;
                    }
                })
                .into(holder.ivCover);

        //设置点赞数
        holder.tvLikeCount.setText(String.valueOf(work.getLikeCount()));

        //设置标题
        holder.tvTitle.setText(work.getTitle());

        //设置整个item的点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(work);
            }
        });
    }
    /**
     * getItemCount()
     * 获取数据项数量
     */
    @Override
    public int getItemCount() {
        return workList.size();
    }
    /**
     * updateData()
     * 用于刷新整个作品列表
     */
    public void updateData(List<Work> newWorkList) {
        workList.clear();
        workList.addAll(newWorkList);
        notifyDataSetChanged();
    }
    /**
     * ViewHolder内部类
     * 用于缓存视图组件，避免重复查找提高性能
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvLikeCount;
        TextView tvTitle;
        /**
         * ViewHolder构造函数
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count);
            tvTitle = itemView.findViewById(R.id.tv_title);
        }
    }
}
