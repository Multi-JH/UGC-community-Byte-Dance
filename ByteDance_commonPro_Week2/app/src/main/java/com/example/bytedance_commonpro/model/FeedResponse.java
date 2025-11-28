package com.example.bytedance_commonpro.model;

import java.util.List;

public class FeedResponse {
    private int status_code;
    private int has_more;
    private List<Post> post_list;

    // Getter and Setter
    public int getStatusCode() {
        return status_code;
    }

    public void setStatusCode(int status_code) {
        this.status_code = status_code;
    }

    public int getHasMore() {
        return has_more;
    }

    public void setHasMore(int has_more) {
        this.has_more = has_more;
    }

    public List<Post> getPostList() {
        return post_list;
    }

    public void setPostList(List<Post> post_list) {
        this.post_list = post_list;
    }
}