import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import Communication.Request;
import Database.DB_Connection;
import Messages.Message;

public class Client {

	ArrayList<String>body=new ArrayList<>();//storing the body of the client

	/**
     * Initialise a new client. To run the client, call run().
     */

	public Client() {}

   /**
     * Runs the client.
     * @throws IOException
	* @return
     */
   public boolean run() throws IOException {
	   /*** Connect to the server ***/
	   //InetAddress host = InetAddress.getLocalHost();//TO DO: accept input from the user
	   //127.0.0.1
	   int port = 20111;//Communicates with this port


	   //Socket clientSocket = new Socket(host, port);

	   System.out.println("Enter Destination IP Address: ");
	   Scanner IPAddressSC = new Scanner(System.in);
	   String IPAddress= IPAddressSC.nextLine();


	   System.out.println("Enter Destination Port Address: ");
	   Scanner portSC = new Scanner(System.in);
	   String  portStr = portSC.nextLine();

	   Socket  clientSocket = new Socket(IPAddress, Integer.parseInt(portStr));
	   System.out.println("Client connecting to " + IPAddress + ":" + portStr);

	   BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));// Reading from the network buffer --> the network card deal with connection
	   Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());// Writing to the network buffer --> the network card deal with connection

	   Scanner scanner= new Scanner(System.in);

	   DB_Connection conn1 = new DB_Connection();
	   Message message = new Message(conn1);
	   Request request;
	   String msg;

	   System.out.println("PROTOCOL? " + 1 + " Gera's Client" + "\n");
	   while(true) {
	   	//User friendly menu
		   System.out.println("Menu: \r\n");
		   System.out.println(" - - - - - - - - Polite Messaging - - - - - - - - -");
		   System.out.println("| Would you like to:                               |\r\n");
		   System.out.println("| • Send a message (enter 1)                       |\r\n");
		   System.out.println("| • Make a request (GET? / TIME?/ LIST?)  (enter 2)|\r\n");
		   System.out.println("|              • Enter BYE! to exit •              |");
		   System.out.println(" --------------------------------------------------");
		   msg = scanner.nextLine();

			//lists requests
		   if (msg.equals("1")) {
		   	//message creation , sending and saving all handled in the Message class
			   message.getMessage(scanner, writer);
		   } else if (msg.equals("2")) {
			   System.out.println(" - - - - - - - - - Protocol Menu - - - - - - - - -");
			   System.out.println("| Enter one of the following Requests...          |\r\n");
			   System.out.println("| • PROTOCOL?                                     |\r\n");
			   System.out.println("| • TIME?                                         |\r\n");
			   System.out.println("| • LIST?                                         |\r\n");
			   System.out.println("| • GET?                                          |\r\n");
			   System.out.println("|             • Enter 'BYE!' to exit •            |");
			   System.out.println(" --------------------------------------------------");
			   msg = scanner.nextLine();
			   //request class deals with all commands
			   request = new Request(writer,clientSocket , reader);
			   //main request in the request class checks what the user input, and will navigate to the appropriate function from this
			   request.mainRequest(msg);

		   }else if (msg.equals("BYE!")) {
		   	//breaks the loop if BYE! is entered. stops the client main() program
			   break;
		   } else {
		   	//prevention of breaking out of the loop, error message
			   System.out.println("Please enter an option on the menu \r\n");
		   }
	   }
	   // Close down the connection
	   return(false);
   }

    /**
	 * runs the client
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
    	try{
			while(true){
				//enables the client to constantly send messages and responds to requests until the user inputs "BYE!"
				Client client = new Client();
				boolean checker=client.run();
				if (checker==false){
					break;
				}
			}
    	}catch (IOException  e) {
			e.printStackTrace();
		}
    }
}

