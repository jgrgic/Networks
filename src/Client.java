import java.util.Scanner;
import java.io.*;
import java.net.*;

public class Client {

    static String firstServerIP;
    static int firstServerPort;
    static int peerServerPort;

    public static void main(String [] args) {

        Thread mainThread;

        //port of the client server; always on
        peerServerPort = 20310;
        //port of first server in DHT
        firstServerPort = 20310;
        //IP of first server in DHT
        firstServerIP = "10.17.0.1";

        mainThread = new Thread(mainRunnable);
        mainThread.start();
    }

    static Runnable mainRunnable = new Runnable() {
        public void run() {
            System.out.println("Starting the client");

            //create instance of client and server
            PeerClient client = new PeerClient(firstServerIP, firstServerPort, peerServerPort);
            PeerServer peerServer = new PeerServer(peerServerPort);

            //user input
            Scanner input = new Scanner(System.in);
            String userInput;

            while(true) {
                //menu for user input
                System.out.println("Select an option: U = upload, S = search for content, E = exit");
                System.out.println("Input: ");
                userInput = input.next();

                //case if user wants to upload a file to the network
                if (userInput.equalsIgnoreCase("U")) {
                    System.out.println("Enter the file's name: ");
                    userInput = input.next();

                    int serverNum = 0;

                    //determine which server the file will be uploaded to
                    for (int i = 0; i < userInput.length(); i++) {
                        serverNum = serverNum + userInput.charAt(i);
                    }
                    serverNum = serverNum % 4;

                    //try uploading it to that server, otherwise, timeout
                    try {
                        client.upload(serverNum, userInput);
                    }
                    catch (Exception e) {
                        System.out.println("Cannot connect to server");
                    }

                }

                //case if a user wants to search for a file in the network
                else if (userInput.equalsIgnoreCase("S")) {
                    System.out.println("Enter the file's name: ");
                    userInput = input.next();

                    int serverNum = 0;

                    //determine which server to query for the user based on their input
                    for (int i = 0; i < userInput.length(); i++) {
                        serverNum = serverNum + userInput.charAt(i);
                    }
                    serverNum = serverNum % 4;

                    //try querying that server, otherwise, timeout
                    try {
                        client.query(serverNum ,userInput);
                    }
                    catch (Exception e) {
                        System.out.println("Cannot connect to server");
                    }
                }

                //case if user wants to exit the session
                else if (userInput.equalsIgnoreCase("E")) {
                    try {
                        client.exit();
                    }
                    catch (Exception e) {
                        System.out.println("something");
                    }
                }

                //otherwise, invalid input
                else {
                    System.out.println("Not an option");
                }
            }
        }
    };

    public static class PeerClient {

        //PeerClient parameters
        int serverPort;
        String[] fileName;
        int[] serverPortNumbers; //open ports on all the servers
        String[] serverIPs; //IPs of all the servers
        DatagramSocket UDPSocket;

        //PeerClient constructor; creates it with the first IP, port, and the clients port
        public PeerClient(String firstServerIP, int firstServerPort, int peerServerPort) {

            this.serverPort = peerServerPort;
            this.serverIPs[0] = firstServerIP;
            this.serverPortNumbers[0] = firstServerPort;

            try {
                UDPSocket = new DatagramSocket();
                init();
            }
            catch (Exception e) {
                System.out.println("Could not establish connection");
            }

        } //end of PeerClient() constructor

        //initialize client
        public void init() throws Exception {
            String message;
            String code;
            sendData("all IP", serverIPs[0], serverPortNumbers[0]);
            message = recieveData();
            Scanner scanner = new Scanner(message);
            code = scanner.next();

            for (int i = 0; i < 5; i++) {
                scanner.next();
            }

            if (code.equals("200")) {
                System.out.println("Client as server created");
            }

            for (int i = 0; i < 4; i++) {
                serverIPs[i] = scanner.next();
                serverPortNumbers[i] = Integer.parseInt(scanner.next());
            }
        }//end of init()

        //method to upload data to servers
        //serverNum is which server we are uploading to, nameOfFile is what we want to name it
        public void upload(int serverNum, String userInput) throws Exception {

        }

        //method to query servers for some file
        //serverNum is that which contains the data, nameOfFile is what we are searching for
        public void query(int serverNum, String nameOfFile) throws Exception {

        }

        //exit from network
        public void exit() throws Exception {

        }

        //method that sends data to server
        //copy of the one found in DirectoryServer
        public void sendData(String message, String serverIP, int portNumber) throws IOException {
            byte[] dataToBeSent = new byte[1024];
            dataToBeSent = message.getBytes();
            InetAddress address = InetAddress.getByName(serverIP);
            DatagramPacket packet = new DatagramPacket(dataToBeSent, dataToBeSent.length, address, portNumber);
            UDPSocket.send(packet);
        }//end of sendData()

        //method that receives data
        //copy of the one found in DirectoryServer
        public String receiveData() throws IOException {
            byte[] receivedData = new byte[1024];
            DatagramPacket packet = new DatagramPacket(receivedData, receivedData.length);
            UDPSocket.receive(packet);
            return new String(packet.getData());
        } //end of receiveData()

        public String connectToPeer() {

        }

        public String connectToCustom() {

        }

        public String getResponse() {

        }

        public String createResponse() {

        }

    }
}
