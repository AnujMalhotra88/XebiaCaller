package in.xebia.xebiacaller.beans;

import android.os.Parcel;
import android.os.Parcelable;

public class XebiaContact extends ContactBean {

    public static final Parcelable.Creator<XebiaContact> CREATOR = new Parcelable.Creator<XebiaContact>() {
        public XebiaContact createFromParcel(Parcel in) {
            return new XebiaContact(in);
        }

        public XebiaContact[] newArray(int size) {
            return new XebiaContact[size];
        }
    };

    protected String designation;
    protected String location;
    protected byte[] image;

    public XebiaContact() {

    }

    public XebiaContact(Parcel in) {
        readFromParcel(in);
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    private void readFromParcel(Parcel in) {
        setName(in.readString());
        setNumber(in.readString());
        setDesignation(in.readString());
        setLocation(in.readString());
        in.readByteArray(image);
    }

    @Override
    public void writeToParcel(Parcel dest, int arg1) {
        dest.writeString(name);
        dest.writeString(number);
        dest.writeString(designation);
        dest.writeString(location);
        dest.writeByteArray(image);
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
