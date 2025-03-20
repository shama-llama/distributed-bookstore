package rpc;

import jakarta.xml.ws.Endpoint;
import java.sql.SQLException;

public class RPCServer {
    public static void main(String[] args) {
        try {
            String address = "http://0.0.0.0:8080/bookstore";
            RPCServiceImplement service = new RPCServiceImplement();
            Endpoint.publish(address, service);
            System.out.println("RPC Service running at " + address); 
        } catch (SQLException e) {
            System.err.println("Failed to start RPC Server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}