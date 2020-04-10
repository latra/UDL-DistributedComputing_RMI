import exceptions.*;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Scanner;

import static java.lang.System.exit;

public class MessengerClient extends UnicastRemoteObject implements MessageListener {
    private MessengerClient() throws RemoteException {

    }

    public static void main(String[] args) {
        /*
          Función principal de ejecución del cliente. Se encarga de la conexión con el servidor y la lectura de los comandos,
          así como de realizar las solicitudes al objeto remoto.
         */
        System.out.println("MSG: Buscando servicios");
        try {
            String registry = "localhost";
            if (args.length >= 1)
                registry = args[0];
            registry = "rmi://" + registry + "/Messenger";
            Remote servicioRemoto = Naming.lookup(registry);
            MessageManagerRMI messageManager = (MessageManagerRMI) servicioRemoto;

            Scanner input = new Scanner(System.in);
            MessengerClient messageListener = new MessengerClient();

            String userLogged = "";


            System.out.println("MSG: \u00a1Se ha establecido conexi\u00f3n con el servidor!\n\n\n " +
                    "INFO: Utiliza Login <Usuario> [<Contraseña>] para conectar o NewUser <Usuario> <Contraseña> [Grupo] para registrarte.");
            //Inicio del proceso de lectura de comandos
            while (true) {
                System.out.println(" - - - - - Introduce el comando. - - - - -");
                String[] command = input.nextLine().split(" ");
                switch (command[0].toLowerCase()) {
                    case "login":
                        userLogged = loginManager(userLogged, messageManager, messageListener, command);
                        break;
                    case "newuser":
                        registerManager(userLogged, messageManager, command);
                        break;
                    case "logout":
                        logoutManager(userLogged, messageManager);
                        userLogged = "";
                        break;
                    case "sendmsg":
                        sendMessageManager(userLogged, messageManager, command);
                        break;
                    case "newgroup":
                        newGroupManager(messageManager, command);
                        break;
                    case "joingroup":
                        joinGroupManager(userLogged, messageManager, command);
                        break;
                    case "exit":
                        if (!userLogged.isEmpty())
                            logoutManager(userLogged, messageManager);
                        exit(0);
                    default:
                        System.out.println("El comando introducido es incorrecto");
                }
            }
        } catch (NotBoundException notBoundEx) {
            System.err.println("ERROR: No existe el servicio de bombilla en el registro");
        } catch (RemoteException remEx) {
            System.err.println("ERROR: Error remoto - " + remEx);
        } catch (Exception e) {
            System.err.println("ERROR: Error - " + e);
        }
    }

    private static String loginManager(String userLogged, MessageManagerRMI messageManager, MessageListener messageListener, String[] command) throws RemoteException {
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

    private static void registerManager(String userLogged, MessageManagerRMI messageManager, String[] command) {
        /*
          Se encarga de solicitar el registro de un usuario al servicio.
          Puede recibir el error "InvalidRegistryException" si el nombre ya está en uso o
          "GroupNotFoundException" si el grupo no existe.
         */
        if (userLogged.isEmpty())
            if (command.length > 2) {
                String[] groups = new String[]{};
                try {
                    if (command.length > 3)
                        groups = Arrays.copyOfRange(command, 3, command.length);
                    messageManager.register(command[1], command[2], Arrays.asList(groups));
                    System.out.println("INFO: Se ha completado el registro. Ahora ya puedes conectarte.");
                } catch (InvalidRegistryException exception) {
                    System.out.println("ERROR: No se ha podido registrar. Posiblemente se deba a que el usuario ya existe");
                } catch (GroupNotFoundException exception) {
                    System.out.println("ERROR: Alguno de los grupos indicados no existen. Se ha creado el usuario, pero no se ha unido a los grupos.");
                } catch (Exception e) {
                    System.out.println("ERROR: - " + e);
                }
            } else {
                System.out.println("ERROR: Sintaxis erronea: NewUser <Usuario> <Contraseña> [grupos]");
            }
        else
            System.out.println("ERROR: Desconectate antes de crear otra cuenta. Puedes usar el comando Logout");
    }

    private static void logoutManager(String userLogged, MessageManagerRMI messageManager) throws RemoteException {
        /*
          Se encarga de solicitar la desconexión en el servidor del usuario. Si por algún motivo ya está considerado
          como desconectado en el servidor, recibirá en error "LogoutException"
         */
        if (!userLogged.isEmpty()) {
            try {

                messageManager.logout(userLogged);
                System.out.println("INFO: Te has desconectado correctamente");

            } catch (LogoutException exception) {
                System.out.println("INFO: El usuario ya había sido desconectado por el servidor. Puedes volver a conectarte.");
            }
        } else
            System.out.println("ERROR: No estás conectado.");
    }

    private static void sendMessageManager(String userLogged, MessageManagerRMI messageManager, String[] command) throws RemoteException {
        /*
          Solicita al servicio el envio de un mensaje a un usuario o grupo concreto (o a ambos simultáeamente).
          Si el usuario receptor no existe, se recibirá el error UserNotFoundException
          Si el usuario receptor está desconectado, se recibirá el error UserDisconnectedException.
          Si el grupo receptor no existe o el usuario no forma parte de él, se recibirá el error InvalidGroupException
         */
        if (!userLogged.isEmpty()) {
            if (command.length > 2) {
                String[] message;
                String receiver = "";
                String gropup = "";
                if (command[1].equals("-g")) {
                    gropup = command[2];
                    message = Arrays.copyOfRange(command, 3, command.length);
                } else {
                    receiver = command[1];
                    if (command[2].equals("-g")) {
                        gropup = command[3];
                        message = Arrays.copyOfRange(command, 4, command.length);

                    } else {
                        message = Arrays.copyOfRange(command, 2, command.length);
                    }
                }
                StringBuilder sb = new StringBuilder();
                for (String s : message) {
                    sb.append(s).append(" ");
                }
                String str = sb.toString();
                try {
                    messageManager.sendMessage(str, userLogged, receiver, gropup);
                } catch (UserNotFoundException exception) {
                    System.out.println("ERROR: Usuario no encontrado");
                } catch (UserDisconnectedException exception) {
                    System.out.println("ERROR: El usuario está desconectado.");
                } catch (InvalidGroupException exception) {
                    System.out.println("ERROR: El grupo indicado o bien no existe o no formas parte de él");
                }

            } else {
                System.out.println("ERROR: Sintaxis errónea: SendMsg <Usuario> [-g grupo] <TextoMensaje>");
            }
        } else {
            System.out.println("ERROR: Debes conectarte antes de enviar un mensaje.");
        }
    }

    private static void newGroupManager(MessageManagerRMI messageManager, String[] command) throws RemoteException {
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

    private static void joinGroupManager(String userLogged, MessageManagerRMI messageManager, String[] command) throws RemoteException {
        /*
          Solicita al servicio la unión en un grupo especificado.
          Si el grupo no existe se recibirá el error "GroupNotFoundEsception".
          Si el usuario ya formaba parte del grupo, se recibirá el error "InvalidGroupException".
         */
        if (!userLogged.isEmpty()) {
            if (command.length == 2) {
                try {
                    messageManager.joinGroup(userLogged, command[1]);
                } catch (GroupNotFoundException exception) {
                    System.out.println("ERROR: El grupo introducido no existe");
                }catch (InvalidGroupException exception) {
                    System.out.println("ERROR: Ya formas parte de este grupo.");
                }
            } else {

                System.out.println("ERROR: Sintaxis erronea: JoinGroup <NombreDelGrupo>");
            }
        } else {
            System.out.println("ERROR: Debes conectarte antes de unirte a un grupo.");
        }
    }

    @Override
    public void messageReceived(String message) {
        /*
          Recibe las notificaciones (mensajes) mediante callback y muestra el mensaje por pantalla.
         */
        System.out.println(message);
    }
}
