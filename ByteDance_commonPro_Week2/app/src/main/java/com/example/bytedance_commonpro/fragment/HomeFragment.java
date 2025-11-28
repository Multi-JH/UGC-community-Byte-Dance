package com.example.bytedance_commonpro.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends BaseFragment {
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PostAdapter postAdapter;
    private List<Post> postList = new ArrayList<>();
    private boolean isTabLayoutInitialized = false;
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean hasMore = true;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (container != null) {
            container.removeAllViews();
        }
        View view = inflater.inflate(R.layout.fragment_home, container, false);// 加载首页布局
        initViews(view);// 初始化视图，找到 TabLayout 等控件
        setupTabLayout();// 设置TabLayout， 配置 Tab 默认选中、监听器等
        setupRecyclerView();
        loadData();
        applyTopWindowInset(view);
        return view;
    }
    @Override
    protected View getTopInsetView() {
        return tabLayout; // 返回顶部需要避让的
    }

    private void initViews(View view) {
        // 获取TabLayout
        tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_search));
        recyclerView = view.findViewById(R.id.recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
    }

    private void setupTabLayout() {

        // 设置默认选中"社区"
        TabLayout.Tab communityTab = tabLayout.getTabAt(3);
        if (communityTab != null) {
            communityTab.select();
        }
        // 设置Tab选择监听器
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // 当Tab被选中时调用
                // 这里可以处理选中状态的变化
                //updateTabAppearance(tab, true);
                refreshData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // 当Tab取消选中时调用
                //updateTabAppearance(tab, false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 当已选中的Tab再次被点击时调用,这里可以处理刷新等操作
                refreshData();
            }
        });
    }
    private void setupRecyclerView() {
        // 创建瀑布流布局管理器(2列)

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.getRecycledViewPool().clear();

        // 创建适配器，将数据绑定到视图之中
        postAdapter = new PostAdapter(requireContext(), postList);
        recyclerView.setAdapter(postAdapter);

        // 设置点击事件
        postAdapter.setOnItemClickListener(new PostAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Post post, int position) {
                // 跳转到详情页
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("post", post);
                startActivity(intent);
            }
        });

        // 设置下拉刷新
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        // 设置上拉加载更多，滚动监听器，上拉加载监听器
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //无法再向下滚动
                if (!recyclerView.canScrollVertically(1)) {
                    loadMoreData();
                }
            }
        });
    }
    //首次加载
    private void loadData() {
        isLoading = true;
        currentPage = 0;
        NetworkManager.ApiService apiService = NetworkManager.getApiService();
        Call<FeedResponse> call = apiService.getFeed(20, false);
        //获取20条数据
        // HTTP 请求成功发出并收到响应
        call.enqueue(new Callback<FeedResponse>() {
            @Override
            public void onResponse(Call<FeedResponse> call, Response<FeedResponse> response) {
                //标记当前不在加载之中，关闭下拉刷新的状态
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);
                //判断http状态码是否在范围之内，响应体不为空
                if (response.isSuccessful() && response.body() != null) {
                    FeedResponse feedResponse = response.body();//区分开http成功与业务成功
                    if (feedResponse.getStatusCode() == 0) { // 业务逻辑成功，且JSON 数据解析后的 Java 对象返回
                        List<Post> newPosts = feedResponse.getPostList();
                        //清空旧数据 添加新获取的作品列表
                        //告知RecyclerView刷新页面
                        postAdapter.updateData(newPosts);
                        hasMore = feedResponse.getHasMore() == 1;
                    } else {//状态码未对应
                        showToast("加载失败，状态码: " + feedResponse.getStatusCode());
                        // 失败时使用降级方案
                        useFallbackData();
                    }
                } else {
                    showToast("网络请求失败");
                    useFallbackData();
                }
            }

            //网络异常则
            @Override
            public void onFailure(Call<FeedResponse> call, Throwable t) {
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);
                showToast("网络连接失败: " + t.getMessage());
                useFallbackData();
            }
        });
    }

    private void refreshData() {
        //避免重复刷新
        if (isLoading) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        //开启刷新
        swipeRefreshLayout.setRefreshing(true);
        isLoading = true;
        currentPage = 0;
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
                        postAdapter.updateData(newPosts);
                        hasMore = feedResponse.getHasMore() == 1;
                        showToast("刷新成功");
                    } else {
                        showToast("刷新失败");
                        useFallbackDataForRefresh();
                    }
                } else {
                    showToast("刷新失败");
                    useFallbackDataForRefresh();
                }
            }

            @Override
            public void onFailure(Call<FeedResponse> call, Throwable t) {
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);
                showToast("刷新失败: " + t.getMessage());
                useFallbackDataForRefresh();
            }
        });
    }

    private void loadMoreData() {
        if (isLoading || !hasMore) return;
        isLoading = true;
        currentPage++;
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
                        postAdapter.addData(morePosts);
                        hasMore = feedResponse.getHasMore() == 1;
                        showToast("加载了 " + morePosts.size() + " 条新内容");
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
    private void useFallbackData() {
        List<Post> newPosts = DataGenerator.generatePosts(20);
        postList.clear();
        postList.addAll(newPosts);
        postAdapter.notifyDataSetChanged();
    }

    private void useFallbackDataForRefresh() {
        List<Post> newPosts = DataGenerator.generatePosts(20);
        postList.clear();
        postList.addAll(newPosts);
        postAdapter.notifyDataSetChanged();
    }
    private void showToast(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }

}
