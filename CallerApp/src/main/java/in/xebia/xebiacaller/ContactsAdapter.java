package in.xebia.xebiacaller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.xebia.xebiacaller.beans.ContactBean;
import in.xebia.xebiacaller.beans.XebiaContact;
import in.xebia.xebiacaller.util.XebiaContactsDB;


public class ContactsAdapter extends ArrayAdapter<ContactBean> implements Filterable {

    private ArrayList<ContactBean> contacts;
    private ArrayList<ContactBean> orgContacts;
    private Context context;
    private int resId;
    private ExecutorService executorService;
    private XebiaContactsDB xebiaContactsDB;
    private Handler handler;


    public ContactsAdapter(Context context, int resId, ArrayList<ContactBean> _contacts) {
        super(context, resId);
        this.context = context;
        this.contacts = _contacts;
        this.orgContacts = _contacts;
        this.resId = resId;
        this.executorService = Executors.newSingleThreadExecutor();
        this.xebiaContactsDB = new XebiaContactsDB(context);
        this.handler = new Handler();
    }

    @Override
    public int getViewTypeCount() {
        return contacts.size();
    }

    @Override
    public int getCount() {
        return contacts.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            v = inflater.inflate(resId, null, true);
            viewHolder = new ViewHolder();
            viewHolder.tName = (TextView) v.findViewById(R.id.tContactName);
            viewHolder.tNumber = (TextView) v.findViewById(R.id.tContactNumber);
            viewHolder.tDesLocation = (TextView) v.findViewById(R.id.tDesLocation);
            viewHolder.ibCall = (ImageButton) v.findViewById(R.id.ibCall);
            viewHolder.ibMessage = (ImageButton) v.findViewById(R.id.ibMessage);
            viewHolder.ivProfile = (ImageView) v.findViewById(R.id.iProfilePic);
            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }

        final ContactBean contactBean = contacts.get(position);
        if (contactBean != null) {
            viewHolder.ivProfile.setImageDrawable(context.getResources().getDrawable(R.drawable.contact));
            if (contactBean instanceof XebiaContact) {
                v.setBackgroundColor(context.getResources().getColor(R.color.xebia_color_trans));
                viewHolder.tDesLocation.setVisibility(View.VISIBLE);
                StringBuffer stringBuffer = new StringBuffer();
                if (((XebiaContact) contactBean).getDesignation() != null)
                    stringBuffer.append(((XebiaContact) contactBean).getDesignation());
                if (((XebiaContact) contactBean).getLocation() != null) {
                    stringBuffer.append(", ").append(((XebiaContact) contactBean).getLocation());
                }
                if (stringBuffer.length() > 0) {
                    viewHolder.tDesLocation.setText(stringBuffer);
                }
                if (((XebiaContact) contactBean).getImage() == null) {
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            final byte[] imgArray = xebiaContactsDB.getImage(contactBean.getNumber());
                            if (imgArray != null) {
                                final Bitmap bitmap = BitmapFactory.decodeByteArray(imgArray, 0, imgArray.length);
                                if (bitmap != null) {
                                    ((XebiaContact) contactBean).setImage(imgArray);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            notifyDataSetChanged();
                                        }
                                    });
                                }
                            }
                        }
                    });
                } else {
                    final byte[] imgArray = ((XebiaContact) contactBean).getImage();
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(imgArray, 0, imgArray.length);
                    if (bitmap != null) {
                        viewHolder.ivProfile.setImageBitmap(bitmap);
                    }
                }
            } else {
                v.setBackgroundColor(context.getResources().getColor(android.R.color.white));
                viewHolder.tDesLocation.setVisibility(View.GONE);
                viewHolder.tDesLocation.setText("");
            }
            viewHolder.tName.setText(contactBean.getName());
            viewHolder.tNumber.setText(contactBean.getNumber());
            viewHolder.tDesLocation.setSelected(true);
            viewHolder.ibCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contactBean.getNumber())));
                }
            });
            viewHolder.ibMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", contactBean.getNumber(), null)));
                }
            });
        }

        return v;
    }

    static class ViewHolder {
        public TextView tName;
        public TextView tNumber;
        public TextView tDesLocation;
        public ImageButton ibCall;
        public ImageButton ibMessage;
        public ImageView ivProfile;
    }

    private Filter myFilter;

    @Override
    public Filter getFilter() {
        if (myFilter == null)
            myFilter = new ContactFilter();
        return myFilter;
    }

    private class ContactFilter extends Filter {
        private final Object mLock = new Object();

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                synchronized (mLock) {
                    orgContacts.clear();
                    results.values = orgContacts;
                    results.count = orgContacts.size();
                }
            } else {
                ArrayList<ContactBean> mContactList = new ArrayList<ContactBean>();
                synchronized (mLock) {
                    for (ContactBean p : orgContacts) {
                        if (p.getName().toLowerCase().contains(constraint.toString().toLowerCase()) ||
                                p.getNumber().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            mContactList.add(p);
                        } else {
                            if (p instanceof XebiaContact) {
                                if (((XebiaContact) p).getDesignation().toLowerCase().contains(constraint.toString().toLowerCase()) ||
                                        ((XebiaContact) p).getLocation().toLowerCase().contains(constraint.toString().toLowerCase())) {
                                    mContactList.add(p);
                                }
                            }
                        }
                    }
                    results.values = mContactList;
                    results.count = mContactList.size();
                }
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                                   FilterResults results) {
            if (results.count == 0) {
                clear();
            } else {
                contacts = (ArrayList<ContactBean>) results.values;
            }
            notifyDataSetChanged();
        }

    }
}
