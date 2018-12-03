/**
 * FileTransfer.java
 * creates a thread for the data connection that sends the file and file list
 *
 *  CS372-400-W15
 *  Project 2
 *  Jeff Rix
 *  rixj@onid.oregonstate.edu
 *
 */

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;


public class FileTransfer implements Runnable {
    private String fileName;
    private String serverName;
    private int  portNumber;
    private boolean sendFile;

    public FileTransfer(String fileName, String ipAddress, int portNumber, boolean sendFile)
    {
        this.fileName = fileName;
        this.serverName = ipAddress;
        this.portNumber = portNumber;
        this.sendFile = sendFile;
    }

    /*
     * Send file to client
     */
    public void sendFile() {
        Socket sock;
        BufferedOutputStream outStream;
        File myFile;
        FileInputStream fis;
        BufferedInputStream fileStream;

        try {
            sock = new Socket(this.serverName, this.portNumber);

            //gets client sockets output stream
            outStream = new BufferedOutputStream(sock.getOutputStream());

            // creates the file stream to send the file
            myFile = new File(this.fileName);
            fis = new FileInputStream(myFile);
            fileStream = new BufferedInputStream(fis);

            byte[] byteArray = new byte[1024];

            //System.out.println("input and out stream created");

            int readLength;
            while ((readLength = fileStream.read(byteArray)) > 0) {
                outStream.write(byteArray, 0, readLength);
                outStream.flush();
            }

            outStream.close();
            fileStream.close();
            fis.close();
            sock.close();
        } catch(FileNotFoundException e) {
            System.out.println("error loading file to socket " + this.portNumber);
        } catch (SocketException e) {
            System.out.println("error opening socket " + this.portNumber);
        } catch (UnknownHostException e) {
            System.out.println("error with unknown host" + this.portNumber);
        } catch (IOException e) {
            System.out.println("error sending to socket " + this.portNumber);
        }
    }

    /*
     *   send a string client
     */
    public void sendString()
    {
        Socket sock;
        PrintWriter outStream;			    //writer output stream to send data back to client
        try
        {
            //System.out.println("Connecting to " + serverName + " on port " + portNumber);
            sock = new Socket(this.serverName, this.portNumber);

            //writer for outgoing server messages
            outStream = new PrintWriter(sock.getOutputStream(), true);
            //send server message to client
            outStream.println(this.fileName);
            outStream.flush();
            outStream.close();
            sock.close();
        }
        catch(IOException ex)
        {
            System.out.println("Error creating or sending message to client");
        }
    }

    @Override
    public void run()
    {
        if (sendFile)
        {
            sendFile();
        }
        else
        {
            sendString();
        }
    }

}
