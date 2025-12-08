package com.example.bytedance_commonpro.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.bytedance_commonpro.R;
import com.example.bytedance_commonpro.model.Clip;

import java.util.List;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {
    /**
     * LoadState枚举，定义4种图片加载状态
     */
    public enum LoadState {
        LOADING,      //加载中
        SUCCESS,      //加载成功
        ERROR,        //加载失败
        IDLE          //空闲状态
    }

    private List<Clip> clips;
    private LayoutInflater inflater;
    private OnRetryClickListener onRetryClickListener;
    private LoadState[] loadStates;
    private Context context;
    /**
     * 构造函数：初始化成员变量，并将每个Clip初始化加载状态为IDLE
     */
    public ImagePagerAdapter(Context context, List<Clip> clips) {
        this.context = context;
        this.clips = clips;
        this.inflater = LayoutInflater.from(context);
        this.loadStates = new LoadState[clips.size()];

        //初始化所有状态为空闲
        for (int i = 0; i < loadStates.length; i++) {
            loadStates[i] = LoadState.IDLE;
        }
    }
    /**
     * setLoadState()设置指定位置的加载状态
     */
    public void setLoadState(int position, LoadState state) {
        if (position >= 0 && position < loadStates.length) {
            loadStates[position] = state;
            notifyItemChanged(position);
        }
    }
    /**
     * getLoadState() 获取指定位置的加载状态
     */
    public LoadState getLoadState(int position) {
        if (position >= 0 && position < loadStates.length) {
            return loadStates[position];
        }
        return LoadState.IDLE;
    }
    /**
     * setOnRetryClickListener() 设置重试按钮的点击监听器
     */
    public void setOnRetryClickListener(OnRetryClickListener listener) {
        this.onRetryClickListener = listener;
    }
    /**
     * onCreateViewHolder()
     * 创建并返回ImageViewHolder实例
     * 加载item_image_pager.xml布局
     */
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_image_pager, parent, false);
        return new ImageViewHolder(view);
    }
    /**
     * onBindViewHolder()
     * 获取当前位置的Clip数据和加载状态
     * 调用ViewHolder的bind()方法绑定数据
     */
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Clip clip = clips.get(position);
        LoadState state = loadStates[position];
        holder.bind(clip, position, state);
    }
    /**
     * getItemCount() 返回数据项总数
     */
    @Override
    public int getItemCount() {
        return clips != null ? clips.size() : 0;
    }
    class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        LinearLayout loadingState;
        LinearLayout errorState;
        TextView tvErrorMessage;
        TextView tvRetry;
        FrameLayout rootLayout;
        /**
         * ImageViewHolder 内部类
         * 绑定所有视图组件
         * 设置重试按钮点击事件
         */
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            //加载状态布局
            loadingState = itemView.findViewById(R.id.loading_state);
            errorState = itemView.findViewById(R.id.error_state);
            tvErrorMessage = itemView.findViewById(R.id.tv_error_message);
            tvRetry = itemView.findViewById(R.id.tv_retry);
            rootLayout = itemView.findViewById(R.id.root_layout);

            //设置重试按钮点击事件
            tvRetry.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onRetryClickListener != null) {
                    onRetryClickListener.onRetryClick(position);
                }
            });
        }
        /**
         * bind()
         * 清除旧图片
         * 根据加载状态显示不同的界面
         */
        public void bind(Clip clip, int position, LoadState state) {

            //清除之前的图片，避免复用问题
            ivImage.setImageDrawable(null);


            //根据状态显示不同的UI
            switch (state) {
                case LOADING:
                    showLoadingState();
                    //开始加载图片
                    loadImage(clip, position);
                    break;

                case SUCCESS:
                    showSuccessState();
                    //如果图片没有加载成功，重新加载
                    if (ivImage.getDrawable() == null) {
                        loadImage(clip, position);
                    }
                    break;

                case ERROR:
                    showErrorState();
                    break;

                case IDLE:
                    //空闲状态，开始加载
                    loadStates[position] = LoadState.LOADING;
                    showLoadingState();
                    loadImage(clip, position);
                    break;
            }
        }
        /**
         * showLoadingState()显示加载状态
         */
        private void showLoadingState() {
            loadingState.setVisibility(View.VISIBLE);
            errorState.setVisibility(View.GONE);
            ivImage.setVisibility(View.GONE);

        }
        /**
         * showSuccessState()显示成功状态
         */
        private void showSuccessState() {
            loadingState.setVisibility(View.GONE);
            errorState.setVisibility(View.GONE);
            ivImage.setVisibility(View.VISIBLE);
        }
        /**
         * showErrorState()显示Error状态
         */
        private void showErrorState() {
            loadingState.setVisibility(View.GONE);
            errorState.setVisibility(View.VISIBLE);
            ivImage.setVisibility(View.GONE);
        }
        /**
         * loadImage()加载图片
         */
        private void loadImage(Clip clip, int position) {
            if (clip == null || clip.getUrl() == null || clip.getUrl().isEmpty()) {
                tvErrorMessage.setText("图片链接无效");
                setLoadState(position, LoadState.ERROR);
                return;
            }

            final int currentPosition = position;

            RequestOptions requestOptions = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image);

            if (clip.getWidth() > 0 && clip.getHeight() > 0) {
                //计算合适的图片尺寸，避免加载过大图片
                int targetWidth = itemView.getContext().getResources().getDisplayMetrics().widthPixels;
                int targetHeight = (int) (targetWidth * ((float) clip.getHeight() / clip.getWidth()));
                requestOptions = requestOptions.override(targetWidth, targetHeight);
            }

            Glide.with(itemView.getContext())
                    .load(clip.getUrl())
                    .apply(requestOptions)
                    .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<android.graphics.drawable.Drawable> target,
                                                    boolean isFirstResource) {
                            itemView.post(() -> {
                                tvErrorMessage.setText("加载失败，点击重试");
                                setLoadState(currentPosition, LoadState.ERROR);
                            });
                            return false; // 返回false让Glide显示error占位图
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource,
                                                       Object model,
                                                       Target<android.graphics.drawable.Drawable> target,
                                                       DataSource dataSource,
                                                       boolean isFirstResource) {
                            itemView.post(() -> {
                                setLoadState(currentPosition, LoadState.SUCCESS);
                            });
                            return false; // 返回false让Glide显示图片
                        }
                    })
                    .into(ivImage);
        }
    }
    /**
     * OnRetryClickListener回调接口，处理重试点击事件
     */
    public interface OnRetryClickListener {
        void onRetryClick(int position);
        //当图片加载失败时，会显示错误状态和重试按钮。
        //当用户点击重试按钮时，会通知DetailActivity重新加载该位置的内容。
    }
}