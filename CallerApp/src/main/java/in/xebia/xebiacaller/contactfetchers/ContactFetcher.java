package in.xebia.xebiacaller.contactfetchers;

import android.content.Context;

import java.util.ArrayList;

import in.xebia.xebiacaller.beans.ContactBean;

/**
 * Created by anujmalhotra on 28/11/14.
 */
public interface ContactFetcher {

    void setContext(Context context);

    ArrayList<? extends ContactBean> fetch(String data);
}
