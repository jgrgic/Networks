import java.util.Scanner;
import java.io.*;
import java.net.*;

public class PeerClient {

    static String firstServerIP;
    static int firstServerPort;
    static int peerServerPort;

    public static void main(String [] args) {

        Thread mainThread;

        //port of the client server; always on
        peerServerPort = Integer.parseInt(args[0]);
        //port of first server in DHT
        firstServerIP = args[1];
        //IP of first server in DHT
        firstServerPort = Integer.parseInt(args[2]);

        mainThread = new Thread(mainRunnable);
        mainThread.start();
    }

    static Runnable mainRunnable = new Runnable() {
        public void run() {
            System.out.println("Starting the client");
            System.out.println("Here");
            //create instance of client and server
            Client peerClient = new Client(firstServerIP, firstServerPort, peerServerPort);
            System.out.println("Here2");
            PeerServer peerServer = new PeerServer(peerServerPort);

            System.out.println("Here");
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
                        peerClient.upload(serverNum, userInput);
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
                        peerClient.query(serverNum ,userInput);
                    }
                    catch (Exception e) {
                        System.out.println("Cannot connect to server");
                    }
                }

                //case if user wants to exit the session
                else if (userInput.equalsIgnoreCase("E")) {
                    try {
                        peerClient.exit();
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

    public static class Client {

        //PeerClient parameters
        int serverPort;
        String[] fileName;
        int[] serverPortNumbers = new int[4]; //open ports on all the servers
        String[] serverIPs = new String[4]; //IPs of all the servers
        DatagramSocket UDPSocket;

        //PeerClient constructor; creates it with the first IP, port, and the clients port
        public Client(String firstServerIP, int firstServerPort, int peerServerPort) {

            System.out.println("Here3");
            this.serverPort = peerServerPort;
            this.serverIPs[0] = firstServerIP;
            this.serverPortNumbers[0] = firstServerPort;
            System.out.println("Here4");

            try {
                System.out.println("Here5");
                UDPSocket = new DatagramSocket();
                init();
                System.out.println("Here6");
            }
            catch (Exception e) {
                System.out.println("Could not establish connection");
            }

        } //end of PeerClient() constructor

        //initialize client
        public void init() throws Exception {
            String message;
            String code;
            System.out.println("Here7");
            sendData("all IP", serverIPs[0], serverPortNumbers[0]);
            System.out.println("Here8");
            message = receiveData();
            System.out.println("Here11");
            Scanner scanner = new Scanner(message);
            System.out.println("Here12");
            code = scanner.next();

            System.out.println("Here9");
            for (int i = 0; i < 5; i++) {
                scanner.next();
            }
            System.out.println("Here10");

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
        public void upload(int serverNum, String nameOfFile) throws Exception {
            String code;
            String message = "upload " + nameOfFile + " " + InetAddress.getLocalHost().getHostAddress() + "        ";
            sendData(message, serverIPs[serverNum], serverPortNumbers[serverNum]);
            message = receiveData();
            Scanner scanner = new Scanner(message);
            code = scanner.next();
            if (code.equals("200")) {
                System.out.println("File added");
            }
        } //end of upload()

        //method to query servers for some file
        //serverNum is that which contains the data, nameOfFile is what we are searching for
        public void query(int serverNum, String nameOfFile) throws Exception {
            String contact;
            String message = "query " + nameOfFile + "        ";
            String code;
            sendData(message, serverIPs[serverNum], serverPortNumbers[serverNum]);
            message = receiveData();
            Scanner scanner = new Scanner(message);
            code = scanner.next();

            if (code.equals("404")) {
                System.out.println("File not found");
            }
            else if (code.equals("200")) {
                scanner = new Scanner(message);
                scanner.next();
                contact = scanner.next();
                String request = createRequest("get", nameOfFile, "close", InetAddress.getByName(contact).getHostName(), "image/jpg", "en-us");
                message = connectToPeer("open " + nameOfFile, contact, peerServerPort);
                scanner = new Scanner(message);
                code = scanner.next();
                int port = scanner.nextInt();

                if (code.equals("200")) {
                    System.out.println("Connected. Request sent.");
                    connectToCustom(nameOfFile, request, contact, port);
                }
            }
        } //end of query()

        //exit from network
        public void exit() throws IOException {
            byte[] data = new byte[1024];
            String code;
            String message = "exit " + serverPortNumbers[0] + " " + serverPortNumbers[1] + " " + serverPortNumbers[2] + " " + serverPortNumbers[3] + "        ";
            sendData(message, serverIPs[0], serverPortNumbers[0]);
            message = receiveData();
            UDPSocket.close();
            Scanner scanner = new Scanner(message);
            code = scanner.next();

            if (code.equals("200")) {
                System.out.println("Success");
            }
            System.exit(0);
        } //end of exit()

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
            System.out.println("here14");
            byte[] receivedData = new byte[1024];
            System.out.println("here15");
            DatagramPacket packet = new DatagramPacket(receivedData, receivedData.length);
            System.out.println("here16");
            UDPSocket.receive(packet);
            System.out.println("here17");
            return new String(packet.getData());
        } //end of receiveData()

        public String connectToPeer(String message, String IP, int port) throws IOException {
            Socket socket = new Socket(IP, port);
            OutputStream toServer = socket.getOutputStream();
            DataOutputStream output = new DataOutputStream(toServer);
            output.writeUTF(message);
            DataInputStream input = new DataInputStream(socket.getInputStream());
            message = input.readUTF();
            socket.close();
            return message;
        } //end of connectToPeer()

        public void connectToCustom(String nameOfFile, String request, String IP, int port) throws IOException {
            Socket custom = new Socket(IP, port);
            OutputStream toServer = custom.getOutputStream();
            DataOutputStream output = new DataOutputStream(toServer);
            output.writeUTF(request);

            InputStream input = custom.getInputStream();
            DataInputStream inputStream = new DataInputStream(input);
            int length = inputStream.readInt();
            byte[] data = new byte[length];

            if (length > 0) {
                inputStream.readFully(data);
            }

            custom.close();

            String string = new String(data);
            Scanner scanner = new Scanner(string);
            String status = scanner.nextLine() + "\r\n";
            String temp;

            if (status.contains("HTTP/1.1 200 OK")) {
                status = getResponse(scanner, status);
                File outputFile = new File(nameOfFile + ".jpeg");
                int size = data.length - status.getBytes().length;
                byte[] toBytes = new byte[size];

                for (int i = status.getBytes().length; i < data.length; i++) {
                    toBytes[i - status.getBytes().length] = data[i];
                }

                FileOutputStream outputStream = new FileOutputStream(outputFile);
                outputStream.write(toBytes);
                outputStream.close();
            }
            else if (status.contains("HTTP/1.1 400 Bad Request")) {
                status = getResponse(scanner, status);
            }
            else if (status.contains("HTTP/1.1 404 Not Found")) {
                status = getResponse(scanner, status);
            }
            else if (status.contains("HTTP/1.1 505 HTTP Version not supported")) {
                status = getResponse(scanner, status);
            }
            System.out.println(status);
        }

        //gets the response message
        public String getResponse(Scanner scanner, String response) {
            String temp;

            while(scanner.hasNext()) {
                temp = scanner.nextLine() + "\r\n";
                response = response + temp;
                if (temp.equals("\r\n")) {
                    break;
                }
            }
            return response;
        } //end of getResponse()

        //creates a request
        public String createRequest(String req, String item, String connection, String host, String type, String LAN) {
            String request = "";
            request = request + " /" + item + ".jpeg" + "HTTP/1.1\r\n";
            request = request + "Host: " + host + "\r\n";
            request = request + "Connection: " + connection + "\r\n";
            request = request + "Accept: " + type + "\r\n";
            request = request + "Accepted Language: " + LAN + "\r\n";
            return request;
        } //end of createRequest()

    }
}
