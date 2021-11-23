package Communication;

import Database.DB_Connection;
import Messages.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Request {


    private Writer writer;
    private Socket clientSocket;
    private ServerSocket serverSocket;
    private DB_Connection conn;
    private BufferedReader reader;
    private boolean protocolSent=false;
    /**
     * Initialise a request (client input/ output)
     */
    public Request(Writer writer, Socket clientSocket ,  BufferedReader reader){
        this.writer = writer;
        this.clientSocket =   clientSocket;
        this.reader = reader;
    }

    public Request(){

    }

    /***
     * Compares protocol versions and displays the highest common protocol version
     * @return
     * @throws IOException
     */

    public boolean ProtocolRequest() throws IOException {
        int protocol=1;
        String myProtocol="PROTOCOL? " + 1 + " Gera's System" + "\n";
        writer.write(myProtocol);
        writer.flush();
        String msg = reader.readLine();
        msg=msg.replace("PROTOCOL? ","");
        msg=msg.replace(" Gera's System","");
        msg=msg.replace(" ","");
        int i = Integer.parseInt(msg);
        if (msg.contains("1")){
            writer.write("PROTOCOL: " + 1  + "\n");
            writer.flush();
            return protocolSent = true;
        }else {
            if(i >protocol){
                writer.write( "PROTOCOL: " + i  + "\n");
                writer.flush();
                return protocolSent = true;
            }else if(i <protocol){
                writer.write("PROTOCOL: " + protocol + "\n");
                writer.flush();
                return protocolSent = true;
            }
        }
        return protocolSent = true;
    }

    /***
     * time is the current time at the peer (server's time)
     * @throws IOException
     */
    public void TimeRequest() throws IOException {
        writer.write("TIME?\n");
        writer.flush();
        String msg;
        while(true){
            msg = reader.readLine();
            if(msg.contains("NOW ")){
                System.out.println(msg);
                break;
            }
        }
    }

    /***
     * Outputs the hash from the Message-id header of each of the messages.
     * @param request
     * @throws IOException
     */

    public void ListRequest(String request) throws IOException {
        System.out.println("Fill out any of the headers below to filter your search, or press enter to leave them empty...");

        Scanner scanner=new Scanner(System.in);
        writer.write(request + "\n");
        System.out.println("Enter To: ");

        String to = scanner.nextLine();
        if(!to.equals("")){
            writer.write("To: "+to+"\r\n");
        }
        System.out.println("Enter From: ");
        String from = scanner.nextLine();
        if(!from.equals("")){
            writer.write("From: "+ from+"\r\n");
        }
        System.out.println("Enter Topic: ");
        String topic = scanner.nextLine();
        if(!topic.equals("")){
            writer.write("Topic: "+topic+"\r\n");
        }
        System.out.println("Enter Subject: ");
        String subject = scanner.nextLine();
        if(!subject.equals("")){
            writer.write("Subject: "+subject+"\r\n");
        }
        System.out.println("Enter Contents: ");
        String content  = scanner.nextLine();
        if(!content.equals("")){
            writer.write("Contents: " + content+"\r\n");
        }

        writer.flush();
        String msg;
        while(true){
            msg = reader.readLine();
            if(msg.contains("MESSAGES")){
                int count = Integer.parseInt(msg.replace("MESSAGES ",""));
                System.out.println(msg);
                for(int i =0; i < count; i++){
                    msg = reader.readLine();
                    System.out.println(msg);
                }
                break;

            }

        }
    }

    /***
     * There are two possible responses. A peer can respond with a single line:
     * SORRY
     * or it can respond with multiple lines, the first is:
     * FOUND
     * @param request
     * @throws IOException
     */
    public void GetRequest(String request) throws IOException {
        writer.write(request + "\n");
        writer.flush();
        String msg;
        while(true){
            msg = reader.readLine();
            if(msg.contains("FOUND")) {
                System.out.println(msg);
                while (true) {
                    msg = reader.readLine();
                    System.out.println(msg);
                    if (msg.contains("Contents:")) {
                        Message message = new Message();
                        int contentsInt = Integer.parseInt(message.removeHeader(msg));
                        for (int x = 0; x < contentsInt; x++) {
                            msg = reader.readLine();
                            System.out.println(msg);
                        }
                        return;
                    }
                }
            }
            else if(msg.contains("SORRY")){
                System.out.println(msg);
                return;
            }
        }
    }

    /***
     * A bye request is a single line:
     * BYE!
     * @throws IOException
     */
    public void ByeRequest() throws IOException {
        writer.write("BYE!\n");
        writer.flush();
        clientSocket.close();
        reader.close();
    }

    /***
     * Handles the client's inputs from the menu found in the Clint class
     * @param request
     * @throws IOException
     */

    public void mainRequest(String request) throws IOException {

       // if(!protocolSent){
         //   ProtocolRequest();
        //}

        if (request.contains("BYE!")) {
           ByeRequest();
        }
        else if(request.contains("TIME?")){
          TimeRequest();
        }
        else if(request.contains("GET? SHA-256")){
            GetRequest(request);

        }else if(request.contains("LIST?")){
               ListRequest(request);
        }
    }
}
