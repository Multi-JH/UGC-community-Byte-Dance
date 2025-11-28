package com.example.bytedance_commonpro.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Author implements Parcelable {
    private String user_id; // 用户ID
    private String nickname; // 用户昵称
    private String avatar; // 头像链接

    public Author() {}

    protected Author(Parcel in) {
        user_id = in.readString();
        nickname = in.readString();
        avatar = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user_id);
        dest.writeString(nickname);
        dest.writeString(avatar);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Author> CREATOR = new Creator<Author>() {
        @Override
        public Author createFromParcel(Parcel in) {
            return new Author(in);
        }

        @Override
        public Author[] newArray(int size) {
            return new Author[size];
        }
    };

    // Getter and Setter
    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
