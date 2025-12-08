package com.example.bytedance_commonpro.model;

/**
 * Work类
 * 作品数据模型
 * 展示用户的作品列表
 */
public class Work {
    private String id;
    private String coverUrl;
    private int likeCount;
    private String title;
    private String createTime;

    public Work(String id, String coverUrl, int likeCount, String title, String createTime) {
        this.id = id;
        this.coverUrl = coverUrl;
        this.likeCount = likeCount;
        this.title = title;
        this.createTime = createTime;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getCoverUrl() {
        return coverUrl;
    }
    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public int getLikeCount() {
        return likeCount;
    }
    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreateTime() {
        return createTime;
    }
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}