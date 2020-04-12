import exceptions.*;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.System.exit;

public class MessengerClient extends UnicastRemoteObject implements ClientRMI {
    private MessengerClient() throws RemoteException {

    }

    private static String userLoged = "";

    /**
     * Función principal de ejecución del cliente. Se encarga de la conexión con el servidor y la lectura de los comandos,
     * así como de realizar las solicitudes al objeto remoto.
     *
     * @param args Pueden recibirse parámetros adicionales. Args[0] corresponderá a la IP del servidor al que se debe conectar.
     */
    public static void main(String[] args) {

        try {
            System.setSecurityManager(new SecurityManager());

            System.out.println("MSG: Buscando servicios");
            String registry = "localhost";
            if (args.length >= 1)
                registry = args[0];
            registry = "rmi://" + registry + "/Messenger";
            Remote servicioRemoto = Naming.lookup(registry);
            ServerRMI messageManager = (ServerRMI) servicioRemoto;

            Scanner input = new Scanner(System.in);
            MessengerClient messageListener = new MessengerClient();

            System.out.println("MSG: \u00a1Se ha establecido conexi\u00f3n con el servidor!\n\n\n " +
                    "INFO: Utiliza Login <Usuario> [<Contraseña>] para conectar o NewUser <Usuario> <Contraseña> [Grupo] para registrarte.");
            //Inicio del proceso de lectura de comandos
            while (true) {
                System.out.println(" - - - - - Introduce el comando. - - - - -");
                String[] command = input.nextLine().split(" ");
                switch (command[0].toLowerCase()) {
                    case "login":
                        MessengerClient.userLoged = loginManager(MessengerClient.userLoged, messageManager, messageListener, command);
                        break;
                    case "newuser":
                        registerManager(MessengerClient.userLoged, messageManager, command);
                        break;
                    case "logout":
                        logoutManager(messageListener, messageManager);
                        MessengerClient.userLoged = "";
                        break;
                    case "sendmsg":
                        sendMessageManager(MessengerClient.userLoged, messageManager, command);
                        break;
                    case "newgroup":
                        newGroupManager(messageManager, command);
                        break;
                    case "joingroup":
                        joinGroupManager(MessengerClient.userLoged, messageListener, messageManager, command);
                        break;
                    case "leavegroup":
                        leaveGroupManager(MessengerClient.userLoged, messageListener, messageManager, command);
                    case "exit":
                        if (!MessengerClient.userLoged.isEmpty())
                            logoutManager(messageListener, messageManager);
                        exit(0);
                    default:
                        System.out.println("El comando introducido es incorrecto");
                }
            }
        } catch (SecurityException exception) {
            System.out.println("Permisos insuficientes");
            System.out.println(exception.getMessage());
        } catch (NotBoundException notBoundEx) {
            System.err.println("ERROR: No existe el servicio en el registro");
        } catch (RemoteException remEx) {
            System.err.println("ERROR: Error remoto - " + remEx);
        } catch (Exception e) {
            System.err.println("ERROR: Error - " + e);
            e.printStackTrace();
        } finally {
            // Si sucede algún error, se fuerza la salida del programa.
            exit(-1);
        }
    }

    /**
     * Función encargada de realizar las llamadas al método de conexión
     *
     * @param userLogged      Nombre del usuario
     * @param messageManager  Objeto RMI del servidor
     * @param messageListener Objeto ClientRMI generado para este cliente.
     * @param command         Todos los campos del comando (si se ha introducido correctamente, command [username][password]
     * @return Devuelve el nombre del usuario conectado si la conexión se ha realizado satisfactoriamente.
     */
    private static String loginManager(String userLogged, ServerRMI messageManager, ClientRMI messageListener, String[] command) throws RemoteException {

        if (userLogged.isEmpty())
            if (command.length == 3) {
                try {
                    messageManager.login(command[1], command[2], messageListener);
                    return command[1];
                } catch (InvalidLoginException exception) {
                    System.out.println("ERROR: El usuario y la contraseña introducida no coinciden");
                }
            } else
                System.out.println("ERROR: Sintaxis erronea: Login <Usuario> <Contraseña>");
        else
            System.out.println("ERROR: Desconectate antes de conectarte con otra cuenta. Puedes usar el comando Logout.");
        return "";
    }

    /**
     * Se encarga de solicitar el registro de un usuario al servicio.
     * Puede recibir el error "InvalidRegistryException" si el nombre ya está en uso o
     * "GroupNotFoundException" si el grupo no existe.
     *
     * @param userLogged     Nombre del usuario a registrar
     * @param messageManager Objeto RMI al cual se llamarán los métodos
     * @param command        Todos los campos del comando
     */
    private static void registerManager(String userLogged, ServerRMI messageManager, String[] command) throws RemoteException {

        if (userLogged.isEmpty())
            if (command.length > 2) {
                String[] groups = new String[]{};
                try {
                    if (command.length > 3)
                        groups = Arrays.copyOfRange(command, 3, command.length);
                    StringBuilder sb = new StringBuilder();
                    for (String group : groups) {
                        sb.append(group).append(" ");
                    }
                    messageManager.newUser(command[1], command[2], sb.toString());
                    System.out.println("INFO: Se ha completado el registro. Ahora ya puedes conectarte.");
                } catch (InvalidRegistryException exception) {
                    System.out.println("ERROR: No se ha podido registrar. Posiblemente se deba a que el usuario ya existe");
                } catch (GroupNotFoundException | InvalidGroupException exception) {
                    System.out.println("ERROR: Alguno de los grupos indicados no existen. Se ha creado el usuario, pero no se ha unido a los grupos.");
                }

            } else {
                System.out.println("ERROR: Sintaxis erronea: NewUser <Usuario> <Contraseña> [grupos]");
            }
        else
            System.out.println("ERROR: Desconectate antes de crear otra cuenta. Puedes usar el comando Logout");
    }

    /**
     * Se encarga de solicitar la desconexión en el servidor del usuario. Si  ya está considerado
     * como desconectado, recibirá en error "LogoutException"
     *
     * @param messageListener Objeto ClientRMI generado para este cliente.
     * @param messageManager  Objeto RMI al cual se llamarán los métodos
     */
    private static void logoutManager(ClientRMI messageListener, ServerRMI messageManager) throws RemoteException {

        try {

            messageManager.logout(messageListener);
            System.out.println("INFO: Te has desconectado correctamente");

        } catch (LogoutException exception) {
            System.out.println("INFO: No estabas conectado.");
        }

    }

    /**
     * Solicita al servicio el envio de un mensaje a un usuario o grupo concreto (o a ambos simultáeamente).
     * Si el usuario receptor no existe, se recibirá el error UserNotFoundException
     * Si el usuario receptor está desconectado, se recibirá el error UserDisconnectedException.
     * Si el grupo receptor no existe o el usuario no forma parte de él, se recibirá el error InvalidGroupException
     *
     * @param userLogged     Nombre del usuario conectado (emisor)
     * @param messageManager Objeto RMI al cual se llamarán los métodos
     * @param command        Todos los campos del comando
     */
    private static void sendMessageManager(String userLogged, ServerRMI messageManager, String[] command) throws RemoteException {
        /*

         */
        if (!userLogged.isEmpty()) {
            if (command.length > 2) {
                Date date = Calendar.getInstance().getTime();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
                String strDate = dateFormat.format(date);
                List<String> commandAsList = Arrays.asList(command);

                if (commandAsList.contains("-g")) {
                    // Es un mensaje dirigido a un grupo
                    sendToGroup(userLogged, messageManager, command, strDate);
                } else {
                    // Mensaje dirigido a un usuario concreto
                    sendToUser(userLogged, messageManager, command, strDate);
                }

            } else {
                System.out.println("ERROR: Sintaxis errónea: SendMsg <Usuario> [-g grupo] <TextoMensaje>");
            }
        } else {
            System.out.println("ERROR: Debes conectarte antes de enviar un mensaje.");
        }
    }

    private static void sendToUser(String userLogged, ServerRMI messageManager, String[] command, String strDate) throws RemoteException {
        String[] messageArray = Arrays.copyOfRange(command, 2, command.length);
        StringBuilder message = new StringBuilder();
        for (String string : messageArray) {
            message.append(string).append(" ");
        }
        try {
            messageManager.sendMsgUser(userLogged, command[1], message.toString(), strDate);
        } catch (UserNotFoundException e) {
            System.out.println("El usuario no existe.");
        } catch (UserDisconnectedException e) {
            System.out.println("El usuario no está conectado");
        }
    }

    private static void sendToGroup(String userLogged, ServerRMI messageManager, String[] command, String date) throws RemoteException {
        String[] messageArray = Arrays.copyOfRange(command, 3, command.length);
        StringBuilder message = new StringBuilder();
        for (String string : messageArray) {
            message.append(string).append(" ");
        }
        try {
            messageManager.sendMsgGroup(userLogged, command[2], message.toString(), date);
        } catch (InvalidGroupException e) {
            System.out.println("No formas parte de este grupo o no existe.");
        }

    }

    private static void newGroupManager(ServerRMI messageManager, String[] command) throws RemoteException {
        /*
          Solicita al servicio la creación de un nuevo grupo.
          Si el nombre especificado coincide con otro grupo, se recibirá el error InvalidGroupException.
         */
        if (command.length == 2) {
            try {
                messageManager.newGroup(command[1]);
            } catch (InvalidGroupException exception) {
                System.out.println("ERROR: El grupo que ha indicado ya existe.");
            }
        } else {
            System.out.println("ERROR: Sintaxis errónea: NewGroup <NombreDelGrupo>");
        }
    }

    private static void joinGroupManager(String userLogged, ClientRMI listener, ServerRMI messageManager, String[] command) throws RemoteException {
        /*
          Solicita al servicio la unión en un grupo especificado.
          Si el grupo no existe se recibirá el error "GroupNotFoundEsception".
          Si el usuario ya formaba parte del grupo, se recibirá el error "InvalidGroupException".
         */
        if (!userLogged.isEmpty()) {
            if (command.length == 2) {
                try {
                    messageManager.joinGroup(command[1], userLogged, listener);
                } catch (GroupNotFoundException exception) {
                    System.out.println("ERROR: El grupo introducido no existe");
                } catch (InvalidGroupException exception) {
                    System.out.println("ERROR: Ya formas parte de este grupo.");
                }
            } else {

                System.out.println("ERROR: Sintaxis erronea: JoinGroup <NombreDelGrupo>");
            }
        } else {
            System.out.println("ERROR: Debes conectarte antes de unirte a un grupo.");
        }
    }

    private static void leaveGroupManager(String userLogged, ClientRMI listener, ServerRMI messageManager, String[] command) throws RemoteException {
        /*
          Solicita al servicio la unión en un grupo especificado.
          Si el grupo no existe se recibirá el error "GroupNotFoundEsception".
          Si el usuario ya formaba parte del grupo, se recibirá el error "InvalidGroupException".
         */
        if (!userLogged.isEmpty()) {
            if (command.length == 2) {
                try {
                    messageManager.leaveGroup(command[1], userLogged, listener);
                } catch (GroupNotFoundException exception) {
                    System.out.println("ERROR: El grupo introducido no existe");
                } catch (InvalidGroupException exception) {
                    System.out.println("ERROR: No formas parte de este grupo.");
                }
            } else {

                System.out.println("ERROR: Sintaxis erronea: JoinGroup <NombreDelGrupo>");
            }
        } else {
            System.out.println("ERROR: Debes conectarte antes de abandonar un grupo.");
        }
    }


    @Override
    public void sendMsgUser(String userSrc, String message, String date) {
        System.out.println(String.format("[%s] %s dice:  %s", date, userSrc, message));
    }

    @Override
    public void sendMsgGroup(String userSrc, String group, String message, String date) {
        System.out.println(String.format("[%s](%s) %s dice:  %s", date, group, userSrc, message));

    }

    @Override
    public void notifyNewUser(String username) {
        System.out.println(String.format("%s se ha conectado.", username));

    }

    @Override
    public void notifyUserLogout(String username) {
        System.out.println(String.format("%s se ha desconectado.", username));

    }

    @Override
    public void notifyNewGroup(String groupName) {
        System.out.println(String.format("Se ha creado el grupo %s", groupName));

    }

    @Override
    public void notifyUserJoined(String username, String groupName) {
        System.out.println(String.format("%s se ha unido a %s", username, groupName));

    }

    @Override
    public void notifyUserLeave(String username, String groupName) {
        System.out.println(String.format("%s ha salido de %s", username, groupName));

    }

    @Override
    public void notifyDisconnection(String message) {
        System.out.println(message);
        MessengerClient.userLoged = "";
    }
}
