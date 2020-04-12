/* ---------------------------------------------------------------
Práctica 1.
Código fuente: Message.java
Grau Informàtica
47129784J - Paula Gallucci Zurita
--------------------------------------------------------------- */
class Message {


    public String getUserSrc() {
        return userSrc;
    }

    public String getDate() {
        return date;
    }

    static class MessageType {
        static final int USER_LOGGED = 0;
        static final int USER_DISCONNECTED = 1;
        static final int USER_MESSAGE = 3;
        static final int GROUP_MESSAGE = 4;
        static final int GROUP_CREATED = 5;
        static final int GROUP_JOINED = 6;
        static final int GROUP_LEAVED = 7;

    }

    private int messageType;
    private String message;
    private String userReceiver;
    private String groupReceiver;
    private String userSrc;
    private String date;

    Message(int messageType, String usrSrc, String message, String userReceiver, String groupReceiver, String date) {
        this.messageType = messageType;
        this.userSrc = usrSrc;
        this.message = message;
        this.userReceiver = userReceiver;
        this.groupReceiver = groupReceiver;
        this.date = date;
    }

    Message(int messageType, String usrSrc, String message, String userReceiver, String groupReceiver) {
        this.messageType = messageType;
        this.userSrc = usrSrc;
        this.message = message;
        this.userReceiver = userReceiver;
        this.groupReceiver = groupReceiver;
        this.date = "";
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
