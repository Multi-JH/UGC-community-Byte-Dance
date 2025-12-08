package com.example.bytedance_commonpro.model;

import android.os.Parcel;
import android.os.Parcelable;
/**
 * Hashtag类
 * 文本标注位置的数据模型
 * 专门用于标记文本中话题标签的位置范围
 */
public class Hashtag implements Parcelable {
    private int start;
    private int end;

    public Hashtag() {}

    protected Hashtag(Parcel in) {
        start = in.readInt();
        end = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(start);
        dest.writeInt(end);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Hashtag> CREATOR = new Creator<Hashtag>() {
        @Override
        public Hashtag createFromParcel(Parcel in) {
            return new Hashtag(in);
        }

        @Override
        public Hashtag[] newArray(int size) {
            return new Hashtag[size];
        }
    };

    public int getStart() {
        return start;
    }
    public void setStart(int start) {
        this.start = start;
    }
    public int getEnd() {
        return end;
    }
    public void setEnd(int end) {
        this.end = end;
    }
}