package com.yan.wang;

import com.yan.wang.utilities.BitstampAuthUtils;
import org.apache.commons.codec.binary.Hex;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        System.out.println( "Hello World!" );

        try {
            BitstampAuthUtils bitstampAuthUtils = new BitstampAuthUtils("POST", "/api/v2/user_transactions/");
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.bitstamp.net/api/v2/user_transactions/"))
                    .POST(HttpRequest.BodyPublishers.ofString(bitstampAuthUtils.payloadString))
                    .setHeader("X-Auth", bitstampAuthUtils.apiKey)
                    .setHeader("X-Auth-Signature", bitstampAuthUtils.signature)
                    .setHeader("X-Auth-Nonce", bitstampAuthUtils.nonce)
                    .setHeader("X-Auth-Timestamp", bitstampAuthUtils.timestamp)
                    .setHeader("X-Auth-Version", bitstampAuthUtils.version)
                    .setHeader("Content-Type", bitstampAuthUtils.contentType)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("status code : " + response.statusCode());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Status code not 200");
            }

            String serverSignature = response.headers().map().get("x-server-auth-signature").get(0);
            String responseContentType = response.headers().map().get("Content-Type").get(0);
            String stringToSign = bitstampAuthUtils.nonce + bitstampAuthUtils.timestamp + responseContentType + response.body();

            bitstampAuthUtils.mac.init(bitstampAuthUtils.secretKey);
            byte[] rawHmacServerCheck = bitstampAuthUtils.mac.doFinal(stringToSign.getBytes());
            String newSignature = new String(Hex.encodeHex(rawHmacServerCheck));

            if (!newSignature.equals(serverSignature)) {
                throw new RuntimeException("Signatures do not match");
            }

            System.out.println(response.body());

            System.out.println("Balance");
            bitstampAuthUtils = new BitstampAuthUtils("POST", "/api/v2/balance/");
            client = HttpClient.newHttpClient();
            request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.bitstamp.net/api/v2/balance/"))
                    .POST(HttpRequest.BodyPublishers.ofString(bitstampAuthUtils.payloadString))
                    .setHeader("X-Auth", bitstampAuthUtils.apiKey)
                    .setHeader("X-Auth-Signature", bitstampAuthUtils.signature)
                    .setHeader("X-Auth-Nonce", bitstampAuthUtils.nonce)
                    .setHeader("X-Auth-Timestamp", bitstampAuthUtils.timestamp)
                    .setHeader("X-Auth-Version", bitstampAuthUtils.version)
                    .setHeader("Content-Type", bitstampAuthUtils.contentType)
                    .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());

            System.out.println("BTSUSD");
            bitstampAuthUtils = new BitstampAuthUtils("GET", "/api/v2/ticker/btcusd/");
            client = HttpClient.newHttpClient();
            request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.bitstamp.net/api/v2/ticker/btcusd/"))
                    .GET()
                    .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
