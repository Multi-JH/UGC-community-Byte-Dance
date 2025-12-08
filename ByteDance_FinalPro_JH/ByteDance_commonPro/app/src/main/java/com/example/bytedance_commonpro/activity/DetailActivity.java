package com.example.bytedance_commonpro.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.bytedance_commonpro.R;
import com.example.bytedance_commonpro.adapter.ImagePagerAdapter;
import com.example.bytedance_commonpro.fragment.FriendsFragment;
import com.example.bytedance_commonpro.manager.MusicManager;
import com.example.bytedance_commonpro.model.Hashtag;
import com.example.bytedance_commonpro.model.Music;
import com.example.bytedance_commonpro.model.Post;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class DetailActivity extends AppCompatActivity {

    private Post post;
    private SharedPreferences preferences;
    private View contentView;


    private ImageView btnBack;
    private ImageView authorAvatar;
    private TextView tvAuthorName;
    private TextView btnFollow;

    private ViewPager2 viewPager;
    private LinearLayout progressContainer;

    private TextView tvTitle;
    private TextView tvContent;
    private TextView tvCreateTime;
    private LinearLayout contentTouchArea;

    private Bitmap homeScreenshot;
    private ImageView backgroundView;
    private float startX;


    // 底部交互区
    private ImageView ivLike;
    private TextView tvLikeCount;
    private ImageView ivComment;
    private ImageView ivCollect;
    private TextView tvCollectCount;
    private ImageView ivShare;
    private TextView tvShareCount;
    private ImageView ivMute;

    private View contentContainer;
    private boolean isAnimating = false;
    private boolean isDragging = false;
    private float dragProgress = 0f;
    private Rect startRect;

    private MusicManager musicManager;

    private Handler autoPlayHandler = new Handler();
    private Runnable autoPlayRunnable;
    private static final long AUTO_PLAY_INTERVAL = 3000;
    private boolean isAutoPlaying = true;
    private boolean isInterruptedByUser = false;

    private SharedPreferences sharePreferences;
    private List<FriendsFragment.Friend> shareFriendList = new ArrayList<>();
    private AlertDialog shareDialog;
    private boolean[] selectedFriends;
    private int currentShareCount;
    private boolean isCollected = false;
    private int currentCollectCount;

    /**
     * 生命周期（onCreate）
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        //获取Post数据以及视图的位置信息
        post = getIntent().getParcelableExtra("post");
        startRect = getIntent().getParcelableExtra("start_rect");
        //接收截图
        byte[] byteArray = getIntent().getByteArrayExtra("home_screenshot");
        if (byteArray != null) {
            homeScreenshot = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        }
        preferences = getSharedPreferences("app_store", MODE_PRIVATE);
        sharePreferences = getSharedPreferences("share_prefs", MODE_PRIVATE);
        musicManager = MusicManager.getInstance(this);

        initViews();
        setupBackPressedCallback();
        if (homeScreenshot != null) {
            backgroundView.setImageBitmap(homeScreenshot);
        }
        loadShareCount();
        loadCollectCount();
        loadCollectState();
        setupTopAuthorArea();
        setupImagePager();
        setupContentArea();
        setupMusic();
        setupBottomInteraction();
        setupTransitionAnimation();
        setupSwipeToDismiss();

    }
    /**
     * 生命周期（onResume）
     */
    @Override
    protected void onResume() {
        super.onResume();
        //恢复音乐播放（如果非静音状态）
        if (!musicManager.isMuted() && post != null && post.getMusic() != null) {
            musicManager.play();
        }
        // 恢复自动轮播（如果之前是开启状态且未被用户打断）
        if (isAutoPlaying && !isInterruptedByUser) {
            startAutoPlay();
        }
    }
    /**
     * 生命周期（onPause）
     */
    @Override
    protected void onPause() {
        super.onPause();
        musicManager.pause();
        stopAutoPlay();
    }
    /**
     * 生命周期（onDestroy）
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //清理轮播相关的Handler
        stopAutoPlay();
        if (autoPlayHandler != null) {
            autoPlayHandler.removeCallbacksAndMessages(null);
        }
        if (homeScreenshot != null && !homeScreenshot.isRecycled()) {
            homeScreenshot.recycle();
            homeScreenshot = null;
        }
    }
    /**
     * initViews()初始化所有视图组件
     */
    private void initViews() {

        contentView = findViewById(R.id.content);
        contentContainer = findViewById(R.id.content_container);
        contentTouchArea = findViewById(R.id.content_touch_area);
        backgroundView = findViewById(R.id.background_view);

        //顶部作者区
        btnBack = findViewById(R.id.iv_btn_back);
        authorAvatar = findViewById(R.id.iv_author_avatar);
        tvAuthorName = findViewById(R.id.tv_author_name);
        btnFollow = findViewById(R.id.tv_btn_follow);

        //图片区域
        viewPager = findViewById(R.id.view_pager);
        progressContainer = findViewById(R.id.progress_container);

        //内容区域
        tvTitle = findViewById(R.id.detail_tv_title);
        tvContent = findViewById(R.id.detail_tv_content);
        tvCreateTime = findViewById(R.id.detail_tv_create_time);

        //底部交互区
        ivLike = findViewById(R.id.detail_iv_like);
        tvLikeCount = findViewById(R.id.detail_tv_like);
        ivComment = findViewById(R.id.iv_comment);
        ivCollect = findViewById(R.id.iv_collect);
        tvCollectCount = findViewById(R.id.tv_collect_count);
        ivShare = findViewById(R.id.iv_share);
        tvShareCount = findViewById(R.id.tv_share_count);
        ivMute = findViewById(R.id.iv_mute);
    }
    /**
     * loadShareCount()加载分享数
     */
    private void loadShareCount() {
        //从SharedPreferences加载分享数
        String shareCountKey = "share_count_" + post.getId();
        //检查是否是第一次加载（即没有保存过分享数）
        boolean hasSavedCount = sharePreferences.contains(shareCountKey);

        if (!hasSavedCount) {
            //第一次加载，生成符合真实分布的随机分享数并且保存
            currentShareCount = generateRealisticRandomShareCount();
            saveShareCount();
        } else {
            // 已经有保存的分享数，直接加载
            currentShareCount = sharePreferences.getInt(shareCountKey, 0);
        }
        // 更新UI显示
        tvShareCount.setText(String.valueOf(currentShareCount));
    }
    /**
     * generateRealisticRandomShareCount()生成符合真实社交媒体分布的随机分享数
     */
    private int generateRealisticRandomShareCount() {
        Random random = new Random();
        double r = random.nextDouble();
        if (r < 0.7) {
            return random.nextInt(100) + 1;
        } else if (r < 0.9) {
            return random.nextInt(400) + 100;
        } else if (r < 0.98) {
            return random.nextInt(1500) + 500;
        } else {
            return random.nextInt(8000) + 2000;
        }
    }
    /**
     * saveShareCount()添加保存持久化分享数
     */
    private void saveShareCount() {
        String shareCountKey = "share_count_" + post.getId();
        sharePreferences.edit()
                .putInt(shareCountKey, currentShareCount)
                .apply();
    }
    /**
     * showShareDialog()显示分享框
     */
    private void showShareDialog() {
        //加载朋友数据
        loadShareFriendsData();
        //创建朋友列表适配器
        String[] friendNames = new String[shareFriendList.size()];
        for (int i = 0; i < shareFriendList.size(); i++) {
            friendNames[i] = shareFriendList.get(i).getName();
        }
        //初始化选中状态数组
        selectedFriends = new boolean[shareFriendList.size()];
        for (int i = 0; i < selectedFriends.length; i++) {
            selectedFriends[i] = false;
        }
        //创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("分享给朋友");
        //设置多选列表
        builder.setMultiChoiceItems(friendNames, selectedFriends,
                (dialog, which, isChecked) -> {
                    selectedFriends[which] = isChecked;
                });
        //设置确定按钮
        builder.setPositiveButton("分享", (dialog, which) -> {
            performShareToFriends();
        });
        //设置取消按钮
        builder.setNegativeButton("取消", null);
        // 显示对话框
        shareDialog = builder.create();
        shareDialog.show();
    }
    /**
     * loadShareFriendsData()加载朋友数据
     */
    private void loadShareFriendsData() {
        shareFriendList.clear();

        // 模拟朋友数据（应该与FriendsFragment中的数据一致）
        shareFriendList.add(new FriendsFragment.Friend("1", "金豪", "https://lf3-static.bytednsdoc.com/obj/eden-cn/219eh7pbyphrnuvk/college_training_camp/avatars/avatar_54.jpg"));
        shareFriendList.add(new FriendsFragment.Friend("2", "小金豪", "https://lf3-static.bytednsdoc.com/obj/eden-cn/219eh7pbyphrnuvk/college_training_camp/avatars/avatar_100.jpg"));
        shareFriendList.add(new FriendsFragment.Friend("3", "大金豪", "https://lf3-static.bytednsdoc.com/obj/eden-cn/219eh7pbyphrnuvk/college_training_camp/avatars/avatar_171.jpg"));
        shareFriendList.add(new FriendsFragment.Friend("4", "大小金豪", "https://lf3-static.bytednsdoc.com/obj/eden-cn/219eh7pbyphrnuvk/college_training_camp/avatars/avatar_75.jpg"));
        shareFriendList.add(new FriendsFragment.Friend("5", "小大金豪", "https://lf3-static.bytednsdoc.com/obj/eden-cn/219eh7pbyphrnuvk/college_training_camp/avatars/avatar_23.jpg"));
        shareFriendList.add(new FriendsFragment.Friend("6", "豪", "https://lf3-static.bytednsdoc.com/obj/eden-cn/219eh7pbyphrnuvk/college_training_camp/avatars/avatar_172.jpg"));
        // 检查是否有新消息
        for (FriendsFragment.Friend friend : shareFriendList) {
            boolean hasNewMessage = sharePreferences.getBoolean("new_msg_" + friend.getId(), false);
            friend.setHasNewMessage(hasNewMessage);
        }
    }
    /**
     * performShareToFriends()分享确认方法并增加分享数持久化
     */
    private void performShareToFriends() {
        int sharedCount = 0;
        for (int i = 0; i < selectedFriends.length; i++) {
            if (selectedFriends[i]) {
                FriendsFragment.Friend friend = shareFriendList.get(i);
                //执行分享逻辑
                shareToFriend(friend);
                sharedCount++;
            }
        }
        // 显示结果
        if (sharedCount > 0) {
            Toast.makeText(this, "已分享给" + sharedCount + "个朋友", Toast.LENGTH_SHORT).show();
            //更新分享计数
            currentShareCount += sharedCount;
            //持久化保存分享数
            saveShareCount();
            //更新UI显示
            tvShareCount.setText(String.valueOf(currentShareCount));
        }
        //关闭对话框
        if (shareDialog != null && shareDialog.isShowing()) {
            shareDialog.dismiss();
        }
    }
    /**
     * shareToFriend()分享单个朋友的方法并添加更多持久化信息
     */
    private void shareToFriend(FriendsFragment.Friend friend) {
        SharedPreferences.Editor editor = sharePreferences.edit();
        // 标记该朋友有新消息
        editor.putBoolean("new_msg_" + friend.getId(), true);
        //保存帖子分享记录，记录分享时间和分享人
        String shareRecordKey = "share_record_" + post.getId();
        String shareRecords = sharePreferences.getString(shareRecordKey, "");
        //添加新的分享记录：格式为 朋友ID:时间戳
        long currentTime = System.currentTimeMillis();
        shareRecords += friend.getId() + ":" + currentTime + ",";
        editor.putString(shareRecordKey, shareRecords);
        // 记录每个朋友分享过的帖子
        String friendSharesKey = "friend_shares_" + friend.getId();
        String friendShares = sharePreferences.getString(friendSharesKey, "");
        if (!friendShares.contains(post.getId())) {
            friendShares += post.getId() + ",";
            editor.putString(friendSharesKey, friendShares);
        }
        editor.apply();
    }
    /**
     * setupMusic添加音乐设置方法
     */
    private void setupMusic() {
        if (post != null && post.getMusic() != null) {
            Music music = post.getMusic();
            musicManager.playMusic(music);
            setupMuteButton();
        } else {
            //如果没有音乐，隐藏静音按钮
            if (ivMute != null) {
                ivMute.setVisibility(View.GONE);
            }
        }
    }
    /**
     * setupMuteButton更新静音按钮设置方法
     */
    private void setupMuteButton() {
        //更新静音按钮图标
        updateMuteButtonUI();
        //设置静音按钮点击事件
        ivMute.setOnClickListener(v -> {
            musicManager.change_Mute();
            isAutoPlaying = !isAutoPlaying;

            //如果之前被用户手动打断，现在静音按钮为取消静音按钮则恢复自动轮播
            if (isInterruptedByUser&&!musicManager.isMuted() ) {
                isInterruptedByUser = false;
                isAutoPlaying = true;
                startAutoPlay();
            } else {
                isAutoPlaying = !isAutoPlaying;
                if (isAutoPlaying) {
                    startAutoPlay();
                } else {
                    stopAutoPlay();
                }
            }
            updateMuteButtonUI();
        });
    }
    /**
     * updateMuteButtonUI更新静音按钮UI
     */
    private void updateMuteButtonUI() {
        if (musicManager.isMuted()  ) {
            ivMute.setImageResource(R.drawable.ic_mute);
        } else {
            ivMute.setImageResource(R.drawable.ic_unmute);
        }
    }
    /**
     * setupAutoPlay设置自动轮播
     */
    private void setupAutoPlay() {
        if (post.getClips() == null || post.getClips().size() <= 1) {
            return;
        }
        autoPlayRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAutoPlaying && viewPager != null && !isInterruptedByUser) {
                    int currentItem = viewPager.getCurrentItem();
                    int totalItems = viewPager.getAdapter().getItemCount();
                    int nextItem = (currentItem + 1) % totalItems;
                    viewPager.setCurrentItem(nextItem, true);
                }
                autoPlayHandler.postDelayed(this, AUTO_PLAY_INTERVAL);
            }
        };
        startAutoPlay();
    }
    /**
     * startAutoPlay开启自动轮播
     */
    private void startAutoPlay() {
        if (autoPlayRunnable != null && isAutoPlaying && !isInterruptedByUser) {
            autoPlayHandler.removeCallbacks(autoPlayRunnable);
            autoPlayHandler.postDelayed(autoPlayRunnable, AUTO_PLAY_INTERVAL);
        }
    }
    /**
     * stopAutoPlay暂停自动轮播
     */
    private void stopAutoPlay() {
        if (autoPlayHandler != null && autoPlayRunnable != null) {
            autoPlayHandler.removeCallbacks(autoPlayRunnable);
        }
    }
    /**
     * setupImagePager设置横滑容器（图片轮播部分）
     */
    private void setupImagePager() {
        if (post.getClips() == null || post.getClips().isEmpty()) {
            viewPager.setVisibility(View.GONE);
            progressContainer.setVisibility(View.GONE);
            return;
        }
        ImagePagerAdapter adapter = new ImagePagerAdapter(this, post.getClips());
        viewPager.setAdapter(adapter);
        //设置初始加载状态
        for (int i = 0; i < post.getClips().size(); i++) {
            adapter.setLoadState(i, ImagePagerAdapter.LoadState.LOADING);
        }
        //设置重试点击监听
        adapter.setOnRetryClickListener(position -> {
            //重新加载指定位置的图片
            retryLoadImage(position);
        });
        setupProgressIndicator();

        // 设置页面切换监听
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateProgressIndicator(position);
                if (isAutoPlaying && !isInterruptedByUser) {
                    restartAutoPlay();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    if (isAutoPlaying && !isInterruptedByUser) {
                        isInterruptedByUser = true;
                        stopAutoPlay();
                        updateMuteButtonUI();
                    }
                }
            }
        });
        setupAutoPlay();
    }
    /**
     * setupProgressIndicator()设置横滑容器中的进度条
     */
    private void setupProgressIndicator() {
        progressContainer.removeAllViews();
        int clipCount = post.getClips().size();
        if (clipCount <= 1) {
            progressContainer.setVisibility(View.GONE);
            return;
        }
        //如果图片大于1张，则显示进度条
        progressContainer.setVisibility(View.VISIBLE);
        for (int i = 0; i < clipCount; i++) {
            View indicator = new View(this);
            //设置进度条的布局，平均分配容器宽度
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 8, 1);
            params.setMargins(i == 0 ? 0 : 12, 0, 0, 0);
            indicator.setLayoutParams(params);
            indicator.setBackgroundResource(i == 0 ?
                    R.drawable.bg_progress_indicator_active : R.drawable.bg_progress_indicator);
            progressContainer.addView(indicator);
        }
    }
    /**
     * updateProgressIndicator()更新进度条的状态
     */
    private void updateProgressIndicator(int position) {
        int childCount = progressContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View indicator = progressContainer.getChildAt(i);
            if (i == position) {
                indicator.setBackgroundResource(R.drawable.bg_progress_indicator_active);
            } else {
                indicator.setBackgroundResource(R.drawable.bg_progress_indicator);
            }
        }
    }
    /**
     * retryLoadImage设置为加载中状态，触发重新加载
     */
    private void retryLoadImage(int position) {
        ImagePagerAdapter adapter = (ImagePagerAdapter) viewPager.getAdapter();
        if (adapter != null) {
            adapter.setLoadState(position, ImagePagerAdapter.LoadState.LOADING);
        }
    }
    /**
     * restartAutoPlay重启自动轮播
     */
    private void restartAutoPlay() {
        if (isAutoPlaying && !isInterruptedByUser) {
            stopAutoPlay();
            startAutoPlay();
        }
    }
    /**
     * setupTransitionAnimation设置双列瀑布流进入详情页的过渡动画
     */
    private void setupTransitionAnimation() {
        if (startRect != null) {
            ViewPager2 viewPager = findViewById(R.id.view_pager);
            ViewGroup.LayoutParams params = viewPager.getLayoutParams();
            params.width = startRect.width();
            params.height = startRect.height();
            viewPager.setLayoutParams(params);

            //将ViewPager2移动到屏幕的之前帖子图片的原本位置
            viewPager.setX(startRect.left - contentView.getLeft());
            viewPager.setY(startRect.top - contentView.getTop());

            //延迟执行展开动画
            viewPager.postDelayed(() -> {
                animateImageExpand();
            }, 50);
        }
    }
    /**
     * aspectRatio()获得图片的宽高比，并对其进行约束
     */
    private float aspectRatio() {
        int width = post.getFirstClipWidth();
        int height = post.getFirstClipHeight();

        if (width > 0 && height > 0) {
            float aspectRatio = (float) width / height;

            // 添加更合理的宽高比限制
            float minRatio = 0.75f;
            float maxRatio = 1.777f;
            return Math.max(minRatio, Math.min(aspectRatio, maxRatio));


        }else{
            return 1.333f;
        }
    }
    /**
     * animateImageExpand()双列瀑布流进入详情页的过渡动画
     */
    private void animateImageExpand() {
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        View imageContainer = findViewById(R.id.image_container);
        View contentContainer = findViewById(R.id.content_container);

        //计算目标位置和大小
        int targetWidth = getResources().getDisplayMetrics().widthPixels;
        int targetHeight = (int) (targetWidth / aspectRatio());

        //设置图片容器的高度
        ViewGroup.LayoutParams containerParams = imageContainer.getLayoutParams();
        containerParams.height = targetHeight;
        imageContainer.setLayoutParams(containerParams);

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(500);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float progress = animation.getAnimatedFraction();

            //图片缩放和位移
            ViewGroup.LayoutParams params = viewPager.getLayoutParams();
            params.width = (int) (startRect.width() + (targetWidth - startRect.width()) * progress);
            params.height = (int) (startRect.height() + (targetHeight - startRect.height()) * progress);
            viewPager.setLayoutParams(params);

            //计算相对于内容容器的位置
            float x = startRect.left - contentView.getLeft() + (0 - (startRect.left - contentView.getLeft())) * progress;
            float y = startRect.top - contentView.getTop() + (0 - (startRect.top - contentView.getTop())) * progress;
            viewPager.setX(x);
            viewPager.setY(y);

            //背景渐显
            contentContainer.setAlpha(progress);
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
                viewPager.setX(0);
                viewPager.setY(0);
                ViewGroup.LayoutParams finalParams = viewPager.getLayoutParams();
                finalParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                finalParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                viewPager.setLayoutParams(finalParams);
            }
        });
        animator.start();
    }
    /**
     * setupSwipeToDismiss()实现触APP屏幕滑动关闭详情页以及恢复详情页
     */
    @SuppressWarnings("ClickableViewAccessibility")
    private void setupSwipeToDismiss() {
        contentTouchArea.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isAnimating) {
                    return false;
                }
                float x = event.getRawX();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = x;
                        isDragging = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float deltaX = x - startX;

                        // 如果横向滑动距离超过阈值，开始拖动
                        if (Math.abs(deltaX) > 200 && !isDragging) {
                            isDragging = true;
                        }

                        if (isDragging) {
                            dragProgress = Math.min(1, Math.abs(deltaX) / 800);
                            updateHorizontalSwipeAnimation(deltaX, dragProgress);
                            return true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (isDragging) {
                            float finalDeltaX = x - startX;
                            // 根据最终滑动距离决定是否退出
                            if (Math.abs(finalDeltaX) > 200) {
                                animateImageReturn();
                            } else {
                                resetHorizontalSwipeAnimation();
                            }
                            isDragging = false;
                            dragProgress = 0f;
                            return true;
                        }
                        break;
                }
                return false;
            }
        });
    }
    /**
     * updateHorizontalSwipeAnimation()实现拖动过程中的缩放动画效果
     */
    private void updateHorizontalSwipeAnimation(float deltaX, float progress) {
        //横向位移以及进度缩放
        float translationX = deltaX;
        float scale = 1 - progress * 0.2f;

        contentView.setTranslationX(translationX);
        contentView.setScaleX(scale);
        contentView.setScaleY(scale);

        //背景透明度变化
        float alpha = 1 - progress;
        contentContainer.setAlpha(alpha);
        backgroundView.setAlpha(progress);
    }
    /**
     * resetHorizontalSwipeAnimation()恢复详情页动画
     */
    private void resetHorizontalSwipeAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(contentView.getTranslationX(), 0);
        animator.setDuration(1000);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float currentX = (float) animation.getAnimatedValue();
            contentView.setTranslationX(currentX);

            //重置缩放和透明度
            float progress = 1 - Math.abs(currentX) / 800;
            float scale = 1 - (1 - progress) * 0.2f;
            contentView.setScaleX(scale);
            contentView.setScaleY(scale);

            //内容区域渐显，双列背景渐隐
            contentContainer.setAlpha(progress);
            backgroundView.setAlpha(1-progress);
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                contentView.setTranslationX(0);
                contentView.setScaleX(1);
                contentView.setScaleY(1);
                backgroundView.setAlpha(0f);
                contentContainer.setAlpha(1f);
            }
        });
        animator.start();
    }
    /**
     * finishWithAnimation()结束详情页动画，分为两个选择：1. 自定义退出详情页动画 2. 正常退出详情页
     */
    private void finishWithAnimation() {
        if (startRect != null) {
            animateImageReturn();
        } else {
            finish();
            overridePendingTransition(0, R.anim.fade_out);
        }
    }
    /**
     * animateImageReturn()恢复详情页动画
     */
    private void animateImageReturn() {
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        View contentContainer = findViewById(R.id.content_container);
        ImageView backgroundView = findViewById(R.id.background_view);

        //当前ViewPager2在容器内的尺寸
        int currentWidth = viewPager.getWidth();
        int currentHeight = viewPager.getHeight();

        //先将ViewPager2移出容器，准备动画
        viewPager.setX(0);
        viewPager.setY(0);
        ViewGroup.LayoutParams params = viewPager.getLayoutParams();
        params.width = currentWidth;
        params.height = currentHeight;
        viewPager.setLayoutParams(params);

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(1000);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float progress = animation.getAnimatedFraction();

            ViewGroup.LayoutParams params2 = viewPager.getLayoutParams();
            params2.width = (int) (currentWidth + (startRect.width() - currentWidth) * progress);
            params2.height = (int) (currentHeight + (startRect.height() - currentHeight) * progress);
            viewPager.setLayoutParams(params2);

            float currentX = viewPager.getX();
            float currentY = viewPager.getY();
            float targetX = startRect.left;
            float targetY = startRect.top;

            viewPager.setX(currentX + (targetX - currentX) * progress);
            viewPager.setY(currentY + (targetY - currentY) * progress);

            //详情页渐显，背景渐隐
            contentContainer.setAlpha(1 - progress);
            if (backgroundView != null) {
                backgroundView.setAlpha(progress);
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimating = true;
                if (backgroundView != null) {
                    backgroundView.setAlpha(0f);
                    backgroundView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
                finish();
                overridePendingTransition(0, 0);
            }
        });

        animator.start();
    }
    /**
     * setupBackPressedCallback()系统返回时需要调用的结束动画
     */
    private void setupBackPressedCallback() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishWithAnimation();
            }
        });
    }
    /**
     * setupTopAuthorArea()点击返回按钮调用动画退出
     */
    private void setupTopAuthorArea() {
        //返回按钮,调用动画退出
        btnBack.setOnClickListener(v -> {
            if (isAnimating) return;
            finishWithAnimation();
        });
        //作者信息
        Glide.with(this)
                .load(post.getAvatarUrlFromAuthor())//加载头像url
                .apply(new RequestOptions()
                        .circleCrop()
                        .override(40, 40)// 指定尺寸为40x40像素
                        .placeholder(R.drawable.avatar_placeholder)
                        .error(R.drawable.avatar_placeholder))
                .into(authorAvatar);
        tvAuthorName.setText(post.getAuthorName());
        //设立关注按钮
        setupFollowButton();
    }
    /**
     * setupFollowButton()点击关注按钮
     */
    private void setupFollowButton() {
        //点击前准备：生成关注状态的存储键，查看当前作者是否关注，如果关注需要更新UI
        boolean isFollowing = preferences.getBoolean(getFollowKey(), false);
        updateFollowUI(isFollowing);
        //点击后：关注按钮点击事件：加关注持久化
        btnFollow.setOnClickListener(v -> {
            //获取当前的关注状态
            boolean currentFollow = preferences.getBoolean(getFollowKey(), false);
            //切换关注状态
            boolean newFollow = !currentFollow;
            //保存新的关注状态到本地存储，更新UI显示新关注状态
            preferences.edit().putBoolean(getFollowKey(), newFollow).apply();
            updateFollowUI(newFollow);
        });
    }
    /**
     * updateFollowUI()更新关注按钮UI
     */
    private void updateFollowUI(boolean isFollowing) {
        if (isFollowing) {
            btnFollow.setText("已关注");
            btnFollow.setBackgroundResource(R.drawable.btn_following);
            btnFollow.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        } else {
            btnFollow.setText("关注");
            btnFollow.setBackgroundResource(R.drawable.btn_follow);
            btnFollow.setTextColor(ContextCompat.getColor(this, R.color.red));
        }
    }
    /**
     * setupContentArea()更新内容区域（标题、内容、发布时间）
     */
    private void setupContentArea() {
        tvTitle.setText(post.getTitle() != null ? post.getTitle() : "");
        //内容（处理话题词高亮）
        setupContentWithHashtags();
        tvCreateTime.setText(formatCreateTimePrecise(post.getCreateTime()));
    }
    /**
     * setupContentWithHashtags()内容（处理话题词高亮）以及话题词跳转话题词界面
     */
    private void setupContentWithHashtags() {
        String content = post.getContent();
        if (content == null || content.isEmpty()) {
            tvContent.setVisibility(View.GONE);
            return;
        }
        //将内容字符串转换为SpannableString，以便可以设置样式和点击事件。
        SpannableString spannableString = new SpannableString(content);

        if (post.getHashtag() != null) {
            //判断话题词的索引范围是否合法，如何找到hashtag所对应的标签并且高亮处理
            for (Hashtag hashtag : post.getHashtag()) {
                if (hashtag.getStart() >= 0 && hashtag.getEnd() <= content.length()) {
                    ClickableSpan clickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View widget) {
                            //跳转到话题页面
                            navigateToHashtagPage(hashtag);
                        }

                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            super.updateDrawState(ds);//ds为控制文本绘制外观的对象
                            ds.setColor(ContextCompat.getColor(DetailActivity.this, R.color.hashtag_color));
                            ds.setUnderlineText(false);//关闭下划线
                        }
                    };

                    spannableString.setSpan(clickableSpan,
                            hashtag.getStart(),
                            hashtag.getEnd(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//应用到话题词对应的文本范围
                }
            }
        }

        tvContent.setText(spannableString);//设置TextView的文本为处理后的SpannableString
        tvContent.setMovementMethod(LinkMovementMethod.getInstance());//设置MovementMethod为LinkMovementMethod，这样点击事件才会生效
        tvContent.setHighlightColor(ContextCompat.getColor(this, android.R.color.transparent));//设置点击后的高亮颜色为透明，即点击后不会出现默认的底色
    }
    /**
     * navigateToHashtagPage()实现跳转话题界面
     */
    private void navigateToHashtagPage(Hashtag hashtag) {
        if (post == null || post.getContent() == null || hashtag == null) {
            return;
        }
        //确保索引范围有效
        if (hashtag.getStart() < 0 || hashtag.getEnd() > post.getContent().length()) {
            return;
        }
        //获取话题词文本
        String hashtagText = post.getContent().substring(hashtag.getStart(), hashtag.getEnd());
        //移除#符号（如果存在）
        if (hashtagText.startsWith("#")) {
            hashtagText = hashtagText.substring(1);
        }
        //跳转到话题页面
        Intent intent = new Intent(this, HashTagActivity.class);
        intent.putExtra("hashtag_name", hashtagText);
        try {
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        } catch (Exception e) {
            Toast.makeText(this, "无法打开话题页面", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * formatCreateTimePrecise()发布日期时间的格式化
     */
    private String formatCreateTimePrecise(long timestamp) {
        long currentTime = System.currentTimeMillis() / 1000;
        //timestamp为秒级时间戳
        long diff = currentTime - timestamp;
        //Calendar是毫秒级时间戳
        Calendar postCalendar = Calendar.getInstance();
        postCalendar.setTimeInMillis(timestamp * 1000);
        Calendar currentCalendar = Calendar.getInstance();

        if (diff < 60) {
            return "刚刚";
        } else if (diff < 3600) {
            long minutes = diff / 60;
            return minutes + "分钟前";
        } else if (isSameDay(postCalendar, currentCalendar)) {
            //同一天，显示具体时间
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return "今天 " + sdf.format(new Date(timestamp * 1000));
        } else if (isYesterday(postCalendar, currentCalendar)) {
            //昨天
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return "昨天 " + sdf.format(new Date(timestamp * 1000));
        } else if (diff < 604800) {
            //一周内
            long days = diff / 86400;
            return days + "天前";
        } else if (postCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)) {
            //同一年，显示月日
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd", Locale.getDefault());
            return sdf.format(new Date(timestamp * 1000));
        } else {
            //不同年，显示完整日期
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.format(new Date(timestamp * 1000));
        }
    }
    /**
     * isSameDay()判断是否为同一天
     */
    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
    /**
     * isYesterday()判断是否为昨天
     */
    private boolean isYesterday(Calendar postCal, Calendar currentCal) {
        Calendar yesterday = (Calendar) currentCal.clone();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        return postCal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                postCal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR);
    }
    /**
     * setupBottomInteraction()设置底部导航栏的按钮的交互
     */
    private void setupBottomInteraction() {
        setupLikeButton();
        ivShare.setOnClickListener(v -> showShareDialog());
        ivCollect.setOnClickListener(v -> changeCollect());
        ivComment.setOnClickListener(v ->
                Toast.makeText(this, "评论功能", Toast.LENGTH_SHORT).show());

    }
    /**
     * changeCollect()收藏/取消收藏方法
     */
    private void changeCollect() {
        //切换收藏状态
        isCollected = !isCollected;
        //更新收藏数
        if (isCollected) {
            //收藏：收藏数+1
            currentCollectCount++;
        } else {
            //取消收藏：收藏数-1（但不能少于0）
            currentCollectCount = Math.max(0, currentCollectCount - 1);
        }
        //保存收藏状态和收藏数
        saveCollectState();
        saveCollectCount();
        //更新UI
        updateCollectUI(isCollected);
        tvCollectCount.setText(String.valueOf(currentCollectCount));
        //添加收藏动画效果
        animateCollectButton();
    }
    /**
     * animateCollectButton()收藏按钮动画
     */
    private void animateCollectButton() {
        // 缩放动画
        ivCollect.animate()
                .scaleX(1.3f)
                .scaleY(1.3f)
                .setDuration(150)
                .withEndAction(() -> ivCollect.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(150)
                        .start())
                .start();
    }
    /**
     * loadCollectCount()加载收藏数
     */
    private void loadCollectCount() {
        //SharedPreferences加载收藏数
        String collectCountKey = "collect_count_" + post.getId();
        // 检查是否是第一次加载（即没有保存过收藏数）
        boolean hasSavedCount = preferences.contains(collectCountKey);
        if (!hasSavedCount) {
            //第一次加载，生成随机收藏数
            currentCollectCount = generateRealisticRandomCollectCount();
            //保存随机收藏数
            saveCollectCount();
        } else {
            //已经有保存的收藏数，直接加载
            currentCollectCount = preferences.getInt(collectCountKey, 0);
        }
        //更新UI显示
        tvCollectCount.setText(String.valueOf(currentCollectCount));
    }
    /**
     * loadCollectState()加载收藏状态
     */
    private void loadCollectState() {
        //从SharedPreferences加载收藏状态
        String collectStateKey = "collect_state_" + post.getId();
        isCollected = preferences.getBoolean(collectStateKey, false);
        //更新收藏图标
        updateCollectUI(isCollected);
    }
    /**
     * generateRealisticRandomCollectCount()生成符合真实分布的随机收藏数
     */
    private int generateRealisticRandomCollectCount() {
        Random random = new Random();
        double r = random.nextDouble();
        if (r < 0.8) {
            return random.nextInt(50) + 1;
        } else if (r < 0.95) {
            return random.nextInt(150) + 50;
        } else if (r < 0.99) {
            return random.nextInt(800) + 200;
        } else {
            return random.nextInt(4000) + 1000;
        }
    }
    /**
     * saveCollectCount()保存收藏数
     */
    private void saveCollectCount() {
        String collectCountKey = "collect_count_" + post.getId();
        preferences.edit()
                .putInt(collectCountKey, currentCollectCount)
                .apply();
    }
    /**
     * saveCollectState()保存收藏状态
     */
    private void saveCollectState() {
        String collectStateKey = "collect_state_" + post.getId();
        preferences.edit()
                .putBoolean(collectStateKey, isCollected)
                .apply();
    }
    /**
     * updateCollectUI()更新收藏UI显示
     */
    private void updateCollectUI(boolean isCollected) {
        if (isCollected) {
            //已收藏：黄色图标
            ivCollect.setImageResource(R.drawable.ic_collected);
        } else {
            //未收藏：灰色图标
            ivCollect.setImageResource(R.drawable.ic_collect);
        }
    }
    /**
     * setupLikeButton()设置点赞按钮，获取点赞状态以及基础点赞数
     */
    private void setupLikeButton() {
        //获取点赞状态以及基础点赞数
        boolean isLiked = preferences.getBoolean(getLikeKey(), false);
        int baseLikeCount = preferences.getInt(getBaseCountKey(), -1);
        //计算显示点赞数
        int displayLikeCount = baseLikeCount + (isLiked ? 1 : 0);
        updateLikeUI(isLiked);
        tvLikeCount.setText(String.valueOf(displayLikeCount));
        ivLike.setOnClickListener(v -> change_Like());
    }
    /**
     * change_Like()变更点赞按钮UI，点赞数、状态等
     */
    private void change_Like() {

        boolean currentLike = preferences.getBoolean(getLikeKey(), false);
        int baseLikeCount = preferences.getInt(getBaseCountKey(), -1);
        boolean newLike = !currentLike;
        // 计算新的显示点赞数
        int displayLikeCount = baseLikeCount + (newLike ? 1 : 0);

        preferences.edit().putBoolean(getLikeKey(), newLike).apply();
        updateLikeUI(newLike);
        tvLikeCount.setText(String.valueOf(displayLikeCount));
        animateLikeButton(newLike);
    }
    /**
     * animateLikeButton()点赞按钮动画
     */
    private void animateLikeButton(boolean isLiked) {
        if (isLiked) {
            ValueAnimator scaleAnimator = ValueAnimator.ofFloat(1.0f, 1.5f, 1.0f);
            scaleAnimator.setDuration(400);
            scaleAnimator.setInterpolator(new DecelerateInterpolator());
            scaleAnimator.addUpdateListener(animation -> {
                float scale = (float) animation.getAnimatedValue();
                ivLike.setScaleX(scale);
                ivLike.setScaleY(scale);
            });
            //创建粒子动画效果
            createLikeParticleEffect();
            scaleAnimator.start();
        } else {
            //取消点赞时的动画（简单缩小再恢复）
            ivLike.animate()
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(150)
                    .withEndAction(() -> ivLike.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(150)
                            .start())
                    .start();
        }
    }
    /**
     * createLikeParticleEffect()创建点赞粒子动画效果
     */
    private void createLikeParticleEffect() {
        //创建多个粒子
        int particleCount = 8; // 粒子数量
        for (int i = 0; i < particleCount; i++) {
            createSingleLikeParticle(i);
        }
    }
    /**
     * createSingleLikeParticle()创建单个点赞粒子
     */
    private void createSingleLikeParticle(int index) {
        //创建粒子视图
        ImageView particleView = new ImageView(this);
        particleView.setImageResource(R.drawable.ic_liked_particle);

        //获取点赞按钮的位置
        int[] location = new int[2];
        ivLike.getLocationOnScreen(location);

        //计算粒子起始位置（点赞按钮中心）
        int startX = location[0] + ivLike.getWidth() / 2;
        int startY = location[1] - ivLike.getHeight() / 2;

        //粒子初始位置和大小
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(16, 16);
        params.leftMargin = startX - 8;
        params.topMargin = startY - 8;
        particleView.setLayoutParams(params);

        //添加到根布局
        ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
        rootView.addView(particleView);

        //计算粒子结束位置（随机方向）
        Random random = new Random();
        double angle = (index * (360.0 / 8)) + random.nextDouble() * 20 - 10;
        double radians = Math.toRadians(angle);
        int distance = 60 + random.nextInt(40);

        float endX = (float) (startX + Math.cos(radians) * distance);
        float endY = (float) (startY - Math.sin(radians) * distance);

        //粒子动画
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(600);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float progress = animation.getAnimatedFraction();

            float currentX = startX + (endX - startX) * progress;
            float currentY = startY + (endY - startY) * progress;

            //缩放（先放大后缩小）
            float scale;
            if (progress < 0.3f) {
                scale = 0.5f + progress * 1.67f;
            } else if (progress < 0.6f) {
                scale = 1.0f;
            } else {
                scale = 1.0f - (progress - 0.6f) * 2.5f;
            }
            float alpha = 1.0f - progress * progress;

            particleView.setTranslationX(currentX - startX);
            particleView.setTranslationY(currentY - startY);
            particleView.setScaleX(scale);
            particleView.setScaleY(scale);
            particleView.setAlpha(alpha);
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //动画结束后移除粒子
                rootView.removeView(particleView);
            }
        });
        //延迟30ms启动每个粒子，形成连续效果
        animator.setStartDelay(index * 30L);
        animator.start();
    }
    /**
     * getLikeKey()获取点赞的键值
     */
    private String getLikeKey() {
        return "like_" + post.getId();
    }
    /**
     * getBaseCountKey()获取基础点赞数的键值
     */
    private String getBaseCountKey() {
        return "base_like_count_" + post.getId();
    }
    /**
     * getFollowKey()获取关注的键值
     */
    private String getFollowKey() {
        return "follow_" + post.getAuthorId();
    }
    /**
     * updateLikeUI()更新点赞UI
     */
    private void updateLikeUI(boolean isLiked) {
        int likeRes = isLiked ? R.drawable.ic_liked : R.drawable.ic_like;
        ivLike.setImageResource(likeRes);
    }
}