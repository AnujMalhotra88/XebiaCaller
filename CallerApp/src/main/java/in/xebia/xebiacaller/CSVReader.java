package in.xebia.xebiacaller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import in.xebia.xebiacaller.beans.XebiaContact;
import in.xebia.xebiacaller.util.Functions;

/**
 * Created by anujmalhotra on 29/11/14.
 */
public final class CSVReader {

    public static final ArrayList<XebiaContact> read(InputStream inputStream) {
        ArrayList<XebiaContact> contacts = new ArrayList<XebiaContact>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String csvLine;
            while ((csvLine = reader.readLine()) != null) {
                try {
                    XebiaContact contact = new XebiaContact();
                    String[] row = csvLine.split(",");
                    for (int i = 0; i < row.length; i++) {
                        switch (i) {
                            case 0:
                                contact.setName(row[0]);
                                break;
                            case 1:
                                contact.setNumber(row[1]);
                                break;
                            case 2:
                                contact.setDesignation(row[2]);
                                break;
                            case 3:
                                contact.setLocation(row[3]);
                                break;
                            case 4:
                                try {
                                    contact.setImage(Functions.getImage(row[4]));
                                } catch (IOException e) {
                                }
                                break;
                        }
                    }
                    contacts.add(contact);
                } catch (Exception e) {

                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: " + ex);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException("Error while closing input stream: " + e);
            }
        }
        return contacts;
    }
}
