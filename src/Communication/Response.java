package Communication;

import Database.DB_Connection;
import Messages.Message;

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
    private String from="";
    private String to="";
    private String topic="";
    private String subject="";
    private int content=-1;



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
    }
    /***
     * The response is a single line with two parts:
     * NOW 'time'
     * @throws IOException
     */
    public void TimeResponse() throws IOException {
        writer.write("NOW "+Instant.now().getEpochSecond() + "\n");
        writer.flush();
    }

    /***
     * The responding peer finds every message it has stored with:
     * 1. A Time-sent header that is greater than or equal to since.
     * 2. All of the headers that are given in the request
     * count is the number of messages that it has found.
     * @param time
     * @param header
     * @throws IOException
     */
    public void ListResponse(String time, String header) throws IOException {
        ArrayList<String>listDetails=new ArrayList<String>();
        String statement = "SELECT `Message_id` FROM `Messages` WHERE `Time_sent` >= ?";
        int contentCheck=Integer. parseInt(header);
        if (contentCheck==0){
            /*** only returns the message id with no contents ***/
            try {
                Stm = conn.getConn().prepareStatement(statement);
                Stm.setLong(1, Long.parseLong(time));
                ResultSet rs = Stm.executeQuery();
                while (rs.next()){
                    listDetails.add(rs.getString("Message_id"));
                }
                rs.close();
                Stm.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }else {
            /*** returns the message id with contents based on header value ***/
            try {
                if(!from.equals("")){
                    statement=statement+" AND From_name = " + "\"" + from + "\"";
                }

                if(!to.equals("")){
                    statement=statement+" AND To_name = " + "\"" + to + "\"";
                }

                if(!topic.equals("")){
                    statement=statement+" AND Topic = " + "\"" + topic + "\"";
                }

                if(!subject.equals("")){
                    statement=statement+" AND Subject = " + "\"" + subject + "\"";
                }
                if(content != -1){
                    statement=statement+" AND Contents = " + "\"" + content + "\"";
                    content =-1;
                }
                Stm = conn.getConn().prepareStatement(statement);
                Stm.setLong(1, Long.parseLong(time));
                ResultSet rs = Stm.executeQuery();
                while (rs.next()) {
                    listDetails.add(rs.getString("Message_id"));

                }
                rs.close();
                Stm.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
        writer.write("MESSAGES " + listDetails.size() + "\r\n");
        for(String a:listDetails) {
           // System.out.println(a);
            writer.write(a+"\r\n");
        }
        writer.flush();
    }

    /***
     * Queries the database,
     * Gets all the columns from a message
     * With the id inputted to the console in the client
     * @param id
     */
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

    /***
     * says bye
     */
    public void ByeResponse(){
        System.out.println("BYE!");
    }

    /***
     * Used to present responses for requests, to the user.
     * Navigation of the other functions in the response class, based off of what was read from the console
     * @param msg
     * @throws IOException
     */

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

        }else if(msg.contains("LIST?")){
            String requestTimeHeader = msg.replace("LIST? ","");
            int locateSplit=msg.indexOf(' ');
            String requestNumber = requestTimeHeader.substring(locateSplit+5,requestTimeHeader.length());
            String requestTime = requestTimeHeader.replace(requestNumber,"");
            requestNumber = requestNumber.replace(requestTime,"");
            requestNumber = requestNumber.replace(" ","");

            if ((Integer.parseInt(requestNumber) >=0)&&(Long.parseLong(requestTime)<Instant.now().getEpochSecond())) {
                Message messageUse =new Message();
                for(int i=0;i<Integer.parseInt(requestNumber);i++) {
                    msg = reader.readLine();
                    if (msg.contains("From:")) {
                        from = messageUse.removeHeader(msg);
                    } else if (msg.contains("To:")) {
                        to = messageUse.removeHeader(msg);
                    } else if (msg.contains("Topic:")) {
                        topic = messageUse.removeHeader(msg);
                    } else if (msg.contains("Subject:")) {
                        subject = messageUse.removeHeader(msg);
                    } else if (msg.contains("Contents:")) {
                        content = Integer.parseInt(msg.replace("Contents: ",""));
                    }
                }
                ListResponse(requestTime,requestNumber);

            }
            else if (!((Integer.parseInt(requestNumber) >=0)&&(Long.parseLong(requestTime)<Instant.now().getEpochSecond()))){
                return;
            }

        }else{
            return;
        }

    }
}
