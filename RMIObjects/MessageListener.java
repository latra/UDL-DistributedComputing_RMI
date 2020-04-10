import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MessageListener extends Remote {
    void messageReceived(String message) throws RemoteException;
}
