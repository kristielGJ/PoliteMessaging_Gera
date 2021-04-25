package Communication;

import Database.DB_Connection;
import Messages.Message;
import jdk.swing.interop.SwingInterOpUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;

public class Request {


    private Writer writer;
    private Socket clientSocket;
    private ServerSocket serverSocket;
    private DB_Connection conn;
    private BufferedReader reader;
    private boolean protocolSent = false;
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

    public void ProtocolRequest() throws IOException {
        System.out.println("SENT PROTOCOL");
        writer.write("PROTOCOL? " + 1 + " Gera's System" + "\n");
        writer.flush();
        protocolSent = true;
        String msg = reader.readLine();
        System.out.println(msg);
    }
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

    public void ListRequest(String time, String headers){
        System.out.println("List?");
        System.out.println(time);
        System.out.println(headers);
        //Look at the sent headers to form sql statements
        //loop through number of headers using a for loop
        //no prep statements, use concatenation for sql statements (or similar)


    }

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

    public void ByeRequest() throws IOException {
        writer.write("BYE!\n");
        writer.flush();
        clientSocket.close();
        reader.close();
    }

    public void mainRequest(String request) throws IOException {

        if(protocolSent == false){
            ProtocolRequest();
            protocolSent = true;
            System.out.println(protocolSent);
        }

        if (request.contains("BYE!")) {
           ByeRequest();
        }
        else if(request.contains("TIME?")){
          TimeRequest();
        }
        else if(request.contains("GET? SHA-256")){
            GetRequest(request);

        }else if(request.contains("LIST?")){
            System.out.println(Instant.now().getEpochSecond());
            String requestTimeHeader = request.replace("LIST? ","");
            int locateSplit=request.indexOf(' ');
            String requestNumber = requestTimeHeader.substring(locateSplit+5,requestTimeHeader.length());
            String requestTime = requestTimeHeader.replace(requestNumber,"");
            requestNumber = requestNumber.replace(" ","");
            requestNumber = requestNumber.replace(requestTime,"");

            if ((Integer.parseInt(requestNumber) >=0)&&(Long.parseLong(requestTime)<Instant.now().getEpochSecond())){
                ListRequest(requestTime,requestNumber);
            }
            else if (!((Integer.parseInt(requestNumber) >=0)&&(Long.parseLong(requestTime)<Instant.now().getEpochSecond()))){
                return;
            }

        }else{
            return;
        }

    }


}
