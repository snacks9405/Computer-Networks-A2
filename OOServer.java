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
   public static void main(String[] args) {
      try {
         serverSocket = new ServerSocket(portNumber);
         System.out.println("Server started: " + serverSocket);
         System.out.println("Waiting for a client...");
         clientSocket = serverSocket.accept();
         System.out.println("Connection established: " + clientSocket);
         new Thread(new OO(clientSocket)).run();

         if (clientSocket != null) {
            clientSocket.close();
         }
      } catch (IOException e) {
         System.out.println("Server encountered an error. Shutting down...");
      }
   }// main method

}// OOServer class

class OO implements Runnable {
   static final int MAIN = 0; // M state
   static final int PIZZA_SLICE = 1; // PS state
   static final int HOT_SUB = 2; // HS state
   static final int DISPLAY_ORDER = 3; // DO state
   static final Menu mm = // Main Menu
         new Menu(new String[] { "Main Menu:", "Pizza Slices", "Hot Subs",
               "Display order" });
   static final Menu psm = // Pizza Slice menu
         new Menu(new String[] { "Choose a Pizza Slice:", "Cheese", "Pepperoni",
               "Sausage", "Back to Main Menu", "Display Order" });
   static final Menu hsm = // Hot Sub menu
         new Menu(new String[] { "Choose a Hot Sub:", "Italian", "Meatballs",
               "Back to Main Menu", "Display Order" });
   static final Menu dom = // Display Order menu
         new Menu(new String[] { "What next?", "Proceed to check out",
               "Go Back to Main Menu" });
   int state; // current state
   Order order; // current order
   Socket clientSocket = null; // socket to a client
   DataInputStream in = null; // input stream from client
   DataOutputStream out = null; // output stream to client

   /*
    * Init client socket, current state, and order, and open the necessary
    * streams
    */
   OO(Socket clientSocket) {
      this.clientSocket = clientSocket;
      state = MAIN;
      order = new Order();
      try {
         openStreams(clientSocket);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }// OO constuctor

   /*
    * each execution of this thread corresponds to one online ordering session
    */
   public void run() {
      String separator = "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n";
      try {
         out.writeUTF(separator + "     Welcome to Hot Subs & Wedges!     \n" +
               separator);
         placeOrder();
      } catch (Exception e) {
         System.err.println(e.getMessage());
      } finally {
         close();
      }

   }// run method

   /*
    * implement the OO protocol as described by the FSM in the handout
    * Note that, before reading the first query (i.e., option), the server
    * must display the welcome message shown in the trace in the handout,
    * followed by the main menu.
    */
   void placeOrder() throws IOException {
      String request;
      String separator = "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n";
      int option;
      while (true) {
         Menu curMenu = (state > 1 ? (state == 2 ? hsm : dom) : (state == 0 ? mm : psm));
         if (curMenu == dom) {
            out.writeUTF(order.toString() + "\n" + separator +  curMenu.toString());
         } else {
            out.writeUTF(separator + curMenu.toString());
         }
         while (true) {
            try {
               request = in.readUTF();
               option = Integer.parseInt(request);
               if (option <= curMenu.getNumOptions() - 1 && option > 0) {
                  System.out.println(separator);
                  break;
               }
               out.writeUTF("Invalid Option!\n" + curMenu.toString());
            } catch (Exception e) {
               System.out.println("error with numbers");
            }
         }
         switch (state) {
            case MAIN:
               state = (option < 3 ? (option == 1 ? PIZZA_SLICE : HOT_SUB) : DISPLAY_ORDER);
               break;
            case PIZZA_SLICE:
               if (option < 4) {
                  order.addItem(psm.getOption(option));
               } else {
                  state = (option == 4 ? MAIN : DISPLAY_ORDER);
               }
               break;
            case HOT_SUB:
               if (option < 3) {
                  order.addItem(hsm.getOption(option));
               } else {
                  state = (option == 3 ? MAIN : DISPLAY_ORDER);
               }
               break;
            case DISPLAY_ORDER:

               if (option == 1) {
                  out.writeUTF("Thank you for your visit!");
                  close();
               }
               state = MAIN;
               break;
            default:
               break;
         }
      }

   }// placeOrder method

   /*
    * open the necessary I/O streams and initialize the in and out
    * static variables; this method does not catch any exceptions
    */
   void openStreams(Socket socket) throws IOException {
      in = new DataInputStream(clientSocket.getInputStream());
      out = new DataOutputStream(clientSocket.getOutputStream());
   }// openStreams method

   /*
    * close all open I/O streams and sockets
    */
   void close() {
      try {
         in.close();
         out.close();
         clientSocket.close();
      } catch (Exception e) {
         System.out.println("Error occurred while closing connection");
      }
   }// close method

}// OO class
