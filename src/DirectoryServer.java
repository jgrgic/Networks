import java.util.*;
import java.io.*;
import java.net.*;

//creates a new directory server; used for the DHT
public class DirectoryServer {

    public static void main(String [] args) {

        //initializes a server with a port, a unique identifier, location of the next port in DHT
        //and the next servers IP
        int port = 20310;
        //user inputs which server number this will be
        int serverNum = Integer.parseInt(args[0]);
        int nextPort = 20310;
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

        Runnable runUDP = new Runnable() {
            public void run() {
                System.out.println("Starting UDP thread");

                while(true) {
                    String message;
                }
            }
        };

        public int findUDPPort() {

        }

        //method to send data to the next available server
        public void sendToNextServer(String message) throws IOException {
            //opens a socket on the next server
            Socket nextServer = new Socket(nextServerIP, nextPort);

            //creates an OutPutStream for the next server based on passed data
            OutputStream sentOutData = nextServer.getOutputStream();

            //creates an object to be sent based on the data
            DataOutputStream outbound = new DataOutputStream(sentOutData);

            //attaches a message to it
            outbound.writeUTF(message);

            //closes the server
            nextServer.close();
        }//end of sendToNextServer()

        //method to send data to the client via UDP
        public void sendData(String messageToBeSent, String myIP, int myPort) throws IOException {
            //byte array to store the data
            byte[] dataToBeSent = new byte[1024];

            //get the size of the data to be sent in bytes
            dataToBeSent = messageToBeSent.getBytes();

            //get the ip of the source
            InetAddress IP = InetAddress.getByName(myIP);

            //create a packet with the obtained information
            DatagramPacket packetToBeSent = new DatagramPacket(dataToBeSent, dataToBeSent.length, IP, myPort);

            //send the packet out via UDP
            UDPSocket.send(packetToBeSent);
        } //end of sendData()

        public String[] exit() {

        }

        public String[] init() {

        }
    }

    public static class CustomUDP {

        final int statusCode200 = 200;
        final int statusCode400 = 400;
        final int statusCode404 = 404;
        final int statusCode505 = 505;

        int customPort;
        int nextPort;
        String myIP;
        String nextIP;

        DatagramSocket UDPSocket;
        Thread customThread;

        public CustomUDP() {

        }

        Runnable CustomRunnable = new Runnable() {
            public void run() {

            }
        };

        //kills the thread and closes the socket
        public void kill() {
            customThread.stop();
            UDPSocket.close();
        } //end of kill()

        //method to send data to the next available server
        public void sendToNextServer(String messageToBeSent) throws IOException {
            //opens a socket on the next server
            Socket nextServer = new Socket(nextIP, nextPort);

            //creates an OutPutStream for the next server based on the provided data
            OutputStream sentOutData = nextServer.getOutputStream();

            //creates the object to be sent based on the data
            DataOutputStream outbound = new DataOutputStream(sentOutData);

            //attaches a message to it
            outbound.writeUTF(messageToBeSent);

            //closes the connection
            nextServer.close();
        } //end of sendToNextServer()

        //method to send data to the client via UDP
        public void sendData(String messageToBeSent, String myIP, int myPort) throws IOException {
            //byte array to store the data
            byte[] dataToBeSent = new byte[1024];

            //get the size of the data to be sent in bytes
            dataToBeSent = messageToBeSent.getBytes();

            //get the ip of the source
            InetAddress IP = InetAddress.getByName(myIP);

            //create a packet with the obtained information
            DatagramPacket packetToBeSent = new DatagramPacket(dataToBeSent, dataToBeSent.length, IP, myPort);

            //send the packet out via UDP
            UDPSocket.send(packetToBeSent);
        }//end of sendData()
    }
}
