package rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIServer {
    private static String IP_ADDR = "192.168.70.125";
    
    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", IP_ADDR);
            RMIServiceInterface service = new RMIServiceImplement(4000);
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("BookstoreService", service);
            System.out.println("RMI Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e);
            e.printStackTrace();
        }
    }
}