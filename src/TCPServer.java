import Database.DB_Connection;
import Messages.Message;

import java.sql.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


/***
 * A class to create a TCP Server.
 */
public class TCPServer {
    private final Connection conn;

    /**
     * Initialise a new server. To run the server, call run().
     */
    public TCPServer(DB_Connection conn) {
        this.conn = conn.getConn();
    }

    /**
     * Runs the server.
     * @throws IOException
     * @return
     */
    public void run() throws IOException {

        /*** Set up to accept incoming connections ***/

        int port = 20111;

        /*** Opens the server socket ***/
        System.out.println("Opening the server socket on port " + port);
        ServerSocket serverSocket = new ServerSocket(port);


        /*** Receives client connection ***/

        // Waits until a client connects
        System.out.println("Server waiting for client...");
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client connected!");


        /*** to read data coming from the client ***/
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        Writer writer=new OutputStreamWriter((clientSocket.getOutputStream()));
        DB_Connection conn1 = new DB_Connection();

        Message message=new Message(conn1);

        System.out.println("PROTOCOL? " + 1 + " Gera's Server" + "\n");
        while(true) {
            //goes to the message class where the server outputs are managed in the serverOutput() function
            message.serverOutput(writer,clientSocket,serverSocket,conn1,reader);

        }

    }
    /*** run the program ***/
    public static void main(String[] args) throws IOException {

        try{
            while(true) {
                DB_Connection conn = new DB_Connection();
                TCPServer server = new TCPServer(conn);
                server.run();
        }
        }catch (Exception  e) {
            e.printStackTrace();
        }
    }

}