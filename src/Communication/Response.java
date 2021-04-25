package Communication;

import Database.DB_Connection;
import Database.DB_Connectivity;
import java.sql.PreparedStatement;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;

public class Response {
    private Writer writer;
    private Socket clientSocket;
    private ServerSocket serverSocket;
    private PreparedStatement Stm = null;
    private DB_Connection conn;
    private BufferedReader reader;



    Request request=new Request();
    /**
     * Initialise a response (server's output)
     */
    public Response(Request request){
        this.request=request;
    }

    public Response(Writer writer, Socket clientSocket , ServerSocket serverSocket, DB_Connection conn, BufferedReader reader){
        this.writer = writer;
        this.clientSocket =   clientSocket;
        this.serverSocket = serverSocket;
        this.conn = conn;
        this.reader = reader;
    }

    public void ProtocolResponse() throws IOException {
        writer.write("PROTOCOL? " + 1 + " Gera's System" + "\n");
        writer.flush();
        //String msg = reader.readLine();
        //System.out.println(msg);
    }
    public void TimeResponse() throws IOException {
        writer.write("NOW "+Instant.now().getEpochSecond() + "\n");
        writer.flush();
    }

    public void ListResponse(){
        System.out.println("List: ");

    }

    public void GetResponse(String id){
        System.out.println("Querying Database....");
        try{
            Stm =conn.getConn().prepareStatement("SELECT* FROM `Messages` WHERE `Message_id`=?");
            Stm.setString(1,id);
            ResultSet rs = Stm.executeQuery();
            while (rs.next()) {
                writer.write("FOUND"+"\r\n");
                writer.write("Message-id: "+rs.getString("Message_id")+"\r\n");
                writer.write("Time-sent: "+rs.getString("Time_sent")+"\r\n");
                writer.write("From: "+rs.getString("From_name")+"\r\n");
                writer.write("To: "+rs.getString("To_name")+"\r\n");
                writer.write("Topic: "+rs.getString("Topic")+"\r\n");
                writer.write("Subject: "+rs.getString("Subject")+"\r\n");
                writer.write("Contents: "+rs.getString("Contents")+"\r\n");
                writer.write(rs.getString("Body")+"\r\n");
                writer.flush();
                System.out.println("Database has located the message");
            }
            if (rs==null){
                writer.write("SORRY"+"\r\n");
                writer.flush();
                return;
            }
            return;

        }catch(SQLException | IOException e){
            System.out.println("Database does not have that message");
            e.printStackTrace();
            return;
        }

    }

    public void ByeResponse(){
        System.out.println("BYE!");
    }


    public void mainResponse(String msg) throws IOException {
        if(msg.contains("TIME?")){
            TimeResponse();

        }
        else  if(msg.contains("BYE!")){
            clientSocket.close();
            serverSocket.close();
            System.exit(0);

        }else  if(msg.contains("GET? SHA-256")){
            String id;
            id=msg.replace("GET? SHA-256 ","");
            GetResponse(id);
        }
        else if(msg.contains("PROTOCOL?")){
            ProtocolResponse();
        }

    }
}
