import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;
import java.net.*;


public class PeerServer {

    //declare error codes
    final int statusCode200 = 200;
    final int statusCode400 = 400;
    final int statusCode404 = 404;
    final int statusCode505 = 505;

    //starting port and arrayList that is maintained across the peer servers
    //follows the same format as directory server
    int startingPort;
    ServerSocket serverTCPSocket;
    public static ArrayList<CustomTCP> customTCPList = new ArrayList<CustomTCP>();
    Thread serverThread;

    //creates the PeerServer with some passed port
    public PeerServer(int port) {
        startingPort = port;
        try {
            serverTCPSocket = new ServerSocket(startingPort);
            serverThread = new Thread(TCPRunnable);
            serverThread.start();
        }
        catch(Exception e){
            System.out.println("Unavailable port");
        }
    } //end of PeerServer()

    Runnable TCPRunnable = new Runnable() {
        public void run() {

            while(true) {
                String message;

                try {
                    Socket fromClient = serverTCPSocket.accept();
                    DataInputStream input = new DataInputStream(fromClient.getInputStream());
                    message = input.readUTF();
                    Scanner passedMessage = new Scanner(message);
                    passedMessage.next();

                    int port = findPort();
                    customTCPList.add(new CustomTCP(port));
                    message = statusCode200 + " " + port;

                    DataOutputStream output = new DataOutputStream(fromClient.getOutputStream());
                    output.writeUTF(message);
                    fromClient.close();
                }
                catch(Exception e){
                    System.out.println("Connection error");
                }
            }

        }
    }; //end of TCPRunnable()

    //finds an open port to connect to
    //exactly like findUDPPort in DirectoryServer
    public int findPort(){

        int port = 0;
        int findPort = startingPort;
        boolean finished = false;

        while (finished == false) {
            try {
                ServerSocket possiblePort = new ServerSocket(findPort);
                finished = true;
                possiblePort.close();
                break;
            }
            catch (Exception e) {
                findPort++;
            }
        }
        port = findPort;
        return port;
    }//end of findPort()

    public class CustomTCP {

        //declare error codes
        final int statusCode200 = 200;
        final int statusCode400 = 400;
        final int statusCode404 = 404;
        final int statusCode505 = 505;

        ServerSocket customTCPSocket;
        Thread TCPThread;

        //same as the above constructor for PeerServer
        public CustomTCP(int port) {
            try {
                customTCPSocket = new ServerSocket(port);
                TCPThread = new Thread(TCPRunnable);
                TCPThread.start();
            }
            catch (Exception e) {
                System.out.println("Unavailable port");
            }
        }//end of CustomTCP()

        Runnable TCPRunnable = new Runnable() {
            public void run() {
                try {
                    //empty strings for all the information required to retrieve the file
                    byte[] completeArray = null;
                    String message = "";
                    String nameOfFile = "";
                    String request = "";
                    String version = "";
                    String response = "";
                    String connection = "";
                    String type = "";
                    String timeString = getTime();
                    Socket socket = customTCPSocket.accept();

                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    message = input.readUTF();

                    Scanner scanner = new Scanner(message);
                    request = scanner.next();

                    if (request.equals("get")) {
                        nameOfFile = scanner.next();
                        version = scanner.next();
                        connection = "Close";
                        type = "image/jpg";

                        if (version.equals("HTTP/1.1")) {
                            nameOfFile = nameOfFile.substring(1);
                            File file = new File(nameOfFile);

                            try {
                                String newFile1 = "";
                                String newFile2 = "";
                                newFile1 = nameOfFile.substring(0, nameOfFile.indexOf(".jpg"));
                                newFile2 = nameOfFile.substring(0, nameOfFile.indexOf(".jpg"));

                            }
                            catch (Exception e) {
                                response = createResponse(statusCode400, timeString, null, null, null, connection, null);
                                completeArray = response.getBytes(Charset.forName("UTF-8"));
                            }
                        }
                        else {
                            response = createResponse(statusCode505, timeString, null, null, null, connection, null);
                            completeArray = response.getBytes(Charset.forName("UTF-8"));
                        }

                    }

                    OutputStream output = socket.getOutputStream();
                    DataOutputStream outputStream = new DataOutputStream(output);
                    outputStream.writeInt(completeArray.length);
                    outputStream.write(completeArray, 0, completeArray.length);
                    socket.close();
                    customTCPSocket.close();

                    for (int i = 0; i < PeerServer.customTCPList.size(); i++) {
                        if (PeerServer.customTCPList.get(i).equals(this)) {
                            PeerServer.customTCPList.remove(i);
                            TCPThread.stop();
                            break;
                        }
                    }
                }
                catch (Exception e) {
                    System.out.println("Something went horribly wrong. Good luck figuring it out chump");
                }
            }
        };

        //gets the current time as a string
        public String getTime() {
            Date date = new Date();
            Scanner input = new Scanner(date.toString());
            String day = input.next();
            String month = input.next();
            String number = input.next();
            DateFormat format = new SimpleDateFormat("yyyy HH:mm:ss");
            Date time = new Date();
            String timeString = day + ", " + number + " " + month + " " + format.format(time) + "GMT";
            return timeString;
        }//end of getTime()

        //gets time the file was last modified at
        //follows the same format as getTime()
        public String getModifiedTime(File file) {
            Date date = new Date(file.lastModified());
            Scanner input = new Scanner(date.toString());
            String day = input.next();
            String month = input.next();
            String number = input.next();
            DateFormat format = new SimpleDateFormat("yyyy HH:mm:ss");
            Date time = new Date(file.lastModified());
            String timeString = day + ", " + number + " " + month + " " + format.format(time) + "GMT";
            return timeString;
        }//end of getModifiedTime()

        //creates an HTTP response with appropriate codes and response
        public String createResponse(int code, String currentDate, String lastModifiedDate, String range, String length, String connection, String type) {

            String response = "";

            if (code == statusCode200) {
                //creates the appropriate response code with all information
                response = response + "HTTP/1.1 " + code + "OK\r\n";
                response = response + "Connection: " + connection + "\r\n";
                response = response + "Date: " + currentDate + "\r\n";
                response = response + "Last Modified: " + lastModifiedDate + "\r\n";
                response = response + "Accepted Range: " + range + "\r\n";
                response = response + "Content Length: " + length + "\r\n";
                response = response + "Content Type: " + type + "\r\n\r\n";
            }
            else {
                if (code == statusCode400) {
                    response = response + "HTTP/1.1 " + code + " " + "Bad request\r\n";
                }
                else if (code == statusCode404) {
                    response = response + "HTTP/1.1 " + code + " " + "Not Found\r\n";
                }
                else if (code == statusCode505) {
                    response = response + "HTTP/1.1 " + code + " " + "HTTP version not supported\r\n";
                }
                response = response + "Connection: " + connection + "\r\n";
                response = response + "Date: " + currentDate + "\r\n";
            }
            return response;
        }//end of createResponse()

    }
}
