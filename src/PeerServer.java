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
    ServerSocket serverTCPsocket;
    public static ArrayList<CustomTCP> customTCPList = new ArrayList<CustomTCP>();
    Thread serverThread;

    //creates the PeerServer with some passed port
    public PeerServer(int port) {
        startingPort = port;
        try {
            serverTCPsocket = new ServerSocket(startingPort);
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
                    Socket fromClient = serverTCPsocket.accept();
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
    };

    public int findPort(){

    }

    public class CustomTCP {

        public CustomTCP(int port) {

        }

        Runnable TCPRunnable = new Runnable() {
            public void run() {

            }
        };

        public String getTime() {

        }

        public String getModifiedTime(File file) {

        }

        public String createResponse() {

        }

    }
}
