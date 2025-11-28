package com.example.bytedance_commonpro.model;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Post implements Parcelable {
    // 根据JSON格式修改字段名
    private String post_id; // 作品ID
    private String title; // 作品标题
    private String content; // 作品正文
    private long create_time; // 作品创建时间戳

    // 作者信息改为Author对象
    private Author author; // 作者信息

    // 图片/视频片段数据
    private List<Clip> clips;

    // 音乐信息
    private Music music;

    // 话题标签
    private List<Hashtag> hashtag;

    private String coverUrl;
    private List<String> imageUrls;
    private int commentCount;
    private int shareCount;
    private int likeCount=100;

    // 空构造方法
    public Post() {
        this.imageUrls = new ArrayList<>();
        this.clips = new ArrayList<>();
        this.hashtag = new ArrayList<>();
    }

    // 全参数构造方法
    public Post(String id, String title, String content, String coverUrl,
                List<String> imageUrls, String authorId, String authorName,
                String authorAvatar, int likeCount, long createTime) {
        this.post_id = id;
        this.title = title;
        this.content = content;
        this.coverUrl = coverUrl;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();

        // 创建Author对象
        this.author = new Author();
        this.author.setUser_id(authorId);
        this.author.setNickname(authorName);
        this.author.setAvatar(authorAvatar);

        this.create_time = createTime;
        this.clips = new ArrayList<>();
        this.hashtag = new ArrayList<>();
    }

    // Parcelable 实现
    protected Post(Parcel in) {
        post_id = in.readString();
        title = in.readString();
        content = in.readString();
        create_time = in.readLong();
        author = in.readParcelable(Author.class.getClassLoader());
        clips = in.createTypedArrayList(Clip.CREATOR);
        music = in.readParcelable(Music.class.getClassLoader());
        hashtag = in.createTypedArrayList(Hashtag.CREATOR);
        coverUrl = in.readString();
        imageUrls = in.createStringArrayList();
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
        dest.writeString(coverUrl);
        dest.writeStringList(imageUrls);
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

    // Getter 和 Setter 方法（根据JSON格式）
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

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }


    public int getLikeCount() {
        // 这里可以根据需要返回一个默认值或从其他地方获取
        return likeCount; // 或者根据实际需求调整
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;// 如果需要保存点赞数，可以在这里实现
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


    public String getCoverUrlFromClips() {
        if (clips != null && !clips.isEmpty()) {
            return clips.get(0).getUrl();
        }
        return coverUrl; // 回退到原有的coverUrl
    }
    public String getAvatarUrlFromAuthor() {
        if (author != null && author.getAvatar() != null && !author.getAvatar().isEmpty()) {
            return author.getAvatar();
        }
        return "https://example.com/default_avatar.png"; // 默认头像
    }

    public boolean hasVideo() {
        if (clips != null) {
            for (Clip clip : clips) {
                if (clip.getType() == 1) { // 1表示视频
                    return true;
                }
            }
        }
        return false;
    }

}