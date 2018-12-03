/**
 *  ChatServe.java
 *  The server portion of a client/server chat system. This program starts a socket server and waits for an incoming client.
 *  When a client connects to the server, the server waits for the client to send a message.
 *  Then the server sends a message and this form of back to back messaging continues until one side types \quit
 *  
 *  Usage:   java ChatServe server_port_number
 *  
 *  CS372-400-W15
 *  Project 1
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
 *  http://stackoverflow.com/questions/15613626/scanner-is-never-closed
 *  	had issues with closing the scanner
 *  http://rosettacode.org/wiki/Handle_a_signal#Java
 *  	used this code to figure out how to create a signal handler for SIGINT
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ChatServe 
{
	//Global variable to accomadate the GRACEFULLY closing of the program for SIGINT
	static ServerSocket server2 = null;
	static Scanner scan = null;
	static Socket client = null;
	
	public static void main(String[] args)  throws InterruptedException
	{
		 //signal handling code to catches SIGINT and GRACEFULLY close program
		 Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() 
		 {
			 public void run() 
			 {           	
           		closeServer();					
			 }
		 }));
			
		//declare variables
		int portNumber = 0;
		
		//main scanner to get user input		
		scan = new Scanner(System.in);
		
		//process command line arguments
		try
		{
			//parse port numbers
			portNumber = Integer.parseInt(args[0]);				
		}
		catch(Exception e)
		{
			System.out.println("Port Number was not provided or was not an int. Please restart program");
			System.out.println("Proper Usage:   java ChatServe server_port_number");	
			System.exit(0);
		}		
		
		//start and run server		
		server2 = startServer(portNumber);
		
		runServer(server2, scan);		
		
	    System.exit(0);
	}	


	/*
	 * function that GRACEFULLY shuts down the ServerSocket	
	 */	
	public static void closeServer()
	{
		try {		
			scan.close();
			if(client != null)
			{
				client.close();
			}
			server2.close();
			System.out.print("\nServerSocket closed, program shutdown.\n");
		} catch (IOException e) {			
			
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
			System.out.println("Server Started.");	
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
	 * param: Scanner         the main scanner for the program, to get input from server user
	 */	
	public static void runServer(ServerSocket server, Scanner scan)
	{
		//used to 
		boolean run = true;
		
		try 
		{				
			//while loop that waits for a connection from a client
			while(true)
			{
				System.out.print("Chat Server is waiting for clients...\n");
				//attempt to accept a client connection
				client = server.accept();	            
				System.out.println("Client has connected.");
				
				//while loop for receiving and sending messages with client
	            while(run)
	            {
		            //runs receiveMessage() function if the client does not quit the server
	            	//  gets a chance to reply 
	            	//  if this method returns false the client socket will be closed
	            	if (run = receiveMessage(client))
		            {
		            	//code that lets server send message to client
	            		//  if this method returns false the client socket will be closed
	            		run = sendMessage(client, scan);
		            }
		           
		            //if chat is over close client socket connection and server socket
		            if(!run)
		            {
			            client.close();
			            System.out.println("Client Socket closed");
			            break;
		            }	 
	            }
	            //set run variable to true for for next client
	            run = true;
				
			}
		} catch (Exception e) {
			
		}		
	}
	
	
	
			
	/*
	 * function that waits for and receives message from client
	 * 
	 * param:  Socket    the client socket
	 * return: clientSocketOpen   true = keep client socket open, false = close client socket
	 */	
	public static boolean receiveMessage(Socket client)
	{
		boolean clientSocketOpen = true;	//return variable 		
		String clientMessage= "";			//string to store incoming client message		
		BufferedReader in = null;			//used to read buffer from socket
		
		try
		{
			//create readers to get and process buffer stream from socket
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			//while loop to create state of back and forth chat
			
			System.out.println("waiting for client to reply...");
		
			//attempt to read incoming message from client
			clientMessage = in.readLine();
			
			//check if client wants to quit the chat
			if(clientMessage.contains("\\quit")) 
			{
				System.out.println("Client Left Chat");				
				clientSocketOpen = false;				
			}
			else
			{				
				//print client message
				System.out.println(clientMessage);				
			}						
		}
		catch(IOException ex)
		{
			System.out.println("Error creating or reading client input bufferreader");
			clientSocketOpen = false;
		}
		
		return clientSocketOpen;
	}
		
		
	/*
	 *  function that receives message from server user and sends it to client
	 * 
	 *  param: Socket     the client socket
	 *  param: Scanner    the main scanner for the program, to get input from server user
	 *  return: clientSocketOpen    true = keep client socket open, false = close client socket
	 */	
	public static boolean sendMessage(Socket client, Scanner scan)
	{
		String serverMessage = "";			//string to store message from server user	
		boolean clientSocketOpen = true;    //return variable
		PrintWriter out = null;			    //writer output stream to send data back to client
		boolean breakLoop = false;				
        try
        {
        	//writer for outgoing server messages
        	out = new PrintWriter(client.getOutputStream(),true);
					
			while (true)
			{
	        	System.out.print("Server> ");
				
				//get server message input from command line
				serverMessage =  scan.nextLine();
				
				//check for max length of 500 characters for message
				if (serverMessage.trim().length() > 500)
				{
					System.out.print("Message longer than 500 characters. Make it shorter please.\n");
					breakLoop = false;					
				}
				else if (serverMessage.trim().length() == 0)
				{					
					System.out.print("You did not enter a message.\n");
					breakLoop = false;
				}
				else
				{				
					//send server message to client
					out.println("Server> " + serverMessage);
					out.flush();
					//check if server wants to quit chat
					if (serverMessage.contains("\\quit"))
					{
						System.out.println("Chat ended");				
						clientSocketOpen = false;
					}	
					breakLoop = true;
				}				
				//sent successfull message to client break loop
				if (breakLoop)
					break;				
			}
        }
        catch(IOException ex)
        {
        	System.out.println("Error creating or sending message to client");
        	clientSocketOpen = false;
        }			
		
		return clientSocketOpen;
	}
	
	/*
	 * function that asked server user if the want to keep the serversocket listening for clients
	 * 
	 * return: response   true - kill server, false - keep server running
	 */	
	public static boolean quitServerResponse(Scanner scan)
	{
		boolean response = false;		
		String quitServer;
		
		System.out.print("Do you want to quit the Server? (Type Y or N) : ");
		
		//get server message input from command line
		quitServer =  scan.nextLine();
        
		//process input
		if(quitServer.toLowerCase().equals("y"))
        {	            	
			System.out.print("Server has Terminated!\n");
			response = true;			
        }		
		
		return response;
	}
		
}
