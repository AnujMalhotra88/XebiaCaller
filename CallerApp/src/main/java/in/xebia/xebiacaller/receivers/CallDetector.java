package in.xebia.xebiacaller.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import in.xebia.xebiacaller.R;
import in.xebia.xebiacaller.FetchContactRunnable;
import in.xebia.xebiacaller.beans.ContactBean;
import in.xebia.xebiacaller.beans.XebiaContact;
import in.xebia.xebiacaller.contactfetchers.ContactFetcher;
import in.xebia.xebiacaller.contactfetchers.PhoneFetcher;
import in.xebia.xebiacaller.util.Functions;
import in.xebia.xebiacaller.util.XebiaContactsDB;

/**
 * Created by anujmalhotra on 28/11/14.
 */
public class CallDetector extends BroadcastReceiver {

    private View view;
    private Context context;
    private int timeToDismiss = 2000;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        if (intent != null) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            MyPhoneStateListener PhoneListener = new MyPhoneStateListener();
            telephonyManager.listen(PhoneListener, PhoneStateListener.LISTEN_CALL_STATE);

            if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
                final String outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                searchContact(outgoingNumber, PhoneFetcher.getFetcher(PhoneFetcher.PhoneBookContactsFetcher));
            }
        }
    }

    private void searchContact(String number, ContactFetcher contactFetcher) {
        if (number == null || contactFetcher == null)
            return;
        contactFetcher.setContext(context);
        if (PhoneNumberUtils.isGlobalPhoneNumber(number)) {
            new Thread(new FetchContactRunnable(mHandler, contactFetcher, number)).start();
        }

    }

    private View initView(ContactBean contactBean) {
        if (contactBean == null)
            return null;
        LayoutInflater inflater = LayoutInflater.from(context);
        View overlay = inflater.inflate(R.layout.contact_display_overlay, null);

        TextView tName = (TextView) overlay.findViewById(R.id.tContactName);
        TextView tNumber = (TextView) overlay.findViewById(R.id.tContactNumber);
        TextView tDesLocation = (TextView) overlay.findViewById(R.id.tDesLocation);
        ImageView ivProfilePic = (ImageView) overlay.findViewById(R.id.iProfilePic);


        if (contactBean instanceof XebiaContact) {
            StringBuffer stringBuffer = new StringBuffer();
            if (((XebiaContact) contactBean).getDesignation() != null)
                stringBuffer.append(((XebiaContact) contactBean).getDesignation());
            if (((XebiaContact) contactBean).getLocation() != null) {
                stringBuffer.append(", ").append(((XebiaContact) contactBean).getLocation());
            }
            if (stringBuffer.length() > 0) {
                tDesLocation.setText(stringBuffer);
            }
        } else {
            tDesLocation.setText("");
        }
        tName.setText(contactBean.getName());
        tNumber.setText(contactBean.getNumber());
        final byte[] imgArray = new XebiaContactsDB(context).getImage(contactBean.getNumber());
        if (imgArray != null) {
            final Bitmap bitmap = BitmapFactory.decodeByteArray(imgArray, 0, imgArray.length);
            if (bitmap != null) {
                ivProfilePic.setImageBitmap(bitmap);
            }
        }
        return overlay;
    }

    private void setupWM(final View view) {
        final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                Functions.convertDpToPixel(150, context), WindowManager.LayoutParams.TYPE_SYSTEM_ALERT |
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT
        );

        params.format = PixelFormat.TRANSLUCENT;
        params.gravity = Gravity.TOP;
        // Slight Delay to display our alert on top of others
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                wm.addView(view, params);
            }
        }, 1000);
    }

    private class MyPhoneStateListener extends PhoneStateListener {

        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING: {
                    searchContact(incomingNumber, PhoneFetcher.getFetcher(PhoneFetcher.PhoneBookContactsFetcher));
                }
                case TelephonyManager.CALL_STATE_IDLE: {
                    if (view != null) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                                wm.removeView(view);
                                view = null;
                            }
                        }, timeToDismiss);
                    }
                }
            }
        }
    }    private Handler mHandler = new Handler() {

        private String searchedNumber = "";
        private boolean lookXebiaContacts = true;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: {
                    if (msg.obj != null)
                        searchedNumber = (String) msg.obj;
                    break;
                }
                case 1: {
                    if (msg.obj != null) {
                        ArrayList<ContactBean> contactBeans = (ArrayList<ContactBean>) msg.obj;
                        ContactBean contactBean = null;
                        if (contactBeans.size() > 0 && searchedNumber != null) {
                            for (ContactBean cb : contactBeans) {
                                if (PhoneNumberUtils.compare(context, searchedNumber, cb.getNumber())) {
                                    contactBean = cb;
                                    if (contactBean instanceof XebiaContact) {
                                        break;
                                    } else {
                                        return;
                                    }
                                }
                            }
                            if (contactBean == null && lookXebiaContacts) {
                                searchContact(searchedNumber, PhoneFetcher.getFetcher(PhoneFetcher.XebiaContactsFetcher));
                                lookXebiaContacts = false;
                                return;
                            }
                            if (contactBean != null) {
                                view = initView(contactBean);
                                if (view != null) {
                                    setupWM(view);
                                }
                            }
                        }
                    } else {
                        if (lookXebiaContacts) {
                            searchContact(searchedNumber, PhoneFetcher.getFetcher(PhoneFetcher.XebiaContactsFetcher));
                            lookXebiaContacts = false;
                        }
                    }
                    break;
                }
            }
        }
    };



}
