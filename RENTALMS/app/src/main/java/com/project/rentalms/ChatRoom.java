package com.project.rentalms;

public class ChatRoom {
    private String chatRoomId;
    private String messageText;
    private String receiverId;
    private String senderId;
    private Long timestamp;  // Change the timestamp type to Long

    // Default constructor
    public ChatRoom() {}

    // Constructor with all fields
    public ChatRoom(String chatRoomId, String messageText, String receiverId, String senderId, Long timestamp) {
        this.chatRoomId = chatRoomId;
        this.messageText = messageText;
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
