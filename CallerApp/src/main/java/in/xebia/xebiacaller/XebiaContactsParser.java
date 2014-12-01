package in.xebia.xebiacaller;

import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import in.xebia.xebiacaller.beans.XebiaContact;
import in.xebia.xebiacaller.util.Functions;

public final class XebiaContactsParser {

    public static final ArrayList<XebiaContact> parse(InputStream inputstream) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(inputstream, "UTF-8"));
        return readContactArray(reader);
    }

    private static ArrayList<XebiaContact> readContactArray(JsonReader reader) throws IOException {
        ArrayList<XebiaContact> contacts = new ArrayList();
        reader.beginArray();
        while (reader.hasNext()) {
            contacts.add(readContact(reader));
        }
        reader.endArray();
        return contacts;
    }

    private static XebiaContact readContact(JsonReader reader) throws IOException {
        XebiaContact xebiaContact = new XebiaContact();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("name")) {
                xebiaContact.setName(reader.nextString());
            } else if (name.equals("designation")) {
                xebiaContact.setDesignation(reader.nextString());
            } else if (name.equals("location")) {
                xebiaContact.setLocation(reader.nextString());
            } else if (name.equals("contact")) {
                xebiaContact.setNumber(reader.nextString());
            } else if (name.equals("image")) {
                try {
                    xebiaContact.setImage(Functions.getImage(reader.nextString()));
                } catch (IOException e) {
                }
            } else {
                reader.skipValue();
            }
        }

        reader.endObject();
        return xebiaContact;
    }
}