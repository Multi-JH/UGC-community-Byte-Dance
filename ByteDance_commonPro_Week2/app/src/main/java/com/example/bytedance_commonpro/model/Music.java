// Music.java - 音乐信息
package com.example.bytedance_commonpro.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Music implements Parcelable {
    private int volume;
    private int seek_time;
    private String url;

    public Music() {}

    protected Music(Parcel in) {
        volume = in.readInt();
        seek_time = in.readInt();
        url = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(volume);
        dest.writeInt(seek_time);
        dest.writeString(url);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel in) {
            return new Music(in);
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };

    // Getter and Setter
    public int getVolume() { return volume; }
    public void setVolume(int volume) { this.volume = volume; }
    public int getSeekTime() { return seek_time; }
    public void setSeekTime(int seek_time) { this.seek_time = seek_time; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}