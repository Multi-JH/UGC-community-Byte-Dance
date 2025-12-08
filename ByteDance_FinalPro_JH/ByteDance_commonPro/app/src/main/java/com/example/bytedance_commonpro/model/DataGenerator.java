package com.example.bytedance_commonpro.model;
import java.util.ArrayList;
import java.util.List;
/**
 * DataGenerator类
 * 降级操作，生成自己定义的帖子数据
 */
public class DataGenerator {
    public static List<Post> generatePosts(int count) {
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Post post = new Post();
            post.setId("1000" + i); // 使用新的字段名
            post.setTitle("图文时代 " + (i + 1));
            post.setContent("客户端训练营 #快来参与");

            //创建clip
            Clip clip = new Clip();
            clip.setType(0); // 图片
            clip.setWidth(1440);
            clip.setHeight(2438);
            clip.setUrl("https://vcg00.cfp.cn/creative/vcg/800/new/VCG211183291730.jpg");
            post.getClips().add(clip);

            //设置作者信息 - 使用Author对象
            Author author = new Author();
            author.setUser_id("12306");
            author.setNickname("营业中");
            author.setAvatar("https://vcg01.cfp.cn/creative/vcg/800/new/VCG211501908185.jpg");
            post.setAuthor(author); // 设置author对象

            //设置时间戳-使用新的字段名
            post.setCreate_time(System.currentTimeMillis() / 1000 - i * 3600);
            posts.add(post);
        }
        return posts;
    }
}