package com.example.bytedance_commonpro.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bytedance_commonpro.R;
import com.example.bytedance_commonpro.adapter.PostAdapter;
import com.example.bytedance_commonpro.model.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HashTagActivity extends AppCompatActivity implements PostAdapter.OnItemClickListener {


    private TextView tvHashtag;
    private TextView tvPostCount;
    private RecyclerView recyclerView;
    private ImageView btnBack;
    private String hashtagName;
    private List<Post> hashtagPosts = new ArrayList<>();
    private PostAdapter adapter;
    /**
     * 生命周期（onCreate）
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hashtag);

        //获取传递的话题词
        hashtagName = getIntent().getStringExtra("hashtag_name");
        if (hashtagName == null) {
            finish();
            return;
        }
        initViews();
        setupViews();
        loadHashtagPosts();
    }
    /**
     * 设置Activity结束时的转场动画
     */
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
    /**
     * initViews()初始化所有视图组件
     */
    private void initViews() {
        tvHashtag = findViewById(R.id.tv_hashtag);
        tvPostCount = findViewById(R.id.tv_post_count);
        recyclerView = findViewById(R.id.recycler_view);
        btnBack = findViewById(R.id.btn_back);
    }
    /**
     * setupViews()设置话题界面的话题词显示以及返回按钮
     */
    private void setupViews() {
        //设置话题词显示
        tvHashtag.setText("#" + hashtagName);
        //设置返回按钮
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        adapter = new PostAdapter(this, new ArrayList<>());
        //设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onItemClick(Post post, int position) {
        navigateToDetailActivity(post, position);
    }
    /**
     * navigateToDetailActivity()跳转回详情帖子
     */
    private void navigateToDetailActivity(Post post, int position) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("post", post);
        intent.putExtra("position", position);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
    /**
     * loadHashtagPosts()加载话题帖子
     */
    private void loadHashtagPosts() {
        try {
            //清除旧数据
            hashtagPosts.clear();
            //生成模拟数据
            generateMockHashtagPosts();

            if (hashtagPosts.isEmpty()) {
                Toast.makeText(this, "未找到相关话题内容", Toast.LENGTH_SHORT).show();
                return;
            }
            //显示帖子详情
            for (int i = 0; i < hashtagPosts.size(); i++) {
                Post post = hashtagPosts.get(i);
            }

            //更新适配器
            adapter.updateData(hashtagPosts);
            //更新UI
            tvPostCount.setText(hashtagPosts.size() + " 条内容");
            //刷新RecyclerView
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(this, "加载数据出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * generateMockHashtagPosts()生成相关话题的帖子
     */
    private void generateMockHashtagPosts() {
        try {
            if (hashtagPosts.isEmpty()) {
                Random random = new Random();
                int temp = random.nextInt(20);
                for (int i = 0; i < temp; i++) {
                    hashtagPosts.add(createMockPost(i));
                }
            }

        } catch (Exception e) {
            Log.e("HashTagActivity", "生成模拟数据出错", e);
        }
    }
    /**
     * createMockPost()生成相关话题的帖子数据
     */
    private Post createMockPost(int index) {
        Post post = new Post();
        post.setId("mock_post_" + index + "_" + hashtagName);
        post.setTitle("关于#" + hashtagName + "的讨论");
        post.setContent("这是关于#" + hashtagName + "的示例内容，展示了这个话题的相关讨论和分享。");
        post.setCreateTime(System.currentTimeMillis() / 1000 - index * 3600);

        //设置作者
        com.example.bytedance_commonpro.model.Author author =
                new com.example.bytedance_commonpro.model.Author();
        author.setUser_id("mock_author_" + index);
        author.setNickname("话题用户" + (index + 1));
        int avatarIndex = Math.abs((index + hashtagName.hashCode()) % 70) + 1;
        author.setAvatar("https://lf3-static.bytednsdoc.com/obj/eden-cn/219eh7pbyphrnuvk/college_training_camp/avatars/avatar_" + avatarIndex+".jpg");
        post.setAuthor(author);

        int imageWidth = 800;
        int imageHeight = 600;

        //使用Picsum的seed功能，相同的seed会生成相同的图片
        String seed = "topic_" + hashtagName + "_" + (index % 20);
        String imageUrl = String.format("https://picsum.photos/seed/%s/%d/%d",
                seed, imageWidth, imageHeight);
        //设置图片
        List<com.example.bytedance_commonpro.model.Clip> clips = new ArrayList<>();
        com.example.bytedance_commonpro.model.Clip clip =
                new com.example.bytedance_commonpro.model.Clip();
        clip.setUrl(imageUrl);
        clip.setWidth(imageWidth);
        clip.setHeight(imageHeight);
        clips.add(clip);
        post.setClips(clips);
        return post;
    }
}