package me.aheadlcx.scrolltouch.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Description:
 * Creator: aheadlcx
 * Date:16/4/14 下午2:34
 */
public class Star implements Parcelable{
    public int avatarResid;
    private String name;


    public Star(int avatarResid) {
        this.avatarResid = avatarResid;
    }

    public int getAvatarResid() {
        return avatarResid;
    }

    public Star setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.avatarResid);
        dest.writeString(this.name);
    }

    protected Star(Parcel in) {
        this.avatarResid = in.readInt();
        this.name = in.readString();
    }

    public static final Creator<Star> CREATOR = new Creator<Star>() {
        @Override
        public Star createFromParcel(Parcel source) {
            return new Star(source);
        }

        @Override
        public Star[] newArray(int size) {
            return new Star[size];
        }
    };
}
