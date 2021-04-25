package Messages.Hashing;
import java.math.BigInteger;
import java.security.MessageDigest;

public class SHA256 {
    /**
     * Initialise a SHA-256 hash
     */
    public SHA256(){}

    /***
     * The hash MUST be the SHA-256 sum of the
     * rest of the headers and the body of the message.
     * This function calculates a header's hash sum.
     *
     * @param text
     * @return
     */
    public String hashSHA256(String text) {
        String messageID="";
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            digest.update(text.getBytes("utf8"));
            messageID = String.format("%064x",new BigInteger(1,digest.digest()));
            return messageID;
        }
        catch (Exception e){
            e.printStackTrace();
            return "";

        }
    }
}
