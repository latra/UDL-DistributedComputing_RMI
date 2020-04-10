

import java.util.List;

class User {
    String getUserName() {
        return userName;
    }

    static class Status {

        static int DISCONNECTED = 0;
        static int CONNECTED = 1;
    }
    private List<String> groups;
    private String userName;
    private String password;
    private int status = Status.DISCONNECTED;
    private MessageListener messageListener;

    User(String userName, String password, List<String> groups) {
        this.groups = groups;
        this.userName = userName;
        this.password = password;
    }
    void setStatus(int status) {
        this.status = status;
    }
    boolean checkPassword(String sendedPassword) {
        return this.password.equals(sendedPassword);
    }
    int getStatus() {return this.status;}
    boolean isNotPartOf(String groupName) {
        return !this.groups.contains(groupName);
    }
    void addToGroup(String groupName) {
        this.groups.add(groupName);
    }
    MessageListener getMessageListener(){
        return this.messageListener;
    }
    void setMessageListener(MessageListener messageListener){
        this.messageListener = messageListener;
    }
}
