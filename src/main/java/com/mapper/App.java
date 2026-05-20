package com.mapper;
import java.net.InetSocketAddress;
import java.net.Socket;
public class App {
    public static boolean isPortOpen(String host, int port, int timeoutMs){
        try (Socket socket = new Socket()){
            socket.connect(new InetSocketAddress(host,port), timeoutMs);
            return true;
        } catch (Exception e){
            return false;
        }
    }
    public static void main(String[] args) {
        String targetHost = "127.0.0.1";
        int timeout = 200;
        System.out.println("Initializing Network Mapper...");
        System.out.println("Scanning some common ports on "+ targetHost+ " sequentially:");
        int[] portsToTest = {22, 80, 443, 8080, 9000};
        for (int port:portsToTest){
            boolean open = isPortOpen(targetHost, port, timeout);
            System.out.printf("➔ Port %d is %s%n", port, open ? "[OPEN]": "[CLOSED/TIMEOUT]");
        }
    }
}
