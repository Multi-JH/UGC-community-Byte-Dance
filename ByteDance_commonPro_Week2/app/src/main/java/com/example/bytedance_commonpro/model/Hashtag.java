package com.example.bytedance_commonpro.model;

import android.os.Parcel;
import android.os.Parcelable;

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

    // Getter and Setter
    public int getStart() { return start; }
    public void setStart(int start) { this.start = start; }
    public int getEnd() { return end; }
    public void setEnd(int end) { this.end = end; }
}