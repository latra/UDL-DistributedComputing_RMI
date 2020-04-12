import exceptions.GroupNotFoundException;
import exceptions.InvalidGroupException;
import exceptions.InvalidLoginException;
import exceptions.InvalidRegistryException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;

public class MessageManagerTest {
    User user1;
    User user2;
    Group group1;
    Group group2;
    MessageManagerServant messageManager;

    class ClientDummy implements ClientRMI {

        @Override
        public void sendMsgUser(String userSrc, String message, String date) throws RemoteException {

        }

        @Override
        public void sendMsgGroup(String userSrc, String group, String message, String date) throws RemoteException {

        }

        @Override
        public void notifyNewUser(String username) throws RemoteException {

        }

        @Override
        public void notifyUserLogout(String username) throws RemoteException {

        }

        @Override
        public void notifyNewGroup(String groupName) throws RemoteException {

        }

        @Override
        public void notifyUserJoined(String username, String groupName) throws RemoteException {

        }

        @Override
        public void notifyUserLeave(String username, String groupName) throws RemoteException {

        }

        @Override
        public void notifyDisconnection(String message) throws RemoteException {

        }
    }
    @BeforeEach
    void constructor() {
        messageManager = new MessageManagerServant();
    }

    @Test
    void createGroup() {
        Assertions.assertTrue(messageManager.getCreatedGroups().isEmpty());
        try {
            messageManager.newGroup("Hogar");
            Assertions.assertEquals(1, messageManager.getCreatedGroups().size());
        } catch (Exception exception){
            Assertions.fail();
        }
        Assertions.assertThrows(InvalidGroupException.class, () -> messageManager.newGroup("Hogar"));

    }
    @Test
    void newUserTest() {
        Assertions.assertTrue(messageManager.getRegisteredUsers().isEmpty());
        try {
            messageManager.newUser("Manolito", "1234", "");
            Assertions.assertEquals(1, messageManager.getRegisteredUsers().size());
        } catch (Exception exception) {
            Assertions.fail();
        }
        Assertions.assertThrows(InvalidRegistryException.class, ()-> messageManager.newUser("Manolito", "abc", ""));
        Assertions.assertThrows(GroupNotFoundException.class, () -> messageManager.newUser("Laura", "abc123", "Grupito"));
        Assertions.assertThrows(InvalidRegistryException.class, ()-> messageManager.newUser("Laura", "abc", ""));

        try {
            messageManager.newGroup("Casa");
            messageManager.newUser("Paula", "abc123", "Casa");
        } catch (Exception exception) {
            Assertions.fail();
        }
    }
    @Test
    void loginTest() {
        ClientDummy clientDummy = new ClientDummy();
        Assertions.assertTrue(messageManager.getConnectedUsers().isEmpty());
        try {
            messageManager.newUser("persona", "123", "");
            Assertions.assertThrows(InvalidLoginException.class, () -> messageManager.login("persona", "12345", clientDummy));
            messageManager.login("persona", "123", clientDummy);
        } catch (Exception except) {
            Assertions.fail();
        }
    }
}
