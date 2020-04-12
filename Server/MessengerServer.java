import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import static java.lang.System.exit;

public class MessengerServer extends MessageManagerServant {
    public static void main(String[] args) {
        try {
            System.setSecurityManager(new SecurityManager());

            System.out.println("Cargando servicios RMI. Levantando gestor de mensajes");

            MessageManagerServant messageManager = new MessageManagerServant();
            // Generamos los stubs de forma dinámica
            UnicastRemoteObject.exportObject(messageManager, 0);
            //Asignamos la ruta correspondiente
            String serverIP = "localhost";
            if (args.length >= 1) {
                serverIP = args[0];
            }
            System.out.println(String.format("El gestor de mensajes se va a levantar en la IP %s", serverIP));
            String route = "rmi://" + serverIP + "/Messenger";
            System.out.println(String.format("La ruta del objeto es; %s", route));
            // Asignamos el valor del hostname a la ip del servidor
            System.setProperty("java.rmi.server.hostname", serverIP);

            // Vinculamos el objeto messageManager a la ruta asignada
            Naming.rebind(route, messageManager);
            System.out.println(String.format("Objeto bindeado %s", route));

        } catch (SecurityException ex) {
            System.out.println("Permisos insuficientes.");
            System.out.println(ex.getMessage());
            exit(1);
        } catch (RemoteException var6) {
            System.err.println("Remote Error - " + var6);
        } catch (Exception var7) {
            System.err.println("Error - " + var7);
        }
    }
}
