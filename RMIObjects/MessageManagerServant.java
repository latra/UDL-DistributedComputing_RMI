import exceptions.*;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageManagerServant implements MessageManagerRMI {
    private HashMap<String, User> registeredUsers = new HashMap<>();
    private HashMap<String, Group> createdGroups = new HashMap<>();
    private List<Message> poolMessages = new ArrayList<>();

    @Override
    public void register(String user, String password, List<String> groups) throws RemoteException, InvalidRegistryException, GroupNotFoundException, InvalidGroupException {
        System.out.println(String.format("DEBUG: Solicitud de registro entrante: Usuario %s, Contraseña %s", user, password));
        if (registeredUsers.containsKey(user)) {
            System.out.println(String.format("DEBUG: El usuario %s ya existía en el servidor. No se va a crear.", user));

            throw new InvalidRegistryException();
        } else {
            System.out.println(String.format("DEBUG: Creando al usuario %s.", user));

            registeredUsers.put(user, new User(user, password, new ArrayList<>()));
            for (String group : groups) {
                System.out.println(String.format("DEBUG: Añadiendo al usuario %s al grupo %s.", user, group));

                joinGroup(user, group);
            }
        }
    }

    @Override
    public void login(String user, String password, MessageListener messageListener) throws RemoteException, InvalidLoginException {
        System.out.println(String.format("DEBUG: Solicitud de conexión entrante: Usuario %s, contraseña %s", user, password));

        if (registeredUsers.containsKey(user) && registeredUsers.get(user).checkPassword(password)) {
            System.out.println(String.format("DEBUG: Conectando al usuario %s y avisando a los otros usuarios", user));

            registeredUsers.get(user).setStatus(User.Status.CONNECTED);
            registeredUsers.get(user).setMessageListener(messageListener);
            poolMessages.add(new Message(Message.MessageType.USER_LOGGED, String.format("El usuario %s se ha conectado",
                    user), null, null));
            this.messageSender();

        } else {
            System.out.println(String.format("DEBUG: La contraseña introducida por el usuario %s no es correcta", user));

            // Error contraseña incorrecta

            throw new InvalidLoginException();

        }
    }

    @Override
    public void logout(String user) throws RemoteException, LogoutException {
        if (registeredUsers.containsKey(user) && registeredUsers.get(user).getStatus() == User.Status.CONNECTED) {
            registeredUsers.get(user).setStatus(User.Status.DISCONNECTED);
            poolMessages.add(new Message(Message.MessageType.USER_DISCONNECTED, String.format("El usuario %s se ha desconectado", user),
                    null, null));
            this.messageSender();

        } else {
            // No se está conectado
            throw new LogoutException();
        }
    }

    @Override
    public void newGroup(String groupName) throws RemoteException, InvalidGroupException {
        if (createdGroups.containsKey(groupName)) {
            throw new InvalidGroupException();
        } else {
            createdGroups.put(groupName, new Group());

            poolMessages.add(new Message(Message.MessageType.GROUP_CREATED, String.format("Se ha creado el grupo %s", groupName),
                    null, null));
            this.messageSender();

        }
    }

    @Override
    public void joinGroup(String user, String groupName) throws RemoteException, GroupNotFoundException, InvalidGroupException {
        if (createdGroups.containsKey(groupName) && registeredUsers.containsKey(user)) {
            if (registeredUsers.get(user).isNotPartOf(groupName))
                throw new InvalidGroupException();
            createdGroups.get(groupName).addUser(registeredUsers.get(user));
            registeredUsers.get(user).addToGroup(groupName);
            poolMessages.add(new Message(Message.MessageType.GROUP_JOINED, String.format("El usuario %s se ha unido al grupo %s",
                    user, groupName), null, groupName));
            this.messageSender();

        } else {
            throw new GroupNotFoundException();
        }
    }

    @Override
    public void sendMessage(String message, String userSender, String userReceiver, String groupReceiver) throws RemoteException, UserDisconnectedException, UserNotFoundException, InvalidGroupException {
        System.out.println(String.format("Solicitud de mensaje de %s hacia el usuario %s y el grupo %s diciendo %s", userSender, userReceiver, groupReceiver, message));
        if (userReceiver != null && !userReceiver.isEmpty()) {
            if (!this.registeredUsers.containsKey(userReceiver))
                throw new UserNotFoundException();
            else if (this.registeredUsers.get(userReceiver).getStatus() == User.Status.DISCONNECTED)
                throw new UserDisconnectedException();
        }
        if (groupReceiver != null && !groupReceiver.isEmpty()) {
            if (!this.createdGroups.containsKey(groupReceiver) || this.registeredUsers.get(userSender).isNotPartOf(groupReceiver))
                throw new InvalidGroupException();
        }

        poolMessages.add(new Message(Message.MessageType.MESSAGE, String.format("%s dice: %s", userSender, message),
                userReceiver, groupReceiver));
        this.messageSender();


    }

    @Override
    public void messageSender() throws RemoteException {

        while (!this.poolMessages.isEmpty()) {
            Message actualMessage = this.poolMessages.remove(0);
            System.out.println("Enviando el mensaje: " + actualMessage.getMessage());
            switch (actualMessage.getMessageType()) {

                case Message.MessageType.USER_LOGGED:
                case Message.MessageType.GROUP_CREATED:
                case Message.MessageType.USER_DISCONNECTED:
                    informToAllListeners(actualMessage.getMessage());
                    break;
                case Message.MessageType.GROUP_JOINED:
                    informToGroup(actualMessage.getMessage(), actualMessage.getGroupReceiver());
                    break;

                case Message.MessageType.MESSAGE:
                    if (actualMessage.getGroupReceiver() != null && !actualMessage.getGroupReceiver().isEmpty()) {
                        informToGroup(actualMessage.getMessage(), actualMessage.getGroupReceiver());
                    }
                    if (actualMessage.getUserReceiver() != null && !actualMessage.getUserReceiver().isEmpty()) {
                        informToUser(actualMessage.getMessage(), actualMessage.getUserReceiver());
                    }
                    break;
            }

        }

    }

    private void informToUser(String message, String userName) {
        try {
            this.registeredUsers.get(userName).getMessageListener().messageReceived(message);
        } catch (RemoteException exception) {
            registeredUsers.get(userName).setStatus(User.Status.DISCONNECTED);
        }
    }

    private void informToGroup(String message, String groupName) {
        for (User user : this.createdGroups.get(groupName).getUsers()) {
            if (user.getStatus() == User.Status.CONNECTED) {
                this.informToUser(message, user.getUserName());
            }
        }
    }

    private void informToAllListeners(String message) throws RemoteException {
        for (User user : this.registeredUsers.values()) {
            if (user.getStatus() == User.Status.CONNECTED) {
                user.getMessageListener().messageReceived(message);
            }
        }
    }

}
