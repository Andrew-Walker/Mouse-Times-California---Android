package com.andrewnwalker.mousetimes_california;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by andy500mufc on 29/01/2016.
 */
public class Park implements Parcelable {
    String name;
    String address;
    Double latitude;
    Double longitude;
    Integer orientation;
    String dataSource;

    public Park(String name, String address, Double latitude, Double longitude, Integer orientation, String dataSource) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.orientation = orientation;
        this.dataSource = dataSource;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.name);
        out.writeString(this.address);
        out.writeDouble(this.latitude);
        out.writeDouble(this.longitude);
        out.writeInt(this.orientation);
        out.writeString(this.dataSource);
    }
    
    public static final Creator<Park> CREATOR = new Creator<Park>() {
        public Park createFromParcel(Parcel in) {
            return new Park(in);
        }

        public Park[] newArray(int size) {
            return new Park[size];
        }
    };

    private Park(Parcel in) {
        this.name = in.readString();
        this.address = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.orientation = in.readInt();
        this.dataSource = in.readString();
    }
}