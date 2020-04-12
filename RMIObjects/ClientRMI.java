/* ---------------------------------------------------------------
Práctica 1.
Código fuente: ClientRMI.java
Grau Informàtica
47129784J - Paula Gallucci Zurita
--------------------------------------------------------------- */
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * ClientRMI: ClientListener. Define las funciones a las cuales enviar los mensajes.
 */
public interface ClientRMI extends Remote {
    void sendMsgUser(String userSrc, String message, String date) throws RemoteException;

    void sendMsgGroup(String userSrc, String group, String message, String date) throws RemoteException;

    void notifyNewUser(String username) throws RemoteException;

    void notifyUserLogout(String username) throws RemoteException;

    void notifyNewGroup(String groupName) throws RemoteException;

    void notifyUserJoined(String username, String groupName) throws RemoteException;

    void notifyUserLeave(String username, String groupName) throws RemoteException;

    void notifyDisconnection(String message) throws RemoteException;
}
