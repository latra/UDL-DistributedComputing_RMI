

import java.util.List;

class User {

    static class Status {

        static int DISCONNECTED = 0;
        static int CONNECTED = 1;
    }

    private String password;
    private int status = Status.DISCONNECTED;
    private ClientRMI messageListener;

    User(String userName, String password) {
        this.password = password;
    }

    void setStatus(int status) {
        this.status = status;
    }

    boolean checkPassword(String sendedPassword) {
        return this.password.equals(sendedPassword);
    }

    int getStatus() {
        return this.status;
    }

    ClientRMI getMessageListener() {
        return this.messageListener;
    }

    void setMessageListener(ClientRMI messageListener) {
        this.messageListener = messageListener;
    }

}
