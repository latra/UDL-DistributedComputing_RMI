/* ---------------------------------------------------------------
Práctica 1.
Código fuente: MessengerManagerTest.java
Grau Informàtica
47129784J - Paula Gallucci Zurita
--------------------------------------------------------------- */
import exceptions.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;

public class MessageManagerTest {
    User user1;
    User user2;
    Group group1;
    Group group2;

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
    }

    @Test
    void createGroup() {
        MessageManagerServant messageManager = new MessageManagerServant();

        Assertions.assertTrue(messageManager.getCreatedGroups().isEmpty());
        try {
            messageManager.newGroup("Hogar");
            Assertions.assertEquals(1, messageManager.getCreatedGroups().size());
        } catch (Exception exception) {
            Assertions.fail();
        }
        Assertions.assertThrows(InvalidGroupException.class, () -> messageManager.newGroup("Hogar"));

    }

    @Test
    void newUserTest() {
        MessageManagerServant messageManager = new MessageManagerServant();

        Assertions.assertTrue(messageManager.getRegisteredUsers().isEmpty());
        try {
            messageManager.newUser("Manolito", "1234", "");
            Assertions.assertEquals(1, messageManager.getRegisteredUsers().size());
        } catch (Exception exception) {
            Assertions.fail();
        }
        Assertions.assertThrows(InvalidRegistryException.class, () -> messageManager.newUser("Manolito", "abc", ""));

        try {
            messageManager.newUser("Laura", "abc", "");
            messageManager.newGroup("Casa");
            messageManager.newUser("Paula", "abc123", "Casa");
        } catch (Exception exception) {
            Assertions.fail();
        }
    }

    @Test
    void loginTest() {
        MessageManagerServant messageManager = new MessageManagerServant();

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

    @Test
    void logoutTest() {
        MessageManagerServant messageManager = new MessageManagerServant();

        ClientDummy clientDummy1 = new ClientDummy();
        ClientDummy clientDummy2 = new ClientDummy();
        try {
            messageManager.newUser("latra", "123", "");
            Assertions.assertTrue(messageManager.getConnectedUsers().isEmpty());
            Assertions.assertThrows(LogoutException.class, () -> messageManager.logout(clientDummy1));
            messageManager.login("latra", "123", clientDummy2);
            Assertions.assertThrows(LogoutException.class, () -> messageManager.logout(clientDummy1));
            messageManager.logout(clientDummy2);
        } catch (Exception ex) {
            Assertions.fail();
        }
    }

    @Test
    void joinGroupTest() {
        MessageManagerServant messageManager = new MessageManagerServant();
        ClientDummy clientDummy = new ClientDummy();
        Assertions.assertThrows(GroupNotFoundException.class, () -> messageManager.newUser("Laura", "abc123", "Grupito"));
        try {
            messageManager.newGroup("Grupito");
            messageManager.newUser("Paula", "123", "Grupito");
            messageManager.login("Laura", "abc123", clientDummy);
            Assertions.assertThrows(GroupNotFoundException.class, () -> messageManager.joinGroup("TestG", "Laura", clientDummy));
            messageManager.joinGroup("Grupito", "Laura", clientDummy);
            Assertions.assertThrows(InvalidGroupException.class, () -> messageManager.joinGroup("Grupito", "", clientDummy));

        } catch (Exception ex) {
            System.out.println(ex);
            Assertions.fail();
        }
    }

    @Test
    void leaveGroupTest() {
        MessageManagerServant messageManager = new MessageManagerServant();
        ClientDummy clientDummy = new ClientDummy();
        try {
            messageManager.newGroup("Grupito");
            messageManager.newGroup("Perros");
            messageManager.newGroup("Gatos");
            messageManager.newUser("Paula", "123", "Grupito Perros");
            messageManager.login("Paula", "123", clientDummy);
            Assertions.assertThrows(InvalidGroupException.class, () -> messageManager.leaveGroup("Gatos", "Paula", clientDummy));
            Assertions.assertThrows(GroupNotFoundException.class, () -> messageManager.leaveGroup("Casa", "Paula", clientDummy));
            messageManager.leaveGroup("Grupito", "Paula", clientDummy);
            messageManager.leaveGroup("Perros", "Paula", clientDummy);
        } catch (Exception ex) {
            System.out.println(ex);
            Assertions.fail();
        }
    }
}
