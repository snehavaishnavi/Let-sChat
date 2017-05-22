package com.example.amrut.easychatapplication;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by amrut on 11/22/2016.
 */

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.ViewHolder> {

    private ArrayList<InboxObj> mData;
    private Context mContext;
    private DatabaseReference mDatabase= FirebaseDatabase.getInstance().getReference();
    private SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private SimpleDateFormat dateFormat1=new SimpleDateFormat("MMM dd,EEE HH:mm");
    User receiverUser;
    InboxInterface inboxInterfaceListener;

    public InboxAdapter(ArrayList<InboxObj> mData, Context mContext) {
        Log.d("demo","inConstructor");
        this.mData = mData;
        this.mContext = mContext;
        inboxInterfaceListener = (InboxInterface) mContext;
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("demo","onside onCreateViewHolder");
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.inbox_layout, parent, false);
        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.d("demo","onBindVieInboxAdapter"+mData.get(position).toString());
        final InboxObj inboxObj=mData.get(position);

        final ImageView inboxUserPic=holder.inboxUserPic;
        final TextView inboxUserName=holder.inboxUserName;
        TextView inboxMsgTimeStamp=holder.inboxMsgTimeStamp;
        TextView inboxMsgContent=holder.inboxMsgContent;
        ImageView inboxMsgIsRead=holder.inboxMsgIsRead;
        View container=holder.container;


        if (inboxObj.getSenderID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            Log.d("demo","bindHolder sender matched");

            mDatabase.child("users").child(inboxObj.getReceiverID()).child("profile").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    receiverUser = dataSnapshot.getValue(User.class);
                    Log.d("demo","receiverUserprofile"+receiverUser.toString());
                    inboxUserName.setText(receiverUser.getFirstName()+" "+receiverUser.getLastName());

                    if(receiverUser.getUserPicUrl() != null && !receiverUser.getUserPicUrl().isEmpty()){
                        Picasso.with(getContext()).load(receiverUser.getUserPicUrl()).into(inboxUserPic);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }else {
            Log.d("demo","bindHolder receiver matched");
            mDatabase.child("users").child(inboxObj.getSenderID()).child("profile").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    receiverUser = dataSnapshot.getValue(User.class);
                    Log.d("demo","receiverUser  profile"+receiverUser.getUserPicUrl());
                    inboxUserName.setText(receiverUser.getFirstName()+" "+receiverUser.getLastName());

                    if(!receiverUser.getUserPicUrl().isEmpty()){
                        Log.d("demo","inboxpicnotnul");
                        Picasso.with(getContext()).load(receiverUser.getUserPicUrl()).into(inboxUserPic);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("demo","clickedInboxItem");
                    InboxObj current_object = inboxObj;
                    current_object.setIslastMsgSeen(true);
                        mDatabase.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("inboxObjs").child(receiverUser.getUid()).setValue(current_object);
                        inboxInterfaceListener.onItemClick(receiverUser);
                }
            });
        }

        inboxMsgContent.setText(inboxObj.getLastMsg().getMsgContent());
        Log.d("demo","islastmsgseen"+inboxObj.getIslastMsgSeen());
        if (inboxObj.getIslastMsgSeen()){
            inboxMsgIsRead.setVisibility(View.GONE);
        }else {
            inboxMsgIsRead.setVisibility(View.VISIBLE);
        }
        Log.d("demo","lastMsgTimeStamp"+inboxObj.getLastMsg().getMsgTimeStamp());
        try {
            inboxMsgTimeStamp.setText(dateFormat1.format(dateFormat.parse(inboxObj.getLastMsg().getMsgTimeStamp())));
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView inboxUserPic;
        TextView inboxUserName;
        TextView inboxMsgTimeStamp;
        TextView inboxMsgContent;
        ImageView inboxMsgIsRead;
        View container;

        public ViewHolder(View itemView) {
            super(itemView);
            Log.d("demo","ViewHolder");
            container= (View) itemView.findViewById(R.id.rowItemContainer);
            inboxUserPic= (ImageView) itemView.findViewById(R.id.imageViewInboxUserPic);
            inboxUserName= (TextView) itemView.findViewById(R.id.textViewInboxUserName);
            inboxMsgTimeStamp= (TextView) itemView.findViewById(R.id.textViewInboxTimeStamp);
            inboxMsgContent= (TextView) itemView.findViewById(R.id.textViewInboxUserMessage);
            inboxMsgIsRead= (ImageView) itemView.findViewById(R.id.imageViewMsgIsRead);
        }
    }

    public interface InboxInterface{
        public void onItemClick(User user);
    }


}
