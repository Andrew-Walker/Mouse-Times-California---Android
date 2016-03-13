package com.andrewnwalker.mousetimes_california;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.DateTime;

/**
 * Created by andy500mufc on 14/01/2016.
 */
public class Attraction implements Parcelable {
    String name;
    String waitTime;
    Double longitude;
    Double latitude;
    DateTime updated;
    Boolean disabledAccess;
    Boolean singleRider;
    Boolean heightRestriction;
    String attractionDescription;
    Boolean hasWaitTime;
    String attractionImage;
    String attractionImageSmall;
    Boolean mustSee;
    Boolean isLive;
    String fastPassReturn;
    Boolean fastPass;

    public Attraction(String retrievedName, String retrievedWaitTime, Double retrievedLongitude, Double retrievedLatitude, DateTime retrievedUpdated, Boolean retrievedDisabledAccess, Boolean retrievedSingleRider, Boolean retrievedHeightRestriction, String retrievedAttractionDescription, Boolean retrievedHasWaitTime, String retrievedAttractionImage, String retrievedAttractionImageSmall, Boolean retrievedMustSee, Boolean retrievedIsLive, String retrievedFastPassReturn, Boolean retrievedFastPass) {
        this.name = retrievedName;
        this.waitTime = retrievedWaitTime;
        this.longitude = retrievedLongitude;
        this.latitude = retrievedLatitude;
        this.updated = retrievedUpdated;
        this.disabledAccess = retrievedDisabledAccess;
        this.singleRider = retrievedSingleRider;
        this.heightRestriction = retrievedHeightRestriction;
        this.attractionDescription = retrievedAttractionDescription;
        this.hasWaitTime = retrievedHasWaitTime;
        this.attractionImage = retrievedAttractionImage;
        this.attractionImageSmall = retrievedAttractionImageSmall;
        this.mustSee = retrievedMustSee;
        this.isLive = retrievedIsLive;
        this.fastPassReturn = retrievedFastPassReturn;
        this.fastPass = retrievedFastPass;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.name);
        out.writeString(this.waitTime);
        out.writeDouble(this.longitude);
        out.writeDouble(this.latitude);
        out.writeSerializable(this.updated);
        out.writeInt((byte) (this.disabledAccess ? 1 : 0));
        out.writeInt((byte) (this.singleRider ? 1 : 0));
        out.writeInt((byte) (this.heightRestriction ? 1 : 0));
        out.writeString(this.attractionDescription);
        out.writeInt((byte) (this.hasWaitTime ? 1 : 0));
        out.writeString(this.attractionImage);
        out.writeString(this.attractionImageSmall);
        out.writeInt((byte) (this.mustSee ? 1 : 0));
        out.writeInt((byte) (this.isLive ? 1 : 0));
        out.writeString(this.fastPassReturn);
        out.writeInt((byte) (this.fastPass ? 1 : 0));
    }

    public static final Creator<Attraction> CREATOR = new Creator<Attraction>() {
        public Attraction createFromParcel(Parcel in) {
            return new Attraction(in);
        }

        public Attraction[] newArray(int size) {
            return new Attraction[size];
        }
    };

    private Attraction(Parcel in) {
        this.name = in.readString();
        this.waitTime = in.readString();
        this.longitude = in.readDouble();
        this.latitude = in.readDouble();
        this.updated = (DateTime) in.readSerializable();
        this.disabledAccess = in.readByte() != 0;
        this.singleRider = in.readByte() != 0;
        this.heightRestriction = in.readByte() != 0;
        this.attractionDescription = in.readString();
        this.hasWaitTime = in.readByte() != 0;
        this.attractionImage = in.readString();
        this.attractionImageSmall = in.readString();
        this.mustSee = in.readByte() != 0;
        this.isLive = in.readByte() != 0;
        this.fastPassReturn = in.readString();
        this.fastPass = in.readByte() != 0;
    }
}
