import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class MessengerServer  extends MessageManagerServant{
    public static void main(String[] args) {
        System.out.println("Cargando servicios RMI. Levantando gestor de mensajes");
        try {
            MessageManagerServant messageManager = new MessageManagerServant();
            UnicastRemoteObject.exportObject(messageManager, 0);
            String serverIP = "localhost";
            if (args.length>=1) {
                serverIP = args[0];
            }
            System.out.println(String.format("El gestor de mensajes se va a levantar en la IP %s", serverIP));
            String route = "rmi://" + serverIP + "/Messenger";
            System.out.println(String.format("La ruta del objeto es; %s", route));

            Naming.rebind(route, messageManager);
            System.out.println(String.format("Objeto bindeado %s", route));
            ManageMessages managerProcess = new ManageMessages(messageManager);
            managerProcess.start();
        } catch (RemoteException var6) {
            System.err.println("Remote Error - " + var6);
        } catch (Exception var7) {
            System.err.println("Error - " + var7);
        }
    }
    static class ManageMessages extends Thread {
        MessageManagerServant managerServant;
        ManageMessages(MessageManagerServant messageManagerServant) {
            this.managerServant = messageManagerServant;
        }
        public void run() {
            try {
                System.out.println("Iniciando escucha de mensajes");

                this.managerServant.messageSender();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
