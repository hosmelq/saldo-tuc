package com.socialimprover.saldotuc.api.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Agency implements Parcelable {

    public String address;
    public String name;
    public double lat;
    public double lng;

    public Agency() {
    }

    public Agency(Parcel in) {
        address = in.readString();
        name = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(name);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
    }

    public static final Creator<Agency> CREATOR = new Creator<Agency>() {
        @Override
        public Agency createFromParcel(Parcel source) {
            return new Agency(source);
        }

        @Override
        public Agency[] newArray(int size) {
            return new Agency[size];
        }
    };
}
