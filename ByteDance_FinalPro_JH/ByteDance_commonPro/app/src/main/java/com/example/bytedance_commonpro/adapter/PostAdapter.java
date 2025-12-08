package com.example.bytedance_commonpro.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.bytedance_commonpro.R;
import com.example.bytedance_commonpro.model.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private List<Post> postList;
    private OnItemClickListener listener;
    private SharedPreferences preferences;
    private static final int AVATAR_SIZE = 60;

    /**
     * PostAdapter 帖子列表适配器 在RecyclerView中显示帖子卡片
     */
    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        this.preferences = context.getSharedPreferences("app_store", Context.MODE_PRIVATE);
    }
    /**
     * onCreateViewHolder 创建ViewHolder实例
     */
    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }
    /**
     * onBindViewHolder 绑定数据到ViewHolder
     */
    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.bind(post);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(post, position);
            }
        });
    }
    /**
     * getItemCount() 获取数据项数量
     */
    @Override
    public int getItemCount() {
        return postList.size();
    }
    /**
     * setOnItemClickListener 设置点击事件监听器
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    /**
     * updateData 过滤掉没有图片的帖子
     */
    public void updateData(List<Post> newPosts) {
        postList.clear();
        List<Post> filteredList = new ArrayList<>();
        for (Post post : newPosts) {
            if (post.getClips() != null && !post.getClips().isEmpty()) {
                filteredList.add(post);
            }
        }
        postList.addAll(filteredList);
        notifyDataSetChanged();
    }
    /**
     * addData 添加更多数据
     */
    public void addData(List<Post> morePosts) {
        int startPosition = postList.size();
        postList.addAll(morePosts);
        notifyItemRangeInserted(startPosition, morePosts.size());
    }
    /**
     * ViewHolder内部类，用于缓存视图组件
     */
    class PostViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCover, ivAvatar, ivLike;
        private TextView tvTitle, tvAuthor, tvLikeCount;
        private FrameLayout coverFrame;
        private boolean hasSetHeightFromData = false;
        /**
         * ViewHolder构造函数
         */
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews(itemView);
        }
        /**
         * initViews() 初始化所有视图组件
         */
        private void initViews(View itemView) {
            ivCover = itemView.findViewById(R.id.iv_cover);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            ivLike = itemView.findViewById(R.id.iv_like);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count);
            coverFrame = itemView.findViewById(R.id.cover_frame);
        }
        /**
         * bind()绑定帖子数据到ViewHolder
         */
        public void bind(Post post) {
            hasSetHeightFromData = false;

            // 先检查封面URL是否有效
            String coverUrl = post.getCoverUrlFromClips();
            if (coverUrl == null || coverUrl.isEmpty()) {
                //使用默认图片和默认高度
                ivCover.setImageResource(R.drawable.placeholder_image);
                setDefaultCoverHeight();
            } else {
                //先根据Post数据中的尺寸信息设置高度
                setHeightFromPostData(post);
                //然后加载图片
                loadCoverImage(coverUrl, post);
            }

            loadAvatarImage(post.getAvatarUrlFromAuthor());
            setTitleAndContent(post);
            setAuthorInfo(post);
            setupLikeButton(post);
        }
        /**
         * setHeightFromPostData()根据帖子数据中的尺寸信息设置封面高度
         */
        private void setHeightFromPostData(Post post) {
            int width = post.getFirstClipWidth();
            int height = post.getFirstClipHeight();


            if (width > 0 && height > 0) {
                float aspectRatio = (float) height / width;

                // 添加更合理的宽高比限制
                float minRatio = 0.75f;
                float maxRatio = 1.333f;
                aspectRatio = Math.max(minRatio, Math.min(aspectRatio, maxRatio));

                setCoverHeightByRatio(aspectRatio);
                hasSetHeightFromData = true;
            } else {
                setDefaultCoverHeight();
            }
        }
        /**
         * loadCoverImage()通过有效URL加载封面图片
         */
        private void loadCoverImage(String coverUrl, Post post) {
            //检查URL有效性
            if (coverUrl == null || coverUrl.isEmpty()) {
                ivCover.setImageResource(R.drawable.placeholder_image);
                setDefaultCoverHeight();
                return;
            }

            //设置最小高度
            ViewGroup.LayoutParams params = coverFrame.getLayoutParams();
            int minHeight = dpToPx(150);
            if (params.height < minHeight) {
                params.height = minHeight;
                coverFrame.setLayoutParams(params);
            }

            //创建圆角变换
            RequestOptions requestOptions = new RequestOptions()
                    .transform(new CenterCrop())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image);

            Glide.with(context)
                    .load(coverUrl)
                    .apply(requestOptions)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource,
                                                       boolean isFirstResource) {
                            //只有在没有从Post数据设置高度时才根据图片实际尺寸调整
                            if (!hasSetHeightFromData) {
                                calculateAndSetCoverHeight(resource);
                            } else {
                                //如果已经从Post数据设置了高度，这里可以添加验证逻辑
                                int actualWidth = resource.getIntrinsicWidth();
                                int actualHeight = resource.getIntrinsicHeight();
                                Log.d("ImageActualSize", "Actual - width: " + actualWidth + ", height: " + actualHeight +
                                        " | Post data - width: " + post.getFirstClipWidth() + ", height: " + post.getFirstClipHeight());
                            }
                            return false;
                        }
                    })
                    .into(ivCover);
        }
        /**
         * calculateAndSetCoverHeight()
         * 根据实际图片尺寸计算并设置封面高度
         */
        private void calculateAndSetCoverHeight(Drawable resource) {
            int imageWidth = resource.getIntrinsicWidth();
            int imageHeight = resource.getIntrinsicHeight();

            if (imageWidth <= 0 || imageHeight <= 0) {
                setDefaultCoverHeight();
                return;
            }

            float aspectRatio = (float) imageHeight / imageWidth;

            //计算宽高比并限制在合理范围内
            float minRatio = 0.75f;
            float maxRatio = 1.333f;
            aspectRatio = Math.max(minRatio, Math.min(aspectRatio, maxRatio));

            setCoverHeightByRatio(aspectRatio);
        }
        /**
         * setCoverHeightByRatio()
         * 根据宽高比设置封面高度,需要等待容器宽度测量完成后设置
         */
        private void setCoverHeightByRatio(float aspectRatio) {
            if (coverFrame.getWidth() > 0) {
                updateCoverHeight(coverFrame.getWidth(), aspectRatio);
            } else {
                coverFrame.getViewTreeObserver().addOnGlobalLayoutListener(
                        new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                coverFrame.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                int width = coverFrame.getWidth();
                                if (width > 0) {
                                    updateCoverHeight(width, aspectRatio);
                                } else {
                                    setDefaultCoverHeight();
                                }
                            }
                        });
            }
        }
        /**
         * updateCoverHeight()
         * 根据容器宽度和宽高比更新封面高度
         */
        private void updateCoverHeight(int containerWidth, float aspectRatio) {
            ViewGroup.LayoutParams params = coverFrame.getLayoutParams();
            int newHeight = (int) (containerWidth * aspectRatio);
            params.height = newHeight;
            coverFrame.setLayoutParams(params);
        }
        /**
         * setDefaultCoverHeight()
         * 设置默认的封面高度（4:3比例）
         */
        private void setDefaultCoverHeight() {
            if (coverFrame.getWidth() > 0) {
                //容器宽度已知，直接计算
                ViewGroup.LayoutParams params = coverFrame.getLayoutParams();
                params.height = (int) (coverFrame.getWidth() * 1.333f);
                coverFrame.setLayoutParams(params);
            } else {
                //容器宽度未知，延迟计算
                coverFrame.post(() -> {
                    if (coverFrame.getWidth() > 0) {
                        ViewGroup.LayoutParams params = coverFrame.getLayoutParams();
                        params.height = (int) (coverFrame.getWidth() * 1.333f);
                        coverFrame.setLayoutParams(params);
                    } else {
                        //如果仍然无法获取宽度，使用固定高度
                        ViewGroup.LayoutParams params = coverFrame.getLayoutParams();
                        params.height = dpToPx(180);
                        coverFrame.setLayoutParams(params);
                    }
                });
            }
        }
        /**
         * loadAvatarImage()
         * 加载作者头像
         */
        private void loadAvatarImage(String avatarUrl) {
            if (avatarUrl == null || avatarUrl.isEmpty()) {
                ivAvatar.setImageResource(R.drawable.avatar_placeholder);
                return;
            }

            Glide.with(context)
                    .load(avatarUrl)
                    .apply(new RequestOptions()
                            .circleCrop()
                            .override(AVATAR_SIZE, AVATAR_SIZE)
                            .placeholder(R.drawable.avatar_placeholder)
                            .error(R.drawable.avatar_placeholder))
                    .into(ivAvatar);
        }
        /**
         * setTitleAndContent()
         * 设置标题和内容显示文本
         * 优先显示标题
         */
        private void setTitleAndContent(Post post) {
            String displayText = post.getTitle();
            if (displayText == null || displayText.isEmpty()) {
                displayText = post.getContent();
                if (displayText != null && displayText.length() > 23) {
                    displayText = displayText.substring(0, 20) + "...";
                }
            }
            tvTitle.setText(displayText != null ? displayText : "");
        }
        /**
         * setAuthorInfo()
         * 设置作者信息和点赞数
         * 点赞数包括基础点赞数和当前用户的点赞状态
         */
        private void setAuthorInfo(Post post) {
            //获取或生成基础点赞数
            String baseCountKey = "base_like_count_" + post.getId();
            int baseLikeCount = preferences.getInt(baseCountKey, -1);
            if (baseLikeCount == -1) {
                //第一次显示该帖子，生成随机点赞数
                Random random = new Random();
                baseLikeCount = random.nextInt(9999);
                preferences.edit().putInt(baseCountKey, baseLikeCount).apply();
            }
            tvAuthor.setText(post.getAuthorName() != null ? post.getAuthorName() : "未知作者");
            //计算并显示总点赞数（基础数+当前用户是否点赞）
            boolean isLiked = preferences.getBoolean(getLikeKey(post.getId()), false);
            int displayLikeCount = baseLikeCount + (isLiked ? 1 : 0);
            tvLikeCount.setText(formatLikeCount(displayLikeCount));
        }
        /**
         * setupLikeButton()
         * 设置点赞按钮的状态和点击事件
         */
        private void setupLikeButton(Post post) {
            boolean isLiked = preferences.getBoolean(getLikeKey(post.getId()), false);
            updateLikeUI(isLiked);

            ivLike.setOnClickListener(v -> change_Like(post));
        }
        /**
         * change_Like()
         * 处理点赞按钮点击事件
         * 切换点赞状态，更新UI和存储
         */
        private void change_Like(Post post) {
            String likeKey = getLikeKey(post.getId());
            boolean currentLike = preferences.getBoolean(likeKey, false);
            boolean newLike = !currentLike;
            //更新点赞状态存储
            String baseCountKey = "base_like_count_" + post.getId();
            preferences.edit().putBoolean(likeKey, newLike).apply();

            int baseLikeCount = preferences.getInt(baseCountKey, 0);
            int displayLikeCount = baseLikeCount + (newLike ? 1 : 0);

            tvLikeCount.setText(formatLikeCount(displayLikeCount));
            updateLikeUI(newLike);
        }
        /**
         * updateLikeUI()
         * 更新点赞按钮UI
         */
        private void updateLikeUI(boolean isLiked) {
            int likeRes = isLiked ? R.drawable.ic_liked : R.drawable.ic_like;
            ivLike.setImageResource(likeRes);
        }
        /**
         * formatLikeCount()
         * 格式化点赞数显示
         * 规则：
         * - 小于5000：直接显示数字
         * - 5000-9999：显示为K（千）
         * - 10000以上：显示为W（万）
         */
        private String formatLikeCount(int count) {
            if (count < 5000) {
                return String.valueOf(count);
            } else if (count < 10000) {
                return String.format("%.1fK", count / 1000.0);
            } else {
                return String.format("%.1fW", count / 10000.0);
            }
        }
        /**
         * getLikeKey()
         * 生成点赞状态的存储键
         */
        private String getLikeKey(String postId) {

            return "like_" + postId;
        }
        /**
         * dpToPx()
         * dp转px工具方法
         */
        private int dpToPx(int dp) {
            float density = context.getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }
    }
    /**
     * 点击事件监听器接口
     * 用于回调帖子卡片的点击事件
     */
    public interface OnItemClickListener {
        void onItemClick(Post post, int position);
    }
}