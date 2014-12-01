package in.xebia.xebiacaller;

import android.os.Handler;

import in.xebia.xebiacaller.contactfetchers.ContactFetcher;

/**
 * Created by anujmalhotra on 27/11/14.
 */
public class FetchContactRunnable implements Runnable {

    private Handler handler;
    private ContactFetcher contactFetcher;
    private String data;

    public FetchContactRunnable(Handler mHandler, ContactFetcher contactFetcher, String data) {
        this.handler = mHandler;
        this.contactFetcher = contactFetcher;
        this.data = data;
    }

    @Override
    public void run() {
        if (contactFetcher != null && data != null && !data.equals("")) {
            handler.sendMessage(handler.obtainMessage(0, data));
            handler.sendMessage(handler.obtainMessage(1, contactFetcher.fetch(data)));
        } else {
            handler.sendMessage(handler.obtainMessage(-1));
        }
    }
}
