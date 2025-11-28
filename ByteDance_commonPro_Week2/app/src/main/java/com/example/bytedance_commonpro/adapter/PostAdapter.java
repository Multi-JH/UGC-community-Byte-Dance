package com.example.bytedance_commonpro.adapter;


import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.bytedance_commonpro.R;
import com.example.bytedance_commonpro.model.Post;
import java.util.List;
import java.util.Random;

//定义 PostAdapter 继承 RecyclerView.Adapter 泛型指定其 ViewHolder 为内部类 PostViewHolder
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private List<Post> postList;//帖子的数据列表
    private OnItemClickListener listener;//点击事件的回调接口
    private SharedPreferences preferences;//用于持久化点赞状态（本地存储）

    // 图片尺寸配置
    private static final int COVER_WIDTH = 300;
    private static final int COVER_HEIGHT = 400;
    private static final int AVATAR_SIZE = 60;
    private static final int CORNER_RADIUS = 12; // 圆角半径

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        this.preferences = context.getSharedPreferences("app_store", Context.MODE_PRIVATE);
        //初始化一个SharedPreferences对象，用于在Android应用中存储简单的键值对数据。
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.bind(post);
        //这里只显示帖子列表以及处理点赞等局部交互，而不处理帖子详情页面的跳转，使用外部回调
        //如果不外部回调 会和跳转页面绑定
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(post, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }
    //为外界提供点击回调的方法
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<Post> newPosts) {
        postList.clear();
        postList.addAll(newPosts);
        notifyDataSetChanged();
    }

    public void addData(List<Post> morePosts) {
        int startPosition = postList.size();
        postList.addAll(morePosts);
        notifyItemRangeInserted(startPosition, morePosts.size());
    }

    // ViewHolder 内部类
    class PostViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCover, ivAvatar, ivLike;
        private TextView tvTitle, tvAuthor, tvLikeCount;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews(itemView);
        }

        private void initViews(View itemView) {
            ivCover = itemView.findViewById(R.id.iv_cover);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            ivLike = itemView.findViewById(R.id.iv_like);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count);
        }

        public void bind(Post post) {
            loadCoverImage(post.getCoverUrlFromClips());
            loadAvatarImage(post.getAvatarUrlFromAuthor());
            setTitleAndContent(post);
            setAuthorInfo(post);
            setupLikeButton(post);
        }

        private void loadCoverImage(String coverUrl) {
            // 创建圆角变换
            RequestOptions requestOptions = new RequestOptions()
                    .transform(new CenterCrop(), new RoundedCorners(dpToPx(CORNER_RADIUS)))
                    .override(COVER_WIDTH, COVER_HEIGHT)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image);

            Glide.with(context)
                    .load(coverUrl)
                    .apply(requestOptions)
                    .into(ivCover);
        }

        private void loadAvatarImage(String avatarUrl) {
            Glide.with(context)
                    .load(avatarUrl)
                    .apply(new RequestOptions()
                            .circleCrop()
                            .override(AVATAR_SIZE, AVATAR_SIZE)
                            .placeholder(R.drawable.avatar_placeholder)
                            .error(R.drawable.avatar_placeholder))
                    .into(ivAvatar);
        }

        private void setTitleAndContent(Post post) {
            // 优先显示标题，没有标题则显示内容前部分
            String displayText = post.getTitle();
            if (displayText == null || displayText.isEmpty()) {
                displayText = post.getContent();
                // 内容截断处理
                if (displayText != null && displayText.length() > 50) {
                    displayText = displayText.substring(0, 50) + "...";
                }
            }
            tvTitle.setText(displayText != null ? displayText : "");
        }

        private void setAuthorInfo(Post post) {
            String baseCountKey = "base_like_count_" + post.getId();
            // 1. 获取或生成基础点赞数（只生成一次！）
            int baseLikeCount = preferences.getInt(baseCountKey, -1);
            if (baseLikeCount == -1) {
                // 首次出现这个帖子：生成 50~500 的随机数
                Random random = new Random();
                baseLikeCount = random.nextInt(9999); // 50 ~ 500 inclusive
                preferences.edit().putInt(baseCountKey, baseLikeCount).apply();
            }
            tvAuthor.setText(post.getAuthorName() != null ? post.getAuthorName() : "未知作者");
            boolean isLiked = preferences.getBoolean(getLikeKey(post.getId()), false);
            int displayLikeCount = baseLikeCount + (isLiked ? 1 : 0);
            tvLikeCount.setText(formatLikeCount(displayLikeCount));
        }
        //初始化点赞的按钮
        private void setupLikeButton(Post post) {
            // 从本地存储读取点赞状态
            boolean isLiked = preferences.getBoolean(getLikeKey(post.getId()), false);
            updateLikeUI(isLiked);

            // 设置点赞点击监听
            ivLike.setOnClickListener(v -> change_Like(post));
        }

        private void change_Like(Post post) {
            String likeKey = getLikeKey(post.getId());
            boolean currentLike = preferences.getBoolean(likeKey, false);
            boolean newLike = !currentLike;

            String baseCountKey = "base_like_count_" + post.getId();
            preferences.edit().putBoolean(likeKey, newLike).apply();

            // 获取基础点赞数（已存在，无需再生成）
            int baseLikeCount = preferences.getInt(baseCountKey, 0);
            // 计算显示值
            int displayLikeCount = baseLikeCount + (newLike ? 1 : 0);

            // 更新 UI
            tvLikeCount.setText(formatLikeCount(displayLikeCount));
            updateLikeUI(newLike);
        }

        private void updateLikeUI(boolean isLiked) {
            int likeRes = isLiked ? R.drawable.ic_liked : R.drawable.ic_like;
            ivLike.setImageResource(likeRes);
        }

        private String formatLikeCount(int count) {
            if (count < 5000) {
                return String.valueOf(count);
            } else if (count < 10000) {
                return String.format("%.1fK", count / 1000.0);
            } else {
                return String.format("%.1fW", count / 10000.0);
            }
        }
        //生成点赞状态的存储键
        private String getLikeKey(String postId) {
            return "like_" + postId;
        }

        private int dpToPx(int dp) {
            float density = context.getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }
    }

    //点击事件接口，然后在HomeFragment中实现这个接口然后重写方法，保存外部所存入的对象引用
    //listen存的就不是null而是Fragment 中创建的匿名对象
    //执行的是 Fragment 里重写的 onItemClick 方法体
    //为了解耦，通用性强，将业务逻辑交给外部
    public interface OnItemClickListener {
        void onItemClick(Post post, int position);
    }
}