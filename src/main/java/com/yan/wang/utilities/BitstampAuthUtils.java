package com.yan.wang.utilities;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class BitstampAuthUtils {

    public static String apiKey;
    public static String apiKeySecret;
    public static String httpVerb;
    public static String urlHost;
    public static String urlPath;
    public static String urlQuery;
    public static String timestamp;
    public static String nonce;
    public static String contentType;
    public static String version;
    public static String payloadString;
    public static String signature;
    public static SecretKeySpec secretKey;
    public static Mac mac;
    public static byte[] rawHmac;


    public BitstampAuthUtils(String httpProtocol, String urlLink, String apiKey, String apiSecret) throws NoSuchAlgorithmException, InvalidKeyException {
        apiKey = String.format("%s %s", "BITSTAMP", apiKey);
        apiKeySecret = apiSecret;
        httpVerb = httpProtocol;
        urlHost = "www.bitstamp.net";
        urlPath = urlLink;
        urlQuery = "";
        timestamp = String.valueOf(System.currentTimeMillis());
        nonce = UUID.randomUUID().toString();
        contentType = "application/x-www-form-urlencoded";
        version = "v2";
        payloadString = "offset=1";
        signature = apiKey +
                httpVerb +
                urlHost +
                urlPath +
                urlQuery +
                contentType +
                nonce +
                timestamp +
                version +
                payloadString;

        secretKey = new SecretKeySpec(apiKeySecret.getBytes(), "HmacSHA256");
        mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKey);
        rawHmac = mac.doFinal(signature.getBytes());
        signature = new String(Hex.encodeHex(rawHmac)).toUpperCase();
    }
}
