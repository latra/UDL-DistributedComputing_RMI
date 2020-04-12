import exceptions.*;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageManagerServant implements ServerRMI {
    private HashMap<String, User> registeredUsers = new HashMap<>();
    private HashMap<ClientRMI, String> connectedUsers = new HashMap<>();
    private HashMap<String, Group> createdGroups = new HashMap<>();
    private List<Message> poolMessages = new ArrayList<>();

    HashMap<String, User> getRegisteredUsers(){
        return this.registeredUsers;
    }
    HashMap<String, Group> getCreatedGroups() {
        return this.createdGroups;
    }
    HashMap<ClientRMI, String> getConnectedUsers() {
        return this.connectedUsers;
    }


    @Override
    public void newUser(String user, String password, String groups) throws RemoteException, InvalidRegistryException, GroupNotFoundException, InvalidGroupException {
        System.out.println(String.format("DEBUG: Solicitud de registro entrante: Usuario %s, Contraseña %s", user, password));
        // Confirma que el nombre no esté en uso
        if (registeredUsers.containsKey(user)) {
            System.out.println(String.format("DEBUG: El usuario %s ya existía en el servidor. No se va a crear.", user));

            throw new InvalidRegistryException();
        } else {
            System.out.println(String.format("DEBUG: Creando al usuario %s.", user));
            // Añade al usuario a la lista de usuarios
            registeredUsers.put(user, new User(user, password));
            if (!groups.isEmpty())
                // Añade al usuario a los grupos especificados
                for (String group : groups.split(" ")) {
                    System.out.println(String.format("DEBUG: Añadiendo al usuario %s al grupo %s.", user, group));
                    joinGroup(group, user, null);
                }
        }
    }

    @Override
    public void login(String user, String password, ClientRMI listener) throws RemoteException, InvalidLoginException {
        System.out.println(String.format("DEBUG: Solicitud de conexión entrante: Usuario %s, contraseña %s", user, password));
        // Confirma que el usuario exista y que la contraseña sea correcta
        if (registeredUsers.containsKey(user) && registeredUsers.get(user).checkPassword(password)) {
            if (registeredUsers.get(user).getStatus() == User.Status.CONNECTED) {
                // Si ya estaba conectado, informa al cliente que estaba conectado que está siendo desconectado.
                try {
                    registeredUsers.get(user).getMessageListener().notifyDisconnection("Te has conectado desde otro dispositivo... Desconectando.");
                    this.logout(registeredUsers.get(user).getMessageListener());
                } catch (LogoutException exception) {
                    System.out.println("DEBUG: Error al desconectar. Ignorando.");
                }
            }
            System.out.println(String.format("DEBUG: Conectando al usuario %s y avisando a los otros usuarios", user));
            // Se conecta al usuario.

            registeredUsers.get(user).setStatus(User.Status.CONNECTED);
            registeredUsers.get(user).setMessageListener(listener);

            // Se añade al usuario a la tabla auxiliar de Usuarios conectados.
            connectedUsers.put(listener, user);
            poolMessages.add(new Message(Message.MessageType.USER_LOGGED, user, null, null, null));
            this.messageSender();

        } else {
            System.out.println(String.format("DEBUG: La contraseña introducida por el usuario %s no es correcta", user));

            // Error contraseña incorrecta

            throw new InvalidLoginException();

        }
    }

    @Override
    public void logout(ClientRMI listener) throws RemoteException, LogoutException {
        // Confirma que el usuario está conectado
        if (connectedUsers.containsKey(listener)) {
            // Se recupera el nombre del usuario y se le desconecta.
            String user = connectedUsers.get(listener);
            registeredUsers.get(user).setStatus(User.Status.DISCONNECTED);
            connectedUsers.remove(listener);
            // Se informa a los otros usuarios
            poolMessages.add(new Message(Message.MessageType.USER_DISCONNECTED, user, null,
                    null, null));
            this.messageSender();

        } else {
            // No se está conectado
            throw new LogoutException();
        }
    }

    @Override
    public void newGroup(String group) throws RemoteException, InvalidGroupException {
        // Se revisa que el nombre de grupo no esté en uso
        if (createdGroups.containsKey(group)) {
            throw new InvalidGroupException();
        } else {
            // Se añade el grupo a la lista de grupos
            createdGroups.put(group, new Group(group));

            poolMessages.add(new Message(Message.MessageType.GROUP_CREATED, null, null,
                    null, group));
            this.messageSender();
        }
    }

    @Override
    public void joinGroup(String groupName, String userName, ClientRMI listener) throws RemoteException, GroupNotFoundException, InvalidGroupException {
        // (Compatibilidad) Si no se ha especificado el nombre de usuario pero si el listener, se recupera el nombre del usuario de la lista de conectados
        if ((userName == null || userName.isEmpty()) && connectedUsers.containsKey(listener))
            userName = connectedUsers.get(listener);
        // Se confirma que el grupo y el usuario existe y se le añade al grupo
        if (createdGroups.containsKey(groupName) && registeredUsers.containsKey(userName)) {
            // Se revisa que el usuario no perteneza ya a ese mismo grupo
            if (createdGroups.get(groupName).containsUser(registeredUsers.get(userName)))
                throw new InvalidGroupException();
            createdGroups.get(groupName).addUser(registeredUsers.get(userName));
            poolMessages.add(new Message(Message.MessageType.GROUP_JOINED, userName, null, null, groupName));
            this.messageSender();

        } else {
            System.out.println("DEBUG: No se ha encontrado el equipo " + groupName + " o al usuario " + userName);
            throw new GroupNotFoundException();
        }
    }

    @Override
    public void leaveGroup(String groupName, String userName, ClientRMI listener) throws RemoteException, GroupNotFoundException, InvalidGroupException {
        // (Compatibilidad) Si no se ha especificado el nombre de usuario pero si el listener, se recupera el nombre del usuario de la lista de conectados
        if ((userName == null || userName.isEmpty()) && connectedUsers.containsKey(listener))
            userName = connectedUsers.get(listener);
        if (createdGroups.containsKey(groupName) && registeredUsers.containsKey(userName)) {
            if (!createdGroups.get(groupName).containsUser(registeredUsers.get(userName)))
                throw new InvalidGroupException();
            createdGroups.get(groupName).removeUser(registeredUsers.get(userName));
            poolMessages.add(new Message(Message.MessageType.GROUP_LEAVED, userName, null, null, groupName));
            this.messageSender();

        } else {
            throw new GroupNotFoundException();
        }
    }

    @Override
    public void sendMsgUser(String userSrc, String userDst, String message, String date) throws RemoteException, UserNotFoundException, UserDisconnectedException {
        // Se añade un mensaje a la lista de mensajes
        if (userDst != null && !userDst.isEmpty()) {
            if (!this.registeredUsers.containsKey(userDst))
                throw new UserNotFoundException();
            else if (this.registeredUsers.get(userDst).getStatus() == User.Status.DISCONNECTED)
                throw new UserDisconnectedException();
        }
        poolMessages.add(new Message(Message.MessageType.USER_MESSAGE, userSrc, message,
                userDst, null, date));
        this.messageSender();
    }

    @Override
    public void sendMsgGroup(String userSrc, String group, String message, String date) throws RemoteException, InvalidGroupException {
        // Se añade un mensaje a la lista de mensajes

        if (group != null && !group.isEmpty()) {
            if (!this.createdGroups.containsKey(group) || !this.createdGroups.get(group).containsUser(registeredUsers.get(userSrc)))
                throw new InvalidGroupException();
        }
        poolMessages.add(new Message(Message.MessageType.GROUP_MESSAGE, userSrc, message,
                null, group, date));
        messageSender();

    }

    private void messageSender() throws RemoteException {
        // Se encarga de vacíar la lista de mensajes pendientes de envío.
        while (!this.poolMessages.isEmpty()) {
            Message actualMessage = this.poolMessages.remove(0);
            System.out.println("DEBUG: Enviando mensaje de tipo " + actualMessage.getMessageType());

            switch (actualMessage.getMessageType()) {

                case Message.MessageType.USER_LOGGED:

                    notifyUserLogged(actualMessage);
                    break;
                case Message.MessageType.GROUP_CREATED:
                    notifyGroupCreated(actualMessage);
                    break;
                case Message.MessageType.USER_DISCONNECTED:
                    notifyUserDisconnected(actualMessage);
                    break;
                case Message.MessageType.GROUP_JOINED:
                    notifyGroupJoined(actualMessage);
                    break;
                case Message.MessageType.GROUP_MESSAGE:
                    notifyGroupMessage(actualMessage);
                    break;
                case Message.MessageType.USER_MESSAGE:
                    notifyUserMessage(actualMessage);
                    break;
                case Message.MessageType.GROUP_LEAVED:
                    notifyUserLeaved(actualMessage);
            }

        }

    }

    private void notifyGroupCreated(Message actualMessage) throws RemoteException {
        for (ClientRMI connectedUser : connectedUsers.keySet()) {
            connectedUser.notifyNewGroup(actualMessage.getGroupReceiver());
        }
    }

    private void notifyUserLogged(Message actualMessage) throws RemoteException {
        for (ClientRMI connectedUser : connectedUsers.keySet()) {
            connectedUser.notifyNewUser(actualMessage.getUserSrc());
        }
    }

    private void notifyUserDisconnected(Message actualMessage) throws RemoteException {
        for (ClientRMI connectedUser : connectedUsers.keySet()) {
            connectedUser.notifyUserLogout(actualMessage.getUserSrc());
        }
    }

    private void notifyGroupJoined(Message actualMessage) throws RemoteException {
        for (User user : createdGroups.get(actualMessage.getGroupReceiver()).getUsers()) {
            if (user.getStatus() == User.Status.CONNECTED)
                user.getMessageListener().notifyUserJoined(actualMessage.getUserSrc(), actualMessage.getGroupReceiver());
        }
    }

    private void notifyUserLeaved(Message actualMessage) throws RemoteException {
        for (User user : createdGroups.get(actualMessage.getGroupReceiver()).getUsers()) {
            user.getMessageListener().notifyUserLeave(actualMessage.getUserSrc(), actualMessage.getGroupReceiver());
        }
    }

    private void notifyGroupMessage(Message actualMessage) throws RemoteException {
        for (User user : createdGroups.get(actualMessage.getGroupReceiver()).getUsers()) {
            if (user.getStatus() == User.Status.CONNECTED)
                user.getMessageListener().sendMsgGroup(actualMessage.getUserSrc(), actualMessage.getGroupReceiver(),
                        actualMessage.getMessage(), actualMessage.getDate());
        }
    }

    private void notifyUserMessage(Message actualMessage) throws RemoteException {
        registeredUsers.get(actualMessage.getUserReceiver()).getMessageListener().
                sendMsgUser(actualMessage.getUserSrc(), actualMessage.getMessage(), actualMessage.getDate());
    }


}
