package in.xebia.xebiacaller.contactfetchers;

import android.content.Context;

import java.util.ArrayList;

import in.xebia.xebiacaller.beans.ContactBean;
import in.xebia.xebiacaller.util.XebiaContactsDB;

/**
 * Created by anujmalhotra on 28/11/14.
 */
public class XebiaContactsFetcher implements ContactFetcher {

    private Context context;

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public ArrayList<? extends ContactBean> fetch(String data) {
        XebiaContactsDB xebiaContactsDB = new XebiaContactsDB(context);
        return xebiaContactsDB.getContacts(data);
    }
}
