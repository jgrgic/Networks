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

        ArrayList<CustomUDP> customUDPList = new ArrayList<CustomUDP>();

        //actual DHT that we are adding content to
        public static Hashtable<String, String> imageList = new Hashtable<String, String>();


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
                TCPSocket = new ServerSocket(port);

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
        //runs the main TCP thread; follows the same form as that in CustomUDP class
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

                        //if server number is 1, send OK message w/ data
                        if (serverNum == 1 && message.contains("all IP")){
                            String[] information = init(message);
                            String newMessage = statusCode200 + " " + message + "        ";
                            sendData(newMessage, information[0], Integer.parseInt(information[1]));
                        }
                        //create message and send onwards to next server
                        else if (message.contains("all IP")) {
                            String[] information = init(message);
                            int port;
                            port = findUDPPort();
                            customUDPList.add(new CustomUDP(information[0], port, nextServerIP, nextPort));
                            message = message + " " + serverIP + " " + port;
                            sendToNextServer(message);
                        }
                        //if server number is 1, send OK message w/ data
                        else if (serverNum == 1 && message.contains("exit")) {
                            String[] information = exit(message);
                            message = statusCode200 + "        ";
                            sendData(message, information[0], Integer.parseInt(information[1]));
                        }
                        //create message and send onwards to next server
                        else if (message.contains("exit")) {
                            exit(message);
                            sendToNextServer(message);
                        }
                        otherPort.close();
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

                //follows the same form as that in CustomUDP class
                while(true) {
                    String message;
                    byte[] data = new byte[1024];

                    try {
                        DatagramPacket packet = new DatagramPacket(data, data.length);
                        UDPSocket.receive(packet);
                        message = new String(packet.getData());

                        if (message.contains("all IP")) {
                            int port;
                            port = findUDPPort();
                            customUDPList.add(new CustomUDP(packet.getAddress().getHostAddress(), port, nextServerIP, nextPort));
                            message = "all IP" + packet.getPort() + " " + packet.getAddress().getHostAddress() + " " + serverIP + " " + port;
                            sendToNextServer(message);
                        }
                    }
                    catch (Exception e) {
                        System.out.println("Error");
                    }
                }
            }
        };

        //finds a the port number of some available port
        public int findUDPPort() {

            //port that will be returned
            int port = 0;

            //find a new port from the start one
            int findPort = startingPort;
            boolean finished = false;

            //while port isn't found
            while(finished == false) {
                //when you find one, assigned it the value
                try {
                    DatagramSocket tryingPort = new DatagramSocket(findPort);
                    finished = true;
                    tryingPort.close();
                    break;
                }
                //otherwise, increment to the next port
                catch (SocketException e) {
                   findPort++;
                }
            }
            port = findPort;
            return port;
        }//end of findUDPPort()

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

        public String[] exit(String message) {
            Scanner input = new Scanner(message);
            input.next();
            String IP = input.next();
            int port = input.nextInt();
            int startingPort = 0;

            for (int i = 0; i < serverNum; i++) {
                startingPort = input.nextInt();
            }
            for (int k = 0; k < customUDPList.size(); k++) {
                if (customUDPList.get(k).myIP.equals(IP) && customUDPList.get(k).customPort == startingPort) {
                    customUDPList.get(k).kill();
                    customUDPList.remove(k);
                    break;
                }
            }
            Enumeration images = imageList.keys();
            while (images.hasMoreElements()) {
                String key = (String)images.nextElement();
                if (imageList.get(key).equals(IP)) {
                    imageList.remove(key);
                }
            }
            return new String[] {IP, port + ""};
        } //end of exit()

        //initialize the next server based on the information passed in the message
        public String[] init(String message) {
            Scanner input = new Scanner(message);
            for (int i = 0; i < 3; i++) {
                input.next();
            }
            int port = input.nextInt();
            String IP = input.next();
            return new String[] {IP, port + ""};
        }//end of init()
    }

    public static class CustomUDP {

        //status codes; final because they're used often
        final int statusCode200 = 200;
        final int statusCode400 = 400;
        final int statusCode404 = 404;
        final int statusCode505 = 505;

        int customPort, nextPort;
        String myIP, nextIP;

        DatagramSocket UDPSocket;
        Thread customThread;

        //constructor that creates a unique UDP
        public CustomUDP(String myIP, int port, String nextIP, int nextPort) {
            this.myIP = myIP;
            this.nextIP = nextIP;
            this.nextPort = nextPort;
            customPort = port;

            try {
                //provided the port is available, create the socket
                UDPSocket = new DatagramSocket(port);
            }
            catch (Exception e) {
                //otherwise let the user know the port is unavailable
                System.out.println("Unavailable port");
            }

            //create and start the thread
            customThread = new Thread(CustomRunnable);
            customThread.start();
        } //end of CustomUDP()

        //run the main thread
        Runnable CustomRunnable = new Runnable() {
            public void run() {

                while(true) {

                    //message, really just the file
                    String message;
                    byte[] data = new byte[1024];

                    try {
                        DatagramPacket packet = new DatagramPacket(data, data.length);
                        UDPSocket.receive(packet);
                        message = new String(packet.getData());
                        System.out.println("Executing: " + message);

                        //if the user wants to upload
                        if (message.contains("upload")) {
                            Scanner instruction = new Scanner(message);
                            instruction.next();

                            //gets the name of the file to upload
                            String nameOfFile = instruction.next();

                            //sends the data to the client, OK's it
                            sendData(statusCode200 + "        ", myIP, packet.getPort());

                            //update the servers with the file name and IP of the client that has it
                            Server.imageList.put(nameOfFile, myIP);
                        }
                        //if the user wants to query
                        else if (message.contains("query")) {
                            Scanner instruction = new Scanner(message);
                            instruction.next();
                            String nameOfFile = instruction.next();

                            //gets the IP of the server with that file
                            String serverIP = Server.imageList.get(nameOfFile);

                            //if it doesn't exist, print 404 error
                            if (serverIP == null) {
                                sendData(statusCode404 + "        ", myIP, packet.getPort());
                            }
                            //other wise, query for it and OK
                            else {
                                sendData(statusCode200 + " " + serverIP + "        ", myIP, packet.getPort());
                            }
                        }
                        //if the user wants to exit, send to next server
                        else if (message.contains("exit")) {
                            Scanner instruction = new Scanner(message);
                            message = instruction.next() + " " + myIP + " " + packet.getPort();

                            //goes through the 4 servers
                            for (int i = 0; i < 4; i++) {
                                message = message + " " + instruction.next();
                            }
                            //sends the message
                            sendToNextServer(message);
                        }
                    }
                    catch (Exception e) {
                        System.out.println("there's a problem");
                    }
                }
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
