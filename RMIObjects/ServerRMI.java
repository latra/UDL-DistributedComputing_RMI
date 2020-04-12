/* ---------------------------------------------------------------
Práctica 1.
Código fuente: ServerRMI.java
Grau Informàtica
47129784J - Paula Gallucci Zurita
--------------------------------------------------------------- */
import exceptions.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ServerRMI extends Remote {
    void newUser(String user, String password, String groupString) throws RemoteException, InvalidRegistryException, GroupNotFoundException, InvalidGroupException;

    void login(String user, String password, ClientRMI listener) throws RemoteException, InvalidLoginException;

    void logout(ClientRMI listener) throws RemoteException, LogoutException;

    void newGroup(String group) throws RemoteException, InvalidGroupException;

    void joinGroup(String user, String groupName, ClientRMI listener) throws RemoteException, GroupNotFoundException, InvalidGroupException;

    void leaveGroup(String groupName, String userName, ClientRMI listener) throws RemoteException, GroupNotFoundException, InvalidGroupException;

    void sendMsgUser(String userSrc, String userDst, String message, String date) throws RemoteException, UserNotFoundException, UserDisconnectedException;

    void sendMsgGroup(String userSrc, String group, String message, String date) throws RemoteException, InvalidGroupException;
}
