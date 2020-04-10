
class Message {


    static class MessageType {
        static final int USER_LOGGED = 0;
        static final int USER_DISCONNECTED = 1;
        static final int MESSAGE = 4;
        static final int GROUP_CREATED = 5;
        static final int GROUP_JOINED = 6;

    }
    private int messageType;
    private String message;
    private String userReceiver;
    private String groupReceiver;

    Message(int messageType, String message, String userReceiver, String groupReceiver){
        this.messageType = messageType;
        this.message = message;
        this.userReceiver = userReceiver;
        this.groupReceiver = groupReceiver;
    }
    int getMessageType() {
        return messageType;
    }

    String getMessage() {
        return message;
    }

    String getUserReceiver() {
        return userReceiver;
    }

    String getGroupReceiver() {
        return groupReceiver;
    }
}
