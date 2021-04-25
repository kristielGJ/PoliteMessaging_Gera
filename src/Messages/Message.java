package Messages;

import Communication.Response;
import Database.DB_Connection;
import Messages.Hashing.SHA256;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Scanner;

public class Message {
    String messageID="";
    String to="";
    String from="";
    String topic="";
    String subject="";
    String time="";
    String contents="";
    String bodyString="";
    ArrayList<String> body=new ArrayList<>();//storing the body of the client
    private  Connection conn;

    /**
     * Initialise a message
     */
    public Message(DB_Connection conn) {
        this.conn = conn.getConn();
    }
    public Message() {

    }




    public void getMessage(Scanner scanner, Writer writer) throws IOException {
        /*** Gets user inputs for header messages ***/
        System.out.println("Enter To: ");
        String to = "To: "+scanner.nextLine()+"\r\n";
        System.out.println("Enter From: ");
        String from = "From: "+scanner.nextLine()+"\r\n";
        System.out.println("Enter Topic: ");
        String topic = "Topic: "+scanner.nextLine()+"\r\n";
        System.out.println("Enter Subject: ");
        String subject = "Subject: "+scanner.nextLine()+"\r\n";
        System.out.println("Once you finish writing the body, please press enter twice");
        System.out.println("Enter Body: ");

        //populating the body array list
        String Contents="Contents: ";
        Integer lineNo=0;
        String TempBodyInfo="";

        while(true){
            String input=scanner.nextLine();
            if(input.equals("")){
                break;
                //if the end of a input is fount the loop breaks
            }else{
                body.add(input);
                TempBodyInfo=TempBodyInfo+input+"\r\n";
                lineNo+=1;
                //System.out.println(lineNo+" "+TempBodyInfo);//test
            }
        }
        Contents+=String.valueOf(lineNo)+"\r\n";
        long unixTime = Instant.now().getEpochSecond();
        String time ="Time-sent: "+unixTime+"\r\n";
        String inputsToHash=to+from+topic+subject+Contents+TempBodyInfo;
        String messageID= hashHeaders(inputsToHash);
        requestWriter(writer,messageID,time,to,from,topic,subject,Contents,TempBodyInfo);
    }

    public String hashHeaders(String inputsToHash){
        //using SHA356 class in the Messages.Hashing Package to create the message id
        SHA256 messageIdConversion= new SHA256();
        String messageID="Message-id: SHA-256 "+ messageIdConversion.hashSHA256(inputsToHash)+"\r\n";
        //System.out.println(messageID);//test to view hash code
        return messageID;
    }

    public void serverOutput(Writer writer,Socket clientSocket , ServerSocket serverSocket, DB_Connection conn, BufferedReader reader) throws IOException {

        /*** Output what client says ***/
        String msg;
        Integer i=0;
        boolean messageIDSet = false;
        msg = reader.readLine();
        if(msg.contains("Message-id:")) {
            System.out.println(msg);
            messageID = removeHeader(msg);
            messageIDSet = true;
        }
        while (messageIDSet) {
            String messageClient, messageServer;
            /*** read from client ***/
            msg = reader.readLine();
            System.out.println(msg);
            if (msg == null||msg.equals("")) {
                break;
            }
            else if(msg.contains("Time-sent:")){
                time=removeHeader(msg);
            }else if(msg.contains("To:")){
                to=removeHeader(msg);
            }else if(msg.contains("From:")){
                from=removeHeader(msg);
            }else if(msg.contains("Topic:")){
                topic=removeHeader(msg);
            }else if(msg.contains("Subject:")){
                subject=removeHeader(msg);
            } else if(msg.contains("Contents:")) {
                contents = removeHeader(msg);
                int contentsInt = Integer.parseInt(contents);
                for (int x = 0; x < contentsInt; x++) {
                    msg = reader.readLine();
                    msg = removeHeader(msg);
                    System.out.println(msg);
                    body.add(msg);
                    bodyString += msg + "\r\n";
                }
                saveMessage(messageID, time, from, to, topic, subject, contents, bodyString);
                messageIDSet = false;
                break;
            }
        }
        Response response = new Response(writer,clientSocket ,serverSocket, conn, reader);
        response.mainResponse(msg);
    }



    public void closeClientConnections(Socket clientSocket , BufferedReader reader) throws IOException {
        /*** close connections ***/
        clientSocket.close();
        reader.close();
    }

    public void closeServerConnections(ServerSocket serverSocket) throws IOException {
        /*** close connections ***/
        serverSocket.close();
    }

    public String removeHeader(String msg){
        int locateSplit=msg.indexOf(':');
        if(msg.contains("Message-id:")){
            msg = msg.replace("Message-id: SHA-256 ","");
            return msg;
        }else{
            if(locateSplit == -1 ){
                return msg;
            }
            return msg.substring(locateSplit+2,msg.length());
        }
    }

    public void saveMessage(String messageId,String time,String from, String to, String topic,String subject,String contents,String body){
        System.out.println("Updating Database....");
        try{
            PreparedStatement Stm = conn.prepareStatement("INSERT INTO `Messages`(`Message_id`, `Time_sent`, `From_name`,`To_name`,`Subject`,`Topic`,`Contents`,`Body`) VALUES(?,?,?,?,?,?,?,?);");
            Stm.setString(1, messageId);
            Stm.setString(2,time);
            Stm.setString(3,from);
            Stm.setString(4,to);
            Stm.setString(5,subject);
            Stm.setString(6,topic);
            Stm.setString(7,contents);
            Stm.setString(8,body);
            Stm.executeUpdate();
            Stm.close();
            System.out.println("Database has been successfully updated");

        }catch(SQLException e){
            System.out.println("Database not been updated...");
            e.printStackTrace();
        }
    }
    public void requestWriter(Writer writer,String messageID,String time,String from,String to,String topic,String subject,String Contents,String TempBodyInfo) throws IOException {
        System.out.println("Sending Message");
        writer.write(messageID);
        writer.write(time);
        writer.write(to);
        writer.write(from);
        writer.write(topic);
        writer.write(subject);
        writer.write(Contents);
        writer.write(TempBodyInfo);
        writer.flush();
    }

}
