package com.example.bytedance_commonpro.model;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 *  Post类
 * 完整的帖子数据模型
 * 整合了用户内容的所有组成部分
 */
public class Post implements Parcelable {
    private String post_id;
    private String title;
    private String content;
    private long create_time;
    private Author author;
    private List<Clip> clips;
    private Music music;
    private List<Hashtag> hashtag;
    private int commentCount;
    private int shareCount;

    public Post() {
        this.clips = new ArrayList<>();
        this.hashtag = new ArrayList<>();
    }

    public Post(String id, String title, String content, String authorId, String authorName,
                String authorAvatar, long createTime) {
        this.post_id = id;
        this.title = title;
        this.content = content;

        this.author = new Author();
        this.author.setUser_id(authorId);
        this.author.setNickname(authorName);
        this.author.setAvatar(authorAvatar);

        this.create_time = createTime;
        this.clips = new ArrayList<>();
        this.hashtag = new ArrayList<>();
    }

    //Parcelable 实现
    protected Post(Parcel in) {
        post_id = in.readString();
        title = in.readString();
        content = in.readString();
        create_time = in.readLong();
        author = in.readParcelable(Author.class.getClassLoader());
        clips = in.createTypedArrayList(Clip.CREATOR);
        music = in.readParcelable(Music.class.getClassLoader());
        hashtag = in.createTypedArrayList(Hashtag.CREATOR);
        commentCount = in.readInt();
        shareCount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(post_id);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeLong(create_time);
        dest.writeParcelable(author, flags);
        dest.writeTypedList(clips);
        dest.writeParcelable(music, flags);
        dest.writeTypedList(hashtag);
        dest.writeInt(commentCount);
        dest.writeInt(shareCount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    //Getter和Setter方法（根据JSON格式）
    public String getId() {

        return post_id;
    }

    public void setId(String id) {
        this.post_id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCreate_time() {
        return create_time;
    }

    public void setCreate_time(long create_time) {
        this.create_time = create_time;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public List<Clip> getClips() {
        return clips;
    }

    public void setClips(List<Clip> clips) {
        this.clips = clips;
    }

    public Music getMusic() {
        return music;
    }

    public void setMusic(Music music) {
        this.music = music;
    }

    public List<Hashtag> getHashtag() {
        return hashtag;
    }

    public void setHashtag(List<Hashtag> hashtag) {
        this.hashtag = hashtag;
    }

    public long getCreateTime() {
        return create_time;
    }

    public void setCreateTime(long createTime) {
        this.create_time = createTime;
    }

    public String getAuthorId() {
        return author != null ? author.getUser_id() : null;
    }

    public String getAuthorName() {
        return author != null ? author.getNickname() : "未知作者";
    }

    public String getAuthorAvatar() {
        return author != null ? author.getAvatar() : null;
    }
    /**
     * getCoverUrlFromClips()
     * 获取封面图
     * clips中获取第一张有效图片作为封面
     */
    public String getCoverUrlFromClips() {
        if (clips != null && !clips.isEmpty()) {
            for (Clip clip : clips) {
                String url = clip.getUrl();
                if (url != null && !url.isEmpty()) {
                    return url;
                }
            }

        }
        //无有效图片URL，返回默认图片
        return "https://vcg01.cfp.cn/creative/vcg/800/new/VCG211501908185.jpg";
    }
    /**
     * getAvatarUrlFromAuthor()
     * 获取作者头像
     */
    public String getAvatarUrlFromAuthor() {
        if (author != null && author.getAvatar() != null && !author.getAvatar().isEmpty()) {
            return author.getAvatar();
        }
        return "https://vcg01.cfp.cn/creative/vcg/800/new/VCG211501908185.jpg"; // 默认头像
    }
    /**
     * getFirstClipWidth()
     * 获取第一个Clip的宽度
     */
    public int getFirstClipWidth() {
        if (clips != null && !clips.isEmpty()) {
            return clips.get(0).getWidth();
        }
        return 0;
    }
    /**
     * getFirstClipHeight()
     * 获取第一个Clip的高度
     */
    public int getFirstClipHeight() {
        if (clips != null && !clips.isEmpty()) {
            return clips.get(0).getHeight();
        }
        return 0;
    }

}
