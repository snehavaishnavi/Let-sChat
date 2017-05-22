package com.example.amrut.easychatapplication;

/**
 * Created by amrut on 11/22/2016.
 */

public class Message {
    String msgKey;
    String msgContent;
    String imgMsgUrl;
    String msgTimeStamp;
    String senderId;
    String receiverId;

    public String getMsgKey() {
        return msgKey;
    }

    public void setMsgKey(String msgKey) {
        this.msgKey = msgKey;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }

    public String getImgMsgUrl() {
        return imgMsgUrl;
    }

    public void setImgMsgUrl(String imgMsgUrl) {
        this.imgMsgUrl = imgMsgUrl;
    }

    public String getMsgTimeStamp() {
        return msgTimeStamp;
    }

    public void setMsgTimeStamp(String msgTimeStamp) {
        this.msgTimeStamp = msgTimeStamp;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    @Override
    public String toString() {
        return "Message{" +
                "msgKey='" + msgKey + '\'' +
                ", msgContent='" + msgContent + '\'' +
                ", imgMsgUrl='" + imgMsgUrl + '\'' +
                ", msgTimeStamp='" + msgTimeStamp + '\'' +
                ", senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                '}';
    }
}
