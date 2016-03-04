package com.viraj.example.ribbit;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by viraj on 01-02-2016.
 */
public class InboxFragment extends ListFragment {

    protected List<ParseObject> mMessages;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_inbox, container, false);
       return rootView;
    }



    @Override
    public void onResume() {
        super.onResume();


        ParseQuery<ParseObject> query = new ParseQuery<>(ParseConstant.CLASS_MESSAGES);
        query.whereEqualTo(ParseConstant.KEY_RECIPIENT_IDS, ParseUser.getCurrentUser().getObjectId());
        query.addDescendingOrder(ParseConstant.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> messages, ParseException e) {
                if (e == null) {
                    //we found messages
                    mMessages = messages;
                    String[] usernames = new String[mMessages.size()];
                    int i = 0;
                    for (ParseObject message : mMessages) {
                        usernames[i] = message.getString(ParseConstant.KEY_SENDER_NAME);
                        i++;
                    }

                    if (getListView().getAdapter() == null) {
                        // create a anew adapter
                        MessageAdapter adapter = new MessageAdapter(getListView().getContext(), mMessages);
                        getListView().setAdapter(adapter);
                    } else {
                        //refill the adapter
                        ((MessageAdapter) getListView().getAdapter()).refill(mMessages);
                    }
                }
            }
        });
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ParseObject message = mMessages.get(position);
        String messageType = message.getString(ParseConstant.KEY_FILE_TYPE);
        ParseFile file = message.getParseFile(ParseConstant.KEY_FILE);
        Uri fileUri = Uri.parse(file.getUrl());

        if (messageType.equals(ParseConstant.TYPE_IMAGE)) {
            //view the image
            Intent intent = new Intent(getActivity(), ViewImageActivity.class);
            intent.setData(fileUri);
            startActivity(intent);
        } else {
            //view video
            Intent intent = new Intent(Intent.ACTION_VIEW, fileUri);
            intent.setDataAndType(fileUri, "video/*");
            startActivity(intent);
        }

        //Delete it
        List<String> ids = message.getList(ParseConstant.KEY_RECIPIENT_IDS);
        if (ids.size() == 1){
            //last recipients delete the whole thing
            message.deleteInBackground();
        } else {
            //remove the recipients and save
            ids.remove(ParseUser.getCurrentUser().getObjectId());

            ArrayList<String> idsToRemove = new ArrayList<>();
            ids.add(ParseUser.getCurrentUser().getObjectId());


            message.removeAll(ParseConstant.KEY_RECIPIENT_IDS, idsToRemove);
            message.saveInBackground();

        }

    }
}

















