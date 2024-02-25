/* Server program for the OnlineOrder app

   @author YOUR FULL NAMES GO HERE

   @version CS 391 - Spring 2024 - A2
*/

import java.net.*;
import java.io.*;
import java.util.*;

public class OOServer {
   static ServerSocket serverSocket = null; // listening socket
   static int portNumber = 55555; // port on which server listens
   static Socket clientSocket = null; // socket to a client

   /*
    * Start the server then repeatedly wait for a connection request, accept,
    * and start a new thread to handle one online order
    */
    public static void main(String[] args)
    {
      try {
         serverSocket = new ServerSocket(portNumber);

         while(true) {
            clientSocket = serverSocket.accept();
            new Thread(new OO(clientSocket)).start();
         }

      } catch(IOException e) {
         e.printStackTrace();
      }
        
    }// main method

}// OOServer class

class OO implements Runnable
{
    static final int MAIN = 0;          // M state
    static final int PIZZA_SLICE = 1;   // PS state
    static final int HOT_SUB = 2;       // HS state
    static final int DISPLAY_ORDER = 3; // DO state
    static final Menu mm =              // Main Menu
        new Menu( new String[] { "Main Menu:", "Pizza Slices", "Hot Subs",
        "Display order" } );
    static final Menu psm =             // Pizza Slice menu
        new Menu( new String[] { "Choose a Pizza Slice:", "Cheese", "Pepperoni",
        "Sausage", "Back to Main Menu", "Display Order" } );
    static final Menu hsm =             // Hot Sub menu
        new Menu( new String[] { "Choose a Hot Sub:", "Italian", "Meatballs",
        "Back to Main Menu", "Display Order"  } );
    static final Menu dom =             // Display Order menu
        new Menu( new String[] { "What next?", "Proceed to check out",
        "Go Back to Main Menu"  } );
    int state;                          // current state
    Order order;                        // current order
    Socket clientSocket = null;         // socket to a client
    DataInputStream in = null;          // input stream from client
    DataOutputStream out = null;        // output stream to client

    /* Init client socket, current state, and order, and open the necessary
       streams
     */
    OO(Socket clientSocket)
    {
      this.clientSocket = clientSocket;
      order = new Order();
      state = MAIN;
    }// OO constuctor

    /* each execution of this thread corresponds to one online ordering session
     */
    public void run()
    {
      String bar = "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n";

      try {
         System.out.println("New connection established: " + clientSocket);
         openStreams(clientSocket);
         
         out.writeUTF(bar
            + "     Welcome to Hot Subs & Wedges!\n"
            + bar
            + "\n"
            + mm.toString());

         while(state != -1)
            placeOrder();

      } catch(UnknownHostException e) {
         e.printStackTrace();
      } catch(IOException e) {
         e.printStackTrace();
      } finally {
         close();
      }
        

    }// run method

    /* implement the OO protocol as described by the FSM in the handout
       Note that, before reading the first query (i.e., option), the server
       must display the welcome message shown in the trace in the handout,
       followed by the main menu.
     */
    void placeOrder() throws IOException
    {
      Menu[] menus = new Menu[] { mm, psm, hsm, dom };
      String bar = "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n";
      StringBuilder query = new StringBuilder();
      
      try {
         int reply = Integer.parseInt(in.readUTF());
         if(reply <= 0 || reply > menus[state].getNumOptions())
            throw new NumberFormatException();
         
         if(state == MAIN) {
            state = reply;
         } else if(state == DISPLAY_ORDER) {
            if(reply == 1) {
               state = -1;
               out.writeUTF("Thank you for your visit!");
               return;
            }
            state = MAIN;
         } else {
            if(reply == menus[state].getNumOptions()-1) state = DISPLAY_ORDER;
            if(reply == menus[state].getNumOptions()-2) state = MAIN;
            if(reply < menus[state].getNumOptions()-2) order.addItem(menus[state].getOption(reply));
         }
         query.append(bar);
         
      } catch(NumberFormatException e) {
         query.append("Invalid option!\n");
      }

      if(state == DISPLAY_ORDER)
         query.append(order.toString()).append("\n").append(bar);
      query.append(menus[state].toString());
      out.writeUTF(query.toString());


    }// placeOrder method

   /* open the necessary I/O streams and initialize the in and out
      static variables; this method does not catch any exceptions
    */
    void openStreams(Socket socket) throws IOException
    {
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        
    }// openStreams method

    /* close all open I/O streams and sockets
     */
    void close()
    {
        try {
            if (clientSocket != null) clientSocket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }// close method
}// OO class
