// Clip.java - 图片/视频片段
package com.example.bytedance_commonpro.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Clip implements Parcelable {
    private int type; // 0:图片, 1:视频
    private int width;
    private int height;
    private String url;

    public Clip() {}

    protected Clip(Parcel in) {
        type = in.readInt();
        width = in.readInt();
        height = in.readInt();
        url = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeString(url);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Clip> CREATOR = new Creator<Clip>() {
        @Override
        public Clip createFromParcel(Parcel in) {
            return new Clip(in);
        }

        @Override
        public Clip[] newArray(int size) {
            return new Clip[size];
        }
    };

    // Getter and Setter
    public int getType() { return type; }
    public void setType(int type) { this.type = type; }
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
