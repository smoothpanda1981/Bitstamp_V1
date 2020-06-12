package com.yan.wang;

import com.yan.wang.dao.AuthenticationPojo;
import com.yan.wang.utilities.BitstampAuthUtils;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

/**
 * improved code
 */
public class AuthenticationV2 {
        public static void main(String[] args) {

            System.out.println("User Transactions :");
            AuthenticationPojo authenticationPojoForUserTransaction = createAuthenticationPojo("xHLaynEDuWW5s0zzg4sKr5Yuju8h9PEA", "wKlcAp6EIgold46jIU4gT15X4gFWf6xV", "POST", "/api/v2/user_transactions/", "v2");
            getPOSTApiCallResponse(authenticationPojoForUserTransaction);

            System.out.println("User Balance :");
            AuthenticationPojo authenticationPojoForBalance = createAuthenticationPojo("xHLaynEDuWW5s0zzg4sKr5Yuju8h9PEA", "wKlcAp6EIgold46jIU4gT15X4gFWf6xV", "POST", "/api/v2/balance/", "v2");
            getPOSTApiCallResponse(authenticationPojoForBalance);

            System.out.println("Bitcoin for USD :");
            AuthenticationPojo authenticationPojoForBTCUSD = createAuthenticationPojo("xHLaynEDuWW5s0zzg4sKr5Yuju8h9PEA", "wKlcAp6EIgold46jIU4gT15X4gFWf6xV", "GET", "/api/v2/ticker/btcusd/", "v2");
            getGETApiCallResponse(authenticationPojoForBTCUSD);
        }

        public static AuthenticationPojo createAuthenticationPojo(String apiKey, String apiKeySecret, String httpVerb, String url, String version) {
            AuthenticationPojo authenticationPojo = new AuthenticationPojo();
            authenticationPojo.setApiKey("BITSTAMP", apiKey);
            authenticationPojo.setApiKeySecret(apiKeySecret);
            authenticationPojo.setHttpVerb(httpVerb);
            authenticationPojo.setUrlHost("www.bitstamp.net");
            authenticationPojo.setUrlPath(url);
            authenticationPojo.setUrlQuery("");
            authenticationPojo.setTimestamp(String.valueOf(System.currentTimeMillis()));
            authenticationPojo.setNonce(UUID.randomUUID().toString());
            authenticationPojo.setContentType("application/x-www-form-urlencoded");
            authenticationPojo.setVersion(version);
            authenticationPojo.setPayloadString("offset=1");
            authenticationPojo.setSignature(authenticationPojo.getApiKey() + authenticationPojo.getHttpVerb() + authenticationPojo.getUrlHost() + authenticationPojo.getUrlPath() + authenticationPojo.getUrlQuery() + authenticationPojo.getContentType() + authenticationPojo.getNonce() + authenticationPojo.getTimestamp() + authenticationPojo.getVersion() + authenticationPojo.getPayloadString());

            return authenticationPojo;
        }

        public static void getPOSTApiCallResponse(AuthenticationPojo authenticationPojo) {
            try {
                SecretKeySpec secretKey = new SecretKeySpec(authenticationPojo.getApiKeySecret().getBytes(), "HmacSHA256");
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(secretKey);
                byte[] rawHmac = mac.doFinal(authenticationPojo.getSignature().getBytes());
                authenticationPojo.setSignature(new String(Hex.encodeHex(rawHmac)).toUpperCase());

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request =  HttpRequest.newBuilder()
                            .uri(URI.create("https://www.bitstamp.net/" + authenticationPojo.getUrlPath()))
                            .POST(HttpRequest.BodyPublishers.ofString(authenticationPojo.getPayloadString()))
                            .setHeader("X-Auth", authenticationPojo.getApiKey())
                            .setHeader("X-Auth-Signature", authenticationPojo.getSignature())
                            .setHeader("X-Auth-Nonce", authenticationPojo.getNonce())
                            .setHeader("X-Auth-Timestamp", authenticationPojo.getTimestamp())
                            .setHeader("X-Auth-Version", authenticationPojo.getVersion())
                            .setHeader("Content-Type", authenticationPojo.getContentType())
                            .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new RuntimeException("Status code not 200");
                }

                String serverSignature = response.headers().map().get("x-server-auth-signature").get(0);
                String responseContentType = response.headers().map().get("Content-Type").get(0);
                String stringToSign = authenticationPojo.getNonce() + authenticationPojo.getTimestamp() + responseContentType + response.body();

                mac.init(secretKey);
                byte[] rawHmacServerCheck = mac.doFinal(stringToSign.getBytes());
                String newSignature = new String(Hex.encodeHex(rawHmacServerCheck));

                if (!newSignature.equals(serverSignature)) {
                    throw new RuntimeException("Signatures do not match");
                }

                System.out.println(response.body());

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    public static void getGETApiCallResponse(AuthenticationPojo authenticationPojo) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.bitstamp.net/" + authenticationPojo.getUrlPath()))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Status code not 200");
            }

            System.out.println(response.body());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
