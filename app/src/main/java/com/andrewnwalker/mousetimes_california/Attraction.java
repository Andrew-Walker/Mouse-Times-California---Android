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
        name = retrievedName;
        waitTime = retrievedWaitTime;
        longitude = retrievedLongitude;
        latitude = retrievedLatitude;
        updated = retrievedUpdated;
        disabledAccess = retrievedDisabledAccess;
        singleRider = retrievedSingleRider;
        heightRestriction = retrievedHeightRestriction;
        attractionDescription = retrievedAttractionDescription;
        hasWaitTime = retrievedHasWaitTime;
        attractionImage = retrievedAttractionImage;
        attractionImageSmall = retrievedAttractionImageSmall;
        mustSee = retrievedMustSee;
        isLive = retrievedIsLive;
        fastPassReturn = retrievedFastPassReturn;
        fastPass = retrievedFastPass;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeString(waitTime);
        out.writeDouble(longitude);
        out.writeDouble(latitude);
        out.writeSerializable(updated);
        out.writeInt((byte) (disabledAccess ? 1 : 0));
        out.writeInt((byte) (singleRider ? 1 : 0));
        out.writeInt((byte) (heightRestriction ? 1 : 0));
        out.writeString(attractionDescription);
        out.writeInt((byte) (hasWaitTime ? 1 : 0));
        out.writeString(attractionImage);
        out.writeString(attractionImageSmall);
        out.writeInt((byte) (mustSee ? 1 : 0));
        out.writeInt((byte) (isLive ? 1 : 0));
        out.writeString(fastPassReturn);
        out.writeInt((byte) (fastPass ? 1 : 0));
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
        name = in.readString();
        waitTime = in.readString();
        longitude = in.readDouble();
        latitude = in.readDouble();
        updated = (DateTime) in.readSerializable();
        disabledAccess = in.readByte() != 0;
        singleRider = in.readByte() != 0;
        heightRestriction = in.readByte() != 0;
        attractionDescription = in.readString();
        hasWaitTime = in.readByte() != 0;
        attractionImage = in.readString();
        attractionImageSmall = in.readString();
        mustSee = in.readByte() != 0;
        isLive = in.readByte() != 0;
        fastPassReturn = in.readString();
        fastPass = in.readByte() != 0;
    }
}
