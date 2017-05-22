package com.example.amrut.easychatapplication;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by amrut on 11/22/2016.
 */

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Message> mData;
    PrettyTime prettyTime=new PrettyTime();
    private SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private DatabaseReference mDatabase= FirebaseDatabase.getInstance().getReference();

    public ConversationAdapter(Context mContext, ArrayList<Message> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public ConversationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        Log.d("demo","inside oncreateViewHolder");
        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.chat_layout, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ConversationAdapter.ViewHolder holder, int position) {
        Log.d("demo","inside bindViewholder"+mData.get(position));
        final Message message=mData.get(position);
        TextView senderMsg= holder.senderMsg;
        TextView receiverMsg=holder.receiverMsg;
        ImageView senderImgMsg=holder.senderImgMsg;
        ImageView receiverImgMsg=holder.receiverImgMsg;
        TextView senderSentTime=holder.senderSentTime;
        TextView receiverSentTime=holder.receiverSentTime;

        LinearLayout senderMsgLayout=holder.senderMsgLayout;
        LinearLayout receiverMsgLayout=holder.receiverMsgLayout;

        if (message.getSenderId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            receiverMsgLayout.setVisibility(View.GONE);
            senderMsgLayout.setVisibility(View.VISIBLE);
            if (!message.getMsgContent().isEmpty() && message.getMsgContent()!=null){
                senderMsg.setText(message.getMsgContent());
            }else {
                senderMsg.setVisibility(View.GONE);
            }
            if (!message.getImgMsgUrl().isEmpty() && message.getImgMsgUrl()!=null){
                Picasso.with(getContext()).load(message.getImgMsgUrl()).into(senderImgMsg);
            }else {
                senderImgMsg.setVisibility(View.GONE);
            }
            try {
                senderSentTime.setText(prettyTime.format(dateFormat.parse(message.getMsgTimeStamp())));
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }else if (message.getReceiverId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            senderMsgLayout.setVisibility(View.GONE);
            receiverMsgLayout.setVisibility(View.VISIBLE);
            if (!message.getMsgContent().isEmpty() && message.getMsgContent()!=null){
                receiverMsg.setText(message.getMsgContent());
            }else {
                receiverMsg.setVisibility(View.GONE);
            }
            if (!message.getImgMsgUrl().isEmpty() && message.getImgMsgUrl()!=null){
                Picasso.with(getContext()).load(message.getImgMsgUrl()).into(receiverImgMsg);
            }else {
                receiverImgMsg.setVisibility(View.GONE);
            }
            try {
                receiverSentTime.setText(prettyTime.format(dateFormat.parse(message.getMsgTimeStamp())));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        senderMsgLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mDatabase.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("chatroom")
                        .child(message.getReceiverId()).child("messages").child(message.getMsgKey()).removeValue();
                Toast.makeText(getContext(),"Message deleted",Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        receiverMsgLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mDatabase.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("chatroom")
                        .child(message.getSenderId()).child("messages").child(message.getMsgKey()).removeValue();
                Toast.makeText(getContext(),"Message deleted",Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        Log.d("demo","inside getItemcount: "+mData.size());
        return mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView senderMsg,receiverMsg,senderSentTime,receiverSentTime;
        ImageView senderImgMsg,receiverImgMsg;
        LinearLayout senderMsgLayout,receiverMsgLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            Log.d("demo","inside viewholder");

            senderMsg= (TextView) itemView.findViewById(R.id.textViewSenderMsgContent);
            receiverMsg= (TextView) itemView.findViewById(R.id.textViewReceiverMsgContent);
            senderImgMsg= (ImageView) itemView.findViewById(R.id.imageViewSenderImgMsg);
            receiverImgMsg= (ImageView) itemView.findViewById(R.id.imageViewReceiverImgMsg);
            senderSentTime= (TextView) itemView.findViewById(R.id.textViewSenderSentTime);
            receiverSentTime= (TextView) itemView.findViewById(R.id.textViewreceiverSentTime);

            senderMsgLayout= (LinearLayout) itemView.findViewById(R.id.senderMessageLayout);
            receiverMsgLayout= (LinearLayout) itemView.findViewById(R.id.receiverMessageLayout);

        }
    }

}
