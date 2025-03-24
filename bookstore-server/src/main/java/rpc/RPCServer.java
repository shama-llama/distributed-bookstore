package rpc;

import java.sql.SQLException;

import jakarta.xml.ws.Endpoint;

public class RPCServer {
    private static String ADDRESS = "http://localhost:8080/bookstore";

    public static void main(String[] args) {
        try {
            RPCServiceImplement service = new RPCServiceImplement();
            Endpoint.publish(ADDRESS, service);
            System.out.println("RPC Service running at " + ADDRESS); 
        } catch (SQLException e) {
            System.err.println("Failed to start RPC Server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}