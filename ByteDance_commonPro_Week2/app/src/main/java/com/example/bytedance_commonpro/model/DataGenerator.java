package com.example.bytedance_commonpro.model;
import java.util.ArrayList;
import java.util.List;

public class DataGenerator {
    public static List<Post> generatePosts(int count) {
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Post post = new Post();
            post.setId("1000" + i); // 使用新的字段名
            post.setTitle("图文时代 " + (i + 1));
            post.setContent("客户端训练营 #快来参与");

            // 创建clip
            Clip clip = new Clip();
            clip.setType(0); // 图片
            clip.setWidth(1440);
            clip.setHeight(2438);
            clip.setUrl("https://lfa-static.bytednsdoc.com/obj/eden-cn/kndeh7nuvkuhbnbd/test/cobblestone.jpg");
            post.getClips().add(clip);

            // 设置封面图
            post.setCoverUrl(clip.getUrl());

            // 设置作者信息 - 使用Author对象
            Author author = new Author();
            author.setUser_id("12306");
            author.setNickname("营业中");
            author.setAvatar("https://lfa-static.bytednsdoc.com/obj/eden-cn/kndeh7nuvkuhbnbd/test/eye_light.png");
            post.setAuthor(author); // 设置author对象

            // 设置时间戳 - 使用新的字段名
            post.setCreate_time(System.currentTimeMillis() / 1000 - i * 3600);
            post.setLikeCount(100 + i * 10);

            posts.add(post);
        }
        return posts;
    }
}