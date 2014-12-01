package in.xebia.xebiacaller.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import in.xebia.xebiacaller.beans.XebiaContact;

public class XebiaContactsDB {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "xebiaDB";
    private static final String XEBIA_CONTACTS = "xebiacontacts";
    private static final String CREATE_TABLE_WORKOUTS = "create table " + XEBIA_CONTACTS
            + " (number TEXT PRIMARY KEY, name TEXT NOT NULL, designation TEXT, location TEXT, image BLOB)";
    private SQLiteDatabase db;
    private OpenHelper openHelper;

//    private String[] desigantions = {"Developer", "Analyst", "Consultant", "Principal Consultant", "CTO"};
//    private String[] locations = {"Netherlands", "France", "USA", "India"};

    public XebiaContactsDB(Context context) {
        openHelper = new OpenHelper(context);
    }

    private void close() {
        db.close();
    }

    private void open() {
        db = openHelper.getWritableDatabase();
    }

//    public void setupDatabase() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                NameGenerator nameGenerator = new NameGenerator();
//                Random rand = new Random();
//                for (int i = 0; i < 100; i++) {
//                    XebiaContact xebiaContact = new XebiaContact();
//                    xebiaContact.setNumber(String.valueOf((rand.nextInt((9 - 5) + 1) + 5) * 123456789));
//                    xebiaContact.setName(nameGenerator.getName());
//                    xebiaContact.setDesignation(desigantions[(rand.nextInt(4) + 1) + 4]);
//                    xebiaContact.setLocation(desigantions[(rand.nextInt(3) + 1) + 3]);
//                    insertContact(xebiaContact);
//                }
//            }
//        }).start();
//    }

    public boolean insertContact(XebiaContact xebiaContact) {
        if (xebiaContact != null) {
            ContentValues values = new ContentValues();
            values.put("number", xebiaContact.getNumber());
            values.put("name", xebiaContact.getName());
            values.put("designation", xebiaContact.getDesignation());
            values.put("location", xebiaContact.getLocation());
            values.put("image", xebiaContact.getImage());
            open();
            boolean a = (db.insert(XEBIA_CONTACTS, null, values) > 0);
            close();
            return a;
        }
        return false;
    }

    public byte[] getImage(String number) {
        if (number == null)
            return null;
        open();
        Cursor c = null;
        try {
            c = db.query(XEBIA_CONTACTS,
                    new String[]{"image"},
                    "number =  '" + number + "'", null, null, null, null);
            if (c.moveToFirst()) {
                return c.getBlob(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        close();
        return null;
    }

    public ArrayList<XebiaContact> getContacts(String data) {
        if (data == null)
            return null;
        open();
        ArrayList<XebiaContact> xebiaContacts = null;
        Cursor c = null;
        try {
            c = db.query(XEBIA_CONTACTS,
                    new String[]{"number", "name", "designation", "location"},
                    "name like  '%"
                            + data
                            + "%' or number like '%"
                            + data
                            + "%' or designation like '%"
                            + data
                            + "%' or location like '%"
                            + data + "%'", null, null, null, "name asc"
            );
            if (c.moveToFirst()) {
                xebiaContacts = new ArrayList<XebiaContact>();
                do {
                    XebiaContact xebiaContact = new XebiaContact();
                    xebiaContact.setNumber(c.getString(0));
                    xebiaContact.setName(c.getString(1));
                    xebiaContact.setDesignation(c.getString(2));
                    xebiaContact.setLocation(c.getString(3));
                    xebiaContacts.add(xebiaContact);
                } while (c.moveToNext());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        close();
        return xebiaContacts;
    }

    private static class OpenHelper extends SQLiteOpenHelper {

        OpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(final SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_WORKOUTS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
