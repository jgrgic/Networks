# CPS706Project
P2P Network

How to Compile

1. Go to CPS706Project/src
2. Compile using the command: javac *.java

How to Run

DirectoryServer takes arguments in the form of [port, serverNumber, nextServerPort, nextServerIP]. To set up my laptop as a server,
this would be:

      java DirectoryServer 20310 1 20310 10.17.0.1
      
PeerClient takes arguments in the form of [port, firstServerIP, firstServerPort], where firstServerIP and firstServerPort are
the IP & port of the first server in the DHT. As follows:

      java PeerClient 20310 10.17.0.1 20310
      
