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
        out.writeString(name);
        out.writeString(address);
        out.writeDouble(latitude);
        out.writeDouble(longitude);
        out.writeInt(orientation);
        out.writeString(dataSource);
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
        name = in.readString();
        address = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        orientation = in.readInt();
        dataSource = in.readString();
    }

    public String getName() {
        return name;
    }
}