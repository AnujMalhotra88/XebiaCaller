package in.xebia.xebiacaller.beans;

import android.os.Parcel;
import android.os.Parcelable;

public class ContactBean implements Parcelable, Comparable<ContactBean> {

    public static final Creator<ContactBean> CREATOR = new Creator<ContactBean>() {
        public ContactBean createFromParcel(Parcel in) {
            return new ContactBean(in);
        }

        public ContactBean[] newArray(int size) {
            return new ContactBean[size];
        }
    };
    protected String name;
    protected String number;

    public ContactBean() {

    }

    public ContactBean(Parcel in) {
        readFromParcel(in);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    private void readFromParcel(Parcel in) {
        setName(in.readString());
        setNumber(in.readString());
    }

    @Override
    public void writeToParcel(Parcel dest, int arg1) {
        dest.writeString(name);
        dest.writeString(number);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof ContactBean) {
            return this.getNumber().trim().equals(((ContactBean) obj).getNumber().trim());
        }
        return false;
    }

    @Override
    public int compareTo(ContactBean another) {
        if (this.getName() == null || another == null || another.getName() == null)
            return 0;
        return this.getName().toLowerCase().compareTo(another.getName().toLowerCase());
    }
}
