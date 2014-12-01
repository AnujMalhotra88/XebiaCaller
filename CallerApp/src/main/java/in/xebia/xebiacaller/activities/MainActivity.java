package in.xebia.xebiacaller.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.xebia.xebiacaller.CSVReader;
import in.xebia.xebiacaller.ContactsAdapter;
import in.xebia.xebiacaller.FetchContactRunnable;
import in.xebia.xebiacaller.R;
import in.xebia.xebiacaller.XebiaContactsParser;
import in.xebia.xebiacaller.beans.ContactBean;
import in.xebia.xebiacaller.beans.XebiaContact;
import in.xebia.xebiacaller.contactfetchers.ContactFetcher;
import in.xebia.xebiacaller.contactfetchers.PhoneFetcher;
import in.xebia.xebiacaller.util.XebiaContactsDB;


public class MainActivity extends Activity {

    private ListView lvContent;
    private Context context;
    private ArrayList<ContactBean> contactBeans;
    private ExecutorService executorService;
    private ContactsAdapter contactsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText eNameNumber = (EditText) findViewById(R.id.eNameNumber);
        lvContent = (ListView) findViewById(R.id.lvContent);

        eNameNumber.addTextChangedListener(textWatcher);
        executorService = Executors.newSingleThreadExecutor();
        this.context = this;

        // TODO : To be Removed to populate DB
        populateDB();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    // TODO: Temp code to populate contacts db to be removed later
    private void populateDB() {
        ExecutorService readFileExecutor = Executors.newSingleThreadExecutor();
        readFileExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    File dir = new File(Environment.getExternalStorageDirectory() + "/xebia/");
                    if (!(dir.exists() && dir.isDirectory())) {
                        dir.mkdir();
                    }
                    File contactFile = new File(Environment.getExternalStorageDirectory() + "/xebia/xebiacontacts.json");
                    if (contactFile.exists() && contactFile.isFile()) {
                        InputStream is = new FileInputStream(contactFile);
                        ArrayList<XebiaContact> contacts = XebiaContactsParser.parse(is);
                        if (contacts != null && contacts.size() > 0) {
                            XebiaContactsDB xebiaContactsDB = new XebiaContactsDB(context);
                            for (XebiaContact contact : contacts) {
                                xebiaContactsDB.insertContact(contact);
                            }
                            contactFile.delete();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        readFileExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    File dir = new File(Environment.getExternalStorageDirectory() + "/xebia/");
                    if (!(dir.exists() && dir.isDirectory())) {
                        dir.mkdir();
                    }
                    File contactFile = new File(Environment.getExternalStorageDirectory() + "/xebia/xebiacontacts.csv");
                    if (contactFile.exists() && contactFile.isFile()) {
                        InputStream is = new FileInputStream(contactFile);
                        ArrayList<XebiaContact> contacts = CSVReader.read(is);
                        if (contacts != null && contacts.size() > 0) {
                            XebiaContactsDB xebiaContactsDB = new XebiaContactsDB(context);
                            for (XebiaContact contact : contacts) {
                                xebiaContactsDB.insertContact(contact);
                            }
                            contactFile.delete();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: {
                    if (msg.obj != null) {

                    }
                    break;
                }
                case -1: {
                    if (contactBeans != null)
                        contactBeans.clear();
                    if (contactsAdapter != null) {
                        contactsAdapter.clear();
                        contactsAdapter.notifyDataSetChanged();
                    }
                    break;
                }
                case 1: {
                    if (msg.obj != null) {
                        if (contactBeans == null)
                            contactBeans = new ArrayList<ContactBean>();
                        contactBeans.addAll((ArrayList<ContactBean>) msg.obj);
                        if (contactBeans.size() > 0) {
                            Collections.sort(contactBeans);
                            if (contactsAdapter == null) {
                                contactsAdapter = new ContactsAdapter(context, R.layout.row_contacts, contactBeans);
                                lvContent.setAdapter(contactsAdapter);
                            } else {
                                contactsAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                    break;
                }
            }
        }
    };

    TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(final Editable s) {
            if (s.toString().trim().equals("")) {
                contactBeans.clear();
                contactsAdapter = null;
                lvContent.setAdapter(null);
            }
            if (contactBeans == null || contactBeans.size() == 0) {
                ContactFetcher contactFetcher = PhoneFetcher.getFetcher(PhoneFetcher.XebiaContactsFetcher);
                contactFetcher.setContext(context);
                executorService.submit(new FetchContactRunnable(mHandler, contactFetcher, s.toString().trim()));
                contactFetcher = PhoneFetcher.getFetcher(PhoneFetcher.PhoneBookContactsFetcher);
                contactFetcher.setContext(context);
                executorService.submit(new FetchContactRunnable(mHandler, contactFetcher, s.toString().trim()));
            } else {
                if (contactsAdapter != null)
                    contactsAdapter.getFilter().filter(s.toString().trim());
            }
        }
    };
}
