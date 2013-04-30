package columbia.cellular.api.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author intelWorX
 */
public class ApiAuthenticator {

    protected static String deviceNickname;
    protected static String deviceToken;
    protected final static String CHARSET = "UTF-8";
    public static final int KEY_LENGTH = 8;

    public synchronized static String getPayload() throws UnsupportedEncodingException {
        if (getDeviceNickname() == null || getDeviceToken() == null) {
            return "";
        }

        int timestamp = (int) (System.currentTimeMillis() / 1000);
        String authString = getDeviceToken() + timestamp;
        String key = getRandomString(KEY_LENGTH);
        String signature = hmacSha1(authString, key);

        return String.format("timestamp=%d&signature=%s&key=%s&nickname=%s",
                timestamp,
                URLEncoder.encode(signature, CHARSET),
                URLEncoder.encode(key, CHARSET),
                URLEncoder.encode(getDeviceNickname(), CHARSET));
    }

    protected static String getRandomString(int length) {
        String suspects = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String output = "";
        int randIndex;
        for (int i = 0; i < length; i++) {
            randIndex = (int) Math.floor(Math.random() * suspects.length());
            output += suspects.charAt(randIndex);
        }
        return output;
    }

    public static String hmacSha1(String value, String key) {
        try {
            byte[] keyBytes = key.getBytes();
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            // Compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(value.getBytes());

            return bytesToHex(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the deviceNickname
     */
    public static String getDeviceNickname() {
        return deviceNickname;
    }

    /**
     * @param aDeviceNickname the deviceNickname to set
     */
    public static void setDeviceNickname(String aDeviceNickname) {
        deviceNickname = aDeviceNickname;
    }

    /**
     * @return the deviceToken
     */
    public static String getDeviceToken() {
        return deviceToken;
    }

    /**
     * @param aDeviceToken the deviceToken to set
     */
    public static void setDeviceToken(String aDeviceToken) {
        deviceToken = aDeviceToken;
    }

    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    
}

