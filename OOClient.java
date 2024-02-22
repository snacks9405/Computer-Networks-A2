/* Client program for the OnlineOrder app

   @author <YOUR FULL NAMES GO HERE>

   @version CS 391 - Spring 2024 - A2
*/

import java.io.*;
import java.net.*;

public class OOClient
{
    static String hostName = "localhost"; // name of server machine
    static int portNumber = 55555;        // port on which server listens
    static Socket socket = null;          // socket to server
    static DataInputStream in = null;     // input stream from server
    static DataOutputStream out = null;   // output stream to server
    static BufferedReader console = null; // keyboard input stream

    /* connect to the server, then repeatedly:
        1. read the reply from the server
        2. read the query string (i.e., menu option) from the user
        3. send the query string to the server
        until the server's reply is "Thank you for your visit!"
        The amount and format of the console output (e.g., user prompt, server
        replies) are imposed as part of the problem statement in the handout.
     */
    public static void main(String[] args)
    {
        try {
            socket = new Socket(hostName, portNumber);
            openStreams();

            String reply="", query="";
            console = new BufferedReader(new InputStreamReader(System.in));

            do {
                if((reply = in.readUTF()).equals("Thank you for your visit!")) break;
                System.out.println(reply);
                query = console.readLine();
                out.writeUTF(query);
            } while(true);
        
        } catch(NumberFormatException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
        
    }// main method

    /* open the necessary I/O streams and initialize the in, out, and console
       static variables; this method does not catch any exceptions.
     */
    static void openStreams() throws IOException
    {
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        
    }// openStreams method

    /* close all open I/O streams and sockets
     */
    static void close()
    {
        try {
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch(IOException e) {
            e.printStackTrace();
        }

    }// close method

}// OOClient class
