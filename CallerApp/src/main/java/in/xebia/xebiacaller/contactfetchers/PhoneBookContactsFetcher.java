package in.xebia.xebiacaller.contactfetchers;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;

import java.util.ArrayList;

import in.xebia.xebiacaller.beans.ContactBean;

/**
 * Created by anujmalhotra on 28/11/14.
 */
public class PhoneBookContactsFetcher implements ContactFetcher {

    private static final String[] NAME_PROJECTION = {
            ContactsContract.Contacts._ID,
            Build.VERSION.SDK_INT
                    >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                    ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.HAS_PHONE_NUMBER
    };

    private static final String NAME_SELECTION =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                    ContactsContract.Contacts.DISPLAY_NAME;

    private static final String[] NUMBER_PROJECTION = {
            ContactsContract.Contacts._ID,
            Build.VERSION.SDK_INT
                    >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                    ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Event.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };

    private Context context;

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public ArrayList<ContactBean> fetch(String data) {
        if (data == null)
            return null;
        ArrayList<ContactBean> contactBeansByName;
        ArrayList<ContactBean> contactBeansByNumber;
        contactBeansByName = getContactsByName(data);
        contactBeansByNumber = getContactsByNumber(data);
        if (contactBeansByName != null) {
            if (contactBeansByNumber != null) {
                contactBeansByName.addAll(contactBeansByNumber);
            }
            return contactBeansByName;
        }
        if (contactBeansByNumber != null)
            return contactBeansByNumber;
        return null;
    }

    private ArrayList<ContactBean> getContactsByName(String name) {
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
        Cursor cursor = context.getContentResolver().query(uri, NAME_PROJECTION, NAME_SELECTION + " LIKE'" + name + "%'", null, sortOrder);

        ArrayList<ContactBean> contactBeans = null;
        try {
            if (cursor != null && cursor.getCount() > 0) {
                contactBeans = new ArrayList<ContactBean>();
                while (cursor.moveToNext()) {
                    ContactBean contactBean = new ContactBean();
                    String id = cursor.getString(0);
                    contactBean.setName(cursor.getString(1));
                    int numCount = Integer.parseInt(cursor.getString(2));
                    if (numCount > 0) {
                        Cursor phoneCursor = null;
                        try {
                            phoneCursor = context.getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id
                                    }, ContactsContract.Contacts.DISPLAY_NAME + " ASC"
                            );
                            if (phoneCursor != null && phoneCursor.getCount() > 0) {
                                phoneCursor.moveToNext();
                                contactBean.setNumber(phoneCursor.getString(phoneCursor
                                        .getColumnIndex(ContactsContract.CommonDataKinds.
                                                Phone.NUMBER)));
                                contactBeans.add(contactBean);
                            }
                        } finally {
                            if (phoneCursor != null)
                                phoneCursor.close();
                        }
                    }
                }
            }
            return contactBeans;
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }


    private ArrayList<ContactBean> getContactsByNumber(String number) {

        Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                NUMBER_PROJECTION,
                ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE ?",
                new String[]{"%" + number + "%"},
                null);
        try {
            ArrayList<ContactBean> contactBeans = null;
            if (cursor != null && cursor.getCount() > 0) {
                contactBeans = new ArrayList<ContactBean>();
                while (cursor.moveToNext()) {
                    ContactBean contactBean = new ContactBean();
                    contactBean.setName(cursor.getString(1));
                    contactBean.setNumber(cursor.getString(3));
                    contactBeans.add(contactBean);
                }
            }
            return contactBeans;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
