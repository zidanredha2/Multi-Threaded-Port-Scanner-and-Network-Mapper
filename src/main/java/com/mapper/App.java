package com.mapper;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
        String targetHost = System.getenv("SCAN_TARGET")!=null?System.getenv("SCAN_TARGET"):"127.0.0.1";
        int startPort = 1;
        int endPort = 1024;
        int timeoutMs = 200;
        int threadPoolSize = System.getenv("SCAN_THREADS")!=null?Integer.parseInt(System.getenv("SCAN_THREADS")):100;
        System.out.println("==================================================");
        System.out.println("   MULTI-THREADED PORT SCANNER ENGINE INITIALIZED ");
        System.out.println("==================================================");
        System.out.printf("Target Host   : %s%n", targetHost);
        System.out.printf("Port Range    : %d - %d%n", startPort, endPort);
        System.out.printf("Concurrency   : %d active worker threads%n", threadPoolSize);
        System.out.println("Scanning... Please wait.");
        System.out.println("--------------------------------------------------");
        long startTime = System.currentTimeMillis();
        List<Integer> openPorts = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int currentPort=startPort;currentPort<=endPort;currentPort++){
            final int port = currentPort;
            executor.submit(()->{
                if(isPortOpen(targetHost,port,timeoutMs)){
                    openPorts.add(port);
                    System.out.printf("[FOUND] ➔ Port %d is listening.%n", port);
                }
            });
        }
        try {
            executor.invokeAll(tasks);
        } catch(InterruptedException e){
            System.err.println("The scanner was interrupted successfully.");
            Thread.currentThread().interrupt();
        }finally {
            executor.shutdown();
        }
        try{
            if(!executor.awaitTermination(50, TimeUnit.MILLISECONDS)){
                System.out.println("Warning: The scan timed out before completing all ports.");
            }
        }catch(InterruptedException e){
            System.err.println("The scanner main loop was interrupted critically.");
            Thread.currentThread().interrupt();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("--------------------------------------------------");
        System.out.println("                  FINAL SCAN REPORT               ");
        System.out.println("--------------------------------------------------");
        Collections.sort(openPorts);
        if(openPorts.isEmpty()){
            System.out.println("No open ports found within the specified range.");
        } else {
            System.out.printf("Discovered %d active open ports:%n%n", openPorts.size());
            System.out.printf("%-10s | %-15s | %-10s%n", "PORT", "PROTOCOL", "INFERRED SERVICE");
            System.out.println("-----------|-----------------|-----------------");
            for (int port : openPorts) {
                System.out.printf("%-10d | %-15s | %-10s%n", port, "TCP", resolveService(port));
            }
        }
        System.out.println("--------------------------------------------------");
        System.out.printf("Scan completed successfully in %.2f seconds.%n", (endTime - startTime) / 1000.0);
        System.out.println("==================================================");
    }
    private static String resolveService(int port) {
        return switch(port){
            case 21 -> "FTP";
            case 22 -> "SSH / SFTP";
            case 23 -> "Telnet";
            case 25 -> "SMTP";
            case 53 -> "DNS";
            case 80 -> "HTTP (Web)";
            case 110 -> "POP3";
            case 443 -> "HTTPS (Secure Web)";
            case 631 -> "CUPS (Printing)";
            case 8080 -> "Alternative HTTP / Tomcat";
            default -> "Unknown Service";
        };
    }
}
