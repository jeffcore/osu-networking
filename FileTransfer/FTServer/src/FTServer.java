/**
 *  FTServer.java
 *
 *  Usage:   java FTServer server_port_number
 *
 *  CS372-400-W15
 *  Project 2
 *  Jeff Rix
 *  rixj@onid.oregonstate.edu
 *
 *  References
 *  https://norwied.wordpress.com/2012/04/17/how-to-connect-a-python-client-to-java-server-with-tcp-sockets/
 *  	helped me remember how to organize the code for BufferReaders and PrintWriters
 *  Head First Java 2nd Edition - Chapter 15 Networks and Threads - Sierra, Kathy 2005
 *      helped with overall structure of server socket code
 *  Big Java 4th Edition - Chapter 21 Internet Networking - Horstmann, Cay S.
 *      refresher on client and socket code
 *  http://rosettacode.org/wiki/Handle_a_signal#Java
 *  	used this code to figure out how to create a signal handler for SIGINT
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;

public class FTServer
{
    //Global variable to accommodate the gracefully closing of the program for SIGINT
    static ServerSocket server = null;
    static Socket client = null;

    public static void main(String[] args)  throws InterruptedException
    {
        //signal handling code to catches SIGINT and gracefully close program
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
        {
            public void run()
            {
                closeServer();
            }
        }));

        //declare variables
        int portNumber = getPortNumberFromArgs(args);

        //start and run server
        server = startServer(portNumber);

        runServer(server);

        System.exit(0);
    }

    /*
     * Get port number from CLI arguments
     * param: arg    the command line arguments
     * return:  portNumber   the port number for the server socket
     */
    public static int getPortNumberFromArgs(String[] args)
    {
        //declare variables
        int portNumber = 0;

        //process command line arguments
        try
        {
            //parse port numbers
            portNumber = Integer.parseInt(args[0]);
        }
        catch(Exception e)
        {
            System.out.println("Port Number was not provided or was not an int. Please restart program");
            System.out.println("Proper Usage:   java FTServer server_port_number");
            System.exit(0);
        }

        return portNumber;
    }


    /*
     * function that gracefully shuts down the ServerSocket
     */
    public static void closeServer()
    {
        try
        {
            if(client != null)
            {
                client.close();
            }
            server.close();
            System.out.print("\nServerSocket closed, program shutdown.\n");
        }
        catch (Exception e)
        {
            System.out.print("\nServerSocket closed, program shutdown.\n");
            System.exit(0);
        }
    }

    /*
     * create a server socket to listen for clients
     *
     * param:  portNumber    the number of the port the server will be listening on
     * return: ServerSocket    the successfully created server socket
     */
    public static ServerSocket startServer(int portNumber)
    {
        ServerSocket server = null;
        //exit program if there is an error creating ServerSocket
        try
        {
            //create server socket
            server = new ServerSocket(portNumber);
            System.out.println("Server open on port " + portNumber);
        }
        catch(Exception e)
        {
            System.out.println("Error Starting Server. Please restart program.");
            System.exit(0);
        }

        return server;
    }

    /*
     * runs the state of the server, while loops that create the state of accepting clients and
     * message back and forth
     *
     * param: ServerSocket    the successfully created server socket
     */
    public static void runServer(ServerSocket server)
    {
        //while loop that waits for a connection from a client
        try
        {
            while (true)
            {
                System.out.println("Chat Server is waiting for clients...");

                //attempt to accept a client connection
                client = server.accept();
                System.out.println("Connection from " + client.getInetAddress().getHostName());
                //runs receiveMessage() function to wait for client request and process request
                receiveMessage(client);

                if (client != null)
                {
                    client.close();
                    System.out.println("Client Socket closed");
                }
            }
        }
        catch (IOException ex)
        {
            System.out.println("Error connecting to Client");
        }
    }

    /*
     * function that waits for and receives message from client
     *
     * param:  Socket    the client socket
     */
    public static void receiveMessage(Socket client)
    {
        String clientMessage;		//string to store incoming client message
        BufferedReader in;			//used to read buffer from socket
        String request[];           //store incoming command message from client
        //String fileList = "";
        //indexes for incoming messages from client
        final int CLIENT_HOST_NAME = 0;
        final int DATA_PORT = 1;
        final int COMMAND = 2;
        final int FILE_NAME = 3;
        final int LENGTH_OF_FILE_COMMAND = 4;

        try
        {
            //create readers to get and process buffer stream from socket
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            //attempt to read incoming message from client
            clientMessage = in.readLine();

            if (clientMessage != null)
            {
                //split contents of client message into array Client host name, data port, command, filename (if needed)
                request = clientMessage.trim().split(":");

                //decide which command to run
                //    send list of files in directory
                if (request[COMMAND].equals("-l"))
                {
                    //send file list to client
                    sendFileListToClient(client, Integer.parseInt(request[DATA_PORT]), request[CLIENT_HOST_NAME]);
                }
                else if (request.length == LENGTH_OF_FILE_COMMAND && request[COMMAND].equals("-g"))   ///send file
                {
                    //send file to client
                    sendFileToClient(client, request[FILE_NAME], Integer.parseInt(request[DATA_PORT]), request[CLIENT_HOST_NAME]);
                }
                else  //error
                {
                    sendMessage(client, "CLOSED");
                    System.out.println("Client command was invalid");
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading or writing from/to socket");
        }
    }

    /*
    *  function that sends file list to client
    *
    *  param: Socket   the client socket
    *  param: String   the file name
    *  param: int      the data port number
    *  param: String   the client data port host name
    */
    public static void sendFileToClient(Socket client, String fileName, int dataPort, String clientHostName)
    {
        System.out.println("File " + fileName + " requested on port " + fileName);

        //open file to send
        File f = new File(fileName);

        //check if file is a file and send it
        if(f.isFile())
        {
            System.out.println("Sending " + fileName + " to " + client.getInetAddress().getHostName() + ":" + dataPort);

            sendMessage(client, "Receiving " + fileName + " to " + client.getInetAddress().getHostName() + ":" + dataPort);

            // launch thread to send file to client
            Thread myThread = new Thread(new FileTransfer(fileName, clientHostName, dataPort, true));
            myThread.start();
        }
        else
        {
            //if there is a problem with the file
            sendMessage(client, "FNF");

            System.out.println("File not found. Sending Error Message to " + client.getInetAddress().getHostName() + ":" + dataPort);
        }
    }

     /*
     *  function that sends file list to client
     *
     *  param: Socket   the client socket
     *  param: int      the data port number
     *  param: String   the client data port host name
     */
     public static void sendFileListToClient(Socket client, int dataPort, String clientHostName)
     {
         String fileList = "";

         System.out.println("Listing directory requested on port " + dataPort);

         sendMessage(client, "Receiving directory structure from " + client.getInetAddress().getHostName() + ":" + dataPort);

         //getting list of files in directory
         File currentDirectory = new File(new File(".").getAbsolutePath());
         int i = 0;
         for (File fileEntry : currentDirectory.listFiles())
         {
             if (fileEntry.isFile())
             {
                 if (i > 0)
                 {
                     fileList += '\n';
                 }
                 fileList += fileEntry.getName();
                 i++;
             }
         }

         //launch thread to send list of files to client
         Thread myThread = new Thread(new FileTransfer(fileList, clientHostName, dataPort, false));
         myThread.start();

         System.out.println("Sending directory contents to " + client.getInetAddress().getHostName() + ":" + dataPort);
     }

    /*
     *  function that sends message to server
     *
     *  param: Socket     the client socket
     *  param: String     message to send to client
     */
    public static void sendMessage(Socket client, String message)
    {
        PrintWriter outStream;			    //writer output stream to send data back to client
        try
        {
            //writer for outgoing server messages
            outStream = new PrintWriter(client.getOutputStream(), true);
            //send server message to client
            outStream.println(message);
            outStream.flush();
            outStream.close();
        }
        catch(IOException ex)
        {
            System.out.println("Error creating or sending message to client");
        }
    }
}
