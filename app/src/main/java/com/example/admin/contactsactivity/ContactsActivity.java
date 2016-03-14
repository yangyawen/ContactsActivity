package com.example.admin.contactsactivity;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.SimpleCursorAdapter;

public class ContactsActivity extends FragmentActivity {

    private static final int ROOT_ID = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout rootView = new FrameLayout(this);
        rootView.setId(R.id.frame);
        setContentView(rootView);

        /*
        getSupportFragmentManager().beginTransaction()
                .add(R.id.frame, ContactsFragment.newInstance())
                .commit();*/
    }

    public static class ContactsFragment extends ListFragment implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>{

        public static ContactsFragment newInstance(){
            return new ContactsFragment();
        }

        private SimpleCursorAdapter mAdapter;

        @Override
        public void onActivityCreated(Bundle savedInstanceState){
            super.onActivityCreated(savedInstanceState);
            mAdapter = new SimpleCursorAdapter(getActivity(),
                    android.R.layout.simple_list_item_1, null,
                    new String[]{ContactsContract.Contacts.DISPLAY_NAME},
                    new int[]{android.R.id.text1},
                    0);
            setListAdapter(mAdapter);
            getListView().setOnItemClickListener(this);
            getLoaderManager().initLoader(0, null, this);

        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args){
            String[] projection = new String[]{
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME
            };
            return new CursorLoader(getActivity(),
                    ContactsContract.Contacts.CONTENT_URI,
                    projection, null, null,
                    ContactsContract.Contacts.DISPLAY_NAME);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data){
            mAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader){
            mAdapter.swapCursor(null);
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id){
            final Cursor contacts = mAdapter.getCursor();
            if(contacts.moveToPosition(position)){
                int selectedId = contacts.getInt(0);

                Cursor email = getActivity().getContentResolver()
                        .query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                new String[]{ContactsContract.CommonDataKinds.Email.DATA},
                                ContactsContract.Data.CONTACT_ID + " = " + selectedId, null, null);

                Cursor phone = getActivity().getContentResolver()
                        .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                                ContactsContract.Data.CONTACT_ID + " = " + selectedId, null, null);

                Cursor address = getActivity().getContentResolver()
                        .query(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
                                new String[]{ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS},
                                ContactsContract.Data.CONTACT_ID+" = "+selectedId, null, null);

                //建立对话框消息
                StringBuilder sb = new StringBuilder();
                sb.append(email.getCount()+" Email\n");
                if(email.moveToFirst()){
                    do{
                        sb.append("Email: " + email.getString(0));
                        sb.append('\n');
                    }while (email.moveToNext());
                    sb.append('\n');
                }
                sb.append(phone.getCount() + " Phone Number\n");
                if(phone.moveToFirst()){
                    do{
                        sb.append("Phone: " + phone.getString(0));
                        sb.append('\n');
                    }while (phone.moveToNext());
                    sb.append('\n');
                }
                sb.append(address.getCount() + " Address\n");
                if(address.moveToFirst()){
                    do{
                        sb.append("Address: " + address.getString(0));
                        sb.append('\n');
                    }while (address.moveToNext());
                    sb.append('\n');
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(contacts.getString(1));
                builder.setMessage(sb.toString());
                builder.setPositiveButton("OK", null);
                builder.create().show();
                //关闭临时光标
                email.close();
                phone.close();
                address.close();
            }
        }

    }
}
