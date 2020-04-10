
import exceptions.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface MessageManagerRMI extends Remote {
    void register(String user, String password, List<String> groups) throws RemoteException, InvalidRegistryException, GroupNotFoundException, InvalidGroupException;
    void login(String user, String password, MessageListener messageListener) throws RemoteException, InvalidLoginException;
    void logout(String user) throws RemoteException, LogoutException;
    void newGroup(String groupName) throws RemoteException, InvalidGroupException;
    void joinGroup(String user, String groupName) throws RemoteException, GroupNotFoundException, InvalidGroupException;
    void sendMessage(String message, String userSender, String userReceiver, String groupReceiver) throws RemoteException, UserDisconnectedException, UserNotFoundException, InvalidGroupException;

    void messageSender() throws RemoteException;
}
