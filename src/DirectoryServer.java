import java.util.*;
import java.io.*;
import java.net.*;

//creates a new directory server; used for the DHT
public class DirectoryServer {

    public static void main(String [] args) {

        //initializes a server with a port, a unique identifier, location of the next port in DHT
        //and the next servers IP
        int port, serverNum, nextPort;
        String nextIP;
        Server server = new Server(port, serverNum, nextPort, nextIP);
    }

    public static class Server {

        //200 ok
        final int statusCode200 = 200;
        //400 bad request
        final int statusCode400 = 400;
        //404, file not found
        final int statusCode404 = 404;
        //505, HTTP version not supported
        final int statusCode505 = 505;

        //starting servers port, port number, unique identifier, and IP
        int startingPort, port, serverNum;
        String serverIP;

        //successive servers port, unique identifier, and IP in DHT
        int nextPort, nextServerNum;
        String nextServerIP;

        //main thread depending on type of connection
        Thread mainUDPThread, mainTCPThread;

        //servers sockets for type of connection
        ServerSocket TCPSocket;
        DatagramSocket UDPSocket;

        //actual DHT that we are adding content to
        public static HashTable<String, String> imageList = new HashTable<String, String>();

        public Server(int port, int serverNum, int nextPort, String nextIP) {

            //assigns the servers port, unique identifier, pointer to the next port, and the next servers IP address
            this.port = port;
            this.serverNum = serverNum;
            this.nextPort = nextPort;
            this.nextServerIP = nextIP;

            //if at the max number of servers (4), go to the beginning, otherwise increment
            if (serverNum == 4) {
                this.nextServerNum = 1;
            }
            else {
                this.nextServerNum = serverNum + 1;
            }

            //sets an initial port
            this.startingPort = port + 1;

            //gets your IP; otherwise you don't have one?
            try {
                this.serverIP = InetAddress.getLocalHost().getHostName();
            }
            catch(Exception e) {
                System.out.println("No IP?");
            }

            //starting the server
            System.out.println("Start your servers!");
            System.out.println("IP: " + serverIP + "\nPort: " + port + "\nServer Number: " + serverNum +
                    "\nNext Server Port: " + nextPort + "\nNext Server IP: " + nextServerIP +
                    "\nNext Server Number: " + nextServerNum);

            //try to set up a port connection; creates a UDP and TCP thread and assigns it
            try {
                TCPSocket = newServerSocket(port);

                if (serverNum == 1) {
                    UDPSocket = new DatagramSocket(port);
                }

                mainTCPThread = new Thread(runTCP);
                mainUDPThread = new Thread(runUDP);
                //starts the threads
                mainTCPThread.start();
                mainUDPThread.start();
            }
            catch(Exception e) {
                System.out.println("No port?");
            }
        }

        //implements runnable() because instances of runTCP are to be run by a thread
        Runnable runTCP = new Runnable() {
            public void run() {
                System.out.println("Starting TCP thread");

                while(true) {
                    String message;

                    try {
                        Socket otherPort = TCPSocket.accept();
                        DataInputStream input = new DataInputStream(otherPort.getInputStream());
                        message = input.readUTF();
                        System.out.println("Message from other server: " + message);

                        if (serverNum == 1 && message.contains("")){

                        }
                    }
                    catch (Exception e) {
                        System.out.println("something");
                    }
                }
            }
        };

        Runnable runTDP = new Runnable() {
            public void run() {

            }
        };

        public int findUDPPort() {

        }

        public void sendToNextServer(String message) {

        }

        public void sendToClient() {

        }

        public String[] exit() {

        }

        public String[] init() {

        }
    }

    public static class CustomUDP {

        public CustomUDP() {

        }

        Runnable CustomRunnalbe = new Runnable() {
            public void run() {

            }
        };

        public void kill() {

        }

        public void sendToNextServer() {

        }

        public void sendToClient() {

        }
    }
}
