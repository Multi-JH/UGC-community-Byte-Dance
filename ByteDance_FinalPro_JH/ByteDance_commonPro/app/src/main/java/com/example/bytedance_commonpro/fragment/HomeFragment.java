package com.example.bytedance_commonpro.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bytedance_commonpro.R;
import com.example.bytedance_commonpro.activity.DetailActivity;
import com.example.bytedance_commonpro.adapter.PostAdapter;
import com.example.bytedance_commonpro.model.DataGenerator;
import com.example.bytedance_commonpro.model.FeedResponse;
import com.example.bytedance_commonpro.model.Post;
import com.example.bytedance_commonpro.network.NetworkManager;
import com.google.android.material.tabs.TabLayout;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    public static Bitmap homeScreenshot;
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PostAdapter postAdapter;
    private List<Post> postList = new ArrayList<>();
    private boolean isLoading = false;
    private boolean hasMore = true;
    private Rect lastClickedViewRect = new Rect();
    private float startX = 0;
    private float startY = 0;
    private static final int MIN_DISTANCE = 25;
    private int currentTabPosition = 3;
    /**
     * 生命周期（onCreateView）
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (container != null) {
            container.removeAllViews();
        }
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initViews(view);
        setupTabLayout();
        setupRecyclerView();
        loadData();
        return view;
    }
    /**
     * 生命周期（onResume）
     */
    @Override
    public void onResume() {
        super.onResume();
        //当从详情页返回时，刷新列表以同步点赞状态
        if (postAdapter != null) {
            postAdapter.notifyDataSetChanged();
        }
    }
    /**
     * 生命周期（onDestroyView）
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //清理资源
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
    /**
     * initViews()初始化所有视图组件
     */
    private void initViews(View view) {
        // 获取TabLayout
        tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_search));
        recyclerView = view.findViewById(R.id.recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
    }
    /**
     * 设置上方TabLayout的监听器以及默认选择
     */
    private void setupTabLayout() {
        TabLayout.Tab communityTab = tabLayout.getTabAt(3);
        if (communityTab != null) {
            communityTab.select();
        }
        //设置Tab选择监听器
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                refreshData();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                refreshData();
            }
        });
    }
    /**
     * setupRecyclerView()
     * 创建双列瀑布流并绑定数据，并设置双列瀑布流点击事件传递数据
     * 为双列瀑布流构建下拉刷新以及上滑加载更多
     */
    private void setupRecyclerView() {
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.getRecycledViewPool().clear();

        //创建适配器，将数据绑定到视图之中
        postAdapter = new PostAdapter(requireContext(), postList);
        recyclerView.setAdapter(postAdapter);

        //设置点击事件
        postAdapter.setOnItemClickListener(new PostAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Post post, int position) {
                takeHomeScreenshot();
                //通过点击的位置来获取点击的卡片视图
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                if (viewHolder != null) {
                    View itemView = viewHolder.itemView;
                    ImageView coverImage = itemView.findViewById(R.id.iv_cover);
                    //获取视图在屏幕中的位置
                    int[] location = new int[2];
                    coverImage.getLocationOnScreen(location);
                    lastClickedViewRect.set(
                            location[0],
                            location[1],
                            location[0] + coverImage.getWidth(),
                            location[1] + coverImage.getHeight()
                    );
                    //跳转到详情页，并传递共享元素信息
                    Intent intent = new Intent(getActivity(), DetailActivity.class);
                    intent.putExtra("post", post);
                    intent.putExtra("start_rect", lastClickedViewRect);
                    intent.putExtra("position", position);
                    if (homeScreenshot != null) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        homeScreenshot.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                        byte[] byteArray = stream.toByteArray();
                        intent.putExtra("home_screenshot", byteArray);
                    }
                    //创建共享元素转场动画
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            requireActivity(),
                            coverImage,
                            "home_screenshot"
                    );
                    startActivity(intent, options.toBundle());
                } else {
                    //如果无法获取视图，使用普通跳转
                    Intent intent = new Intent(getActivity(), DetailActivity.class);
                    intent.putExtra("post", post);
                    startActivity(intent);
                }
            }
        });
        //设置双列瀑布流的下拉刷新
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });
        //设置双列瀑布流的上拉加载更多
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //若无法再向下滚动，则加载更多数据
                if (!recyclerView.canScrollVertically(1)) {
                    loadMoreData();
                }
            }
        });
        //为RecyclerView添加横向滑动监听
        setupRecyclerViewTouchListener();
    }
    /**
     * setupRecyclerViewTouchListener()
     * 为RecyclerView添加横向滑动监听
     */
    private void setupRecyclerViewTouchListener() {
        recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                float x = e.getX();
                float y = e.getY();

                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = x;
                        startY = y;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float deltaX = Math.abs(x - startX);
                        float deltaY = Math.abs(y - startY);

                        //如果是横向滑动且垂直移动较小，则拦截滑动事件
                        if (deltaX > MIN_DISTANCE && deltaX > deltaY * 2) {
                            return true;
                        }
                        break;
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                float x = e.getX();

                switch (e.getAction()) {
                    case MotionEvent.ACTION_UP:
                        float distanceX = x - startX;

                        //判断是否为有效的横向滑动
                        if (Math.abs(distanceX) > MIN_DISTANCE) {
                            if (distanceX > 0) {
                                //向右滑动，切换到前一个Tab
                                switchToPreviousTab();
                            } else {
                                //向左滑动，切换到后一个Tab
                                switchToNextTab();
                            }
                        }
                        break;
                }
            }
        });
    }
    /**
     * 切换到下一个Tab
     */
    private void switchToNextTab() {
        int nextPosition = currentTabPosition + 1;
        if (nextPosition < tabLayout.getTabCount()) {
            TabLayout.Tab nextTab = tabLayout.getTabAt(nextPosition);
            if (nextTab != null) {
                nextTab.select();
                currentTabPosition = nextPosition;
            }
        }
    }
    /**
     * 切换到上一个Tab
     */
    private void switchToPreviousTab() {
        int prevPosition = currentTabPosition - 1;
        if (prevPosition >= 0) {
            TabLayout.Tab prevTab = tabLayout.getTabAt(prevPosition);
            if (prevTab != null) {
                prevTab.select();
                currentTabPosition = prevPosition;
            }
        }
    }
    /**
     * 截图之后，为实现传递到详情页的共享元素
     */
    private void takeHomeScreenshot() {
        Activity activity = getActivity();
        if (activity == null || activity.isFinishing()) return;

        try {
            // 获取内容视图（不包括系统栏）
            View contentView = activity.findViewById(android.R.id.content);
            if (contentView == null) return;

            //获取内容视图在屏幕中的位置
            int[] location = new int[2];
            contentView.getLocationOnScreen(location);

            //获取DecorView的截图
            View decorView = activity.getWindow().getDecorView();
            decorView.setDrawingCacheEnabled(true);
            decorView.buildDrawingCache();
            Bitmap fullScreenBitmap = decorView.getDrawingCache();
            if (fullScreenBitmap == null) return;

            //计算内容区域的位置和大小
            int left = location[0];
            int top = location[1];
            int width = contentView.getWidth();
            int height = contentView.getHeight();

            //确保坐标在合理范围内
            if (top < 0) top = 0;
            if (left < 0) left = 0;
            if (top + height > fullScreenBitmap.getHeight()) {
                height = fullScreenBitmap.getHeight() - top;
            }
            if (left + width > fullScreenBitmap.getWidth()) {
                width = fullScreenBitmap.getWidth() - left;
            }

            //只截取内容区域
            if (width > 0 && height > 0) {
                homeScreenshot = Bitmap.createBitmap(
                        fullScreenBitmap,
                        left,
                        top,
                        width,
                        height
                );
            } else {
                // 回退到完整截图
                homeScreenshot = Bitmap.createBitmap(fullScreenBitmap);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                View decorView = activity.getWindow().getDecorView();
                decorView.setDrawingCacheEnabled(false);
                decorView.destroyDrawingCache();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 首次加载数据
     */
    private void loadData() {
        isLoading = true;
        NetworkManager.ApiService apiService = NetworkManager.getApiService();
        //根据接口的内容创建请求的对象，获取20条数据
        Call<FeedResponse> call = apiService.getFeed(20, false);
        //HTTP请求成功发出并收到响应
        call.enqueue(new Callback<FeedResponse>() {
            @Override
            public void onResponse(Call<FeedResponse> call, Response<FeedResponse> response) {
                //发送HTTP请求，并接收服务器返回的JSON数据
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);
                //判断http状态码是否在范围之内，响应体不为空
                if (response.isSuccessful() && response.body() != null) {
                    //区分开http成功与业务成功
                    FeedResponse feedResponse = response.body();
                    if (feedResponse.getStatusCode() == 0) {
                        List<Post> newPosts = feedResponse.getPostList();
                        //过滤掉为null的clips
                        List<Post> filteredPosts = new ArrayList<>();
                        for (Post post : newPosts) {
                            if (post.getClips() != null && !post.getClips().isEmpty()) {
                                filteredPosts.add(post);
                            }
                        }
                        postAdapter.updateData(filteredPosts);
                        hasMore = feedResponse.getHasMore() == 1;
                    } else {
                        //状态码未对应，失败时使用降级方案
                        showToast("加载失败，状态码: " + feedResponse.getStatusCode());
                        useFallbackData();
                    }
                } else {
                    showToast("网络请求失败");
                    useFallbackData();
                }
            }
            //网络异常使用降级方案
            @Override
            public void onFailure(Call<FeedResponse> call, Throwable t) {
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);
                showToast("网络连接失败: " + t.getMessage());
                useFallbackData();
            }
        });
    }
    /**
     * 刷新数据
     */
    private void refreshData() {
        //避免重复刷新
        if (isLoading) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        swipeRefreshLayout.setRefreshing(true);
        isLoading = true;
        NetworkManager.ApiService apiService = NetworkManager.getApiService();
        Call<FeedResponse> call = apiService.getFeed(15, false);
        call.enqueue(new Callback<FeedResponse>() {
            @Override
            public void onResponse(Call<FeedResponse> call, Response<FeedResponse> response) {
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    FeedResponse feedResponse = response.body();
                    if (feedResponse.getStatusCode() == 0) {
                        List<Post> newPosts = feedResponse.getPostList();
                        List<Post> filteredPosts = new ArrayList<>();
                        for (Post post : newPosts) {
                            if ((post.getClips() != null && !post.getClips().isEmpty())) {
                                filteredPosts.add(post);
                            }
                        }
                        postAdapter.updateData(filteredPosts);
                        hasMore = feedResponse.getHasMore() == 1;
                    } else {
                        showToast("刷新失败");
                        useFallbackData();
                    }
                } else {
                    showToast("刷新失败");
                    useFallbackData();
                }
            }

            @Override
            public void onFailure(Call<FeedResponse> call, Throwable t) {
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);
                showToast("刷新失败: " + t.getMessage());
                useFallbackData();
            }
        });
    }
    /**
     * 加载更多数据
     */
    private void loadMoreData() {
        if (isLoading || !hasMore) return;
        isLoading = true;
        NetworkManager.ApiService apiService = NetworkManager.getApiService();
        Call<FeedResponse> call = apiService.getFeed(10, false);
        call.enqueue(new Callback<FeedResponse>() {
            @Override
            public void onResponse(Call<FeedResponse> call, Response<FeedResponse> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    FeedResponse feedResponse = response.body();
                    if (feedResponse.getStatusCode() == 0) {
                        List<Post> morePosts = feedResponse.getPostList();
                        List<Post> filteredMorePosts = new ArrayList<>();
                        for (Post post : morePosts) {
                            if ((post.getClips() != null && !post.getClips().isEmpty())) {
                                filteredMorePosts.add(post);
                            }
                        }
                        postAdapter.addData(filteredMorePosts);
                        hasMore = feedResponse.getHasMore() == 1;
                    } else {
                        showToast("加载更多失败");
                    }
                } else {
                    showToast("加载更多失败");
                }
            }

            @Override
            public void onFailure(Call<FeedResponse> call, Throwable t) {
                isLoading = false;
                showToast("加载更多失败: " + t.getMessage());
            }
        });
    }
    /**
     * 降级方案
     */
    private void useFallbackData() {
        List<Post> newPosts = DataGenerator.generatePosts(20);
        postList.clear();
        postList.addAll(newPosts);
        postAdapter.notifyDataSetChanged();
    }
    /**
     * Toast方法
     */
    private void showToast(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 滚动到顶部（单击首页标签时调用）
     */
    public void scrollToTop() {
        if (recyclerView != null) {
            recyclerView.smoothScrollToPosition(0);
        }
    }
    /**
     * 刷新首页内容（双击首页标签时调用）
     */
    public void refreshHomeContent() {
        //如果已经在刷新中，则直接返回
        if (isLoading) {
            return;
        }
        //显示下拉刷新指示器
        if (swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }
        //先滚动到顶部
        scrollToTop();
        //延迟100ms后刷新数据，让用户先看到滚动效果
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshData();
                        }
                    });
                }
            }
        }, 100);
    }
}
