package me.seyit.webhookclient;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WebhookClient {
    private static final String SERVER_URL = "http://localhost:8080/webhook";

    public static void main(String[] args) {
        sendEmbedToServer();
    }

    private static void sendEmbedToServer() {
        try {
            URL url = new URL(SERVER_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            JsonObject embedObject = new JsonObject();
            embedObject.addProperty("title", "Example Title");
            embedObject.addProperty("description", "Example embed message.");

            JsonArray fieldsArray = new JsonArray();
            JsonObject field1 = new JsonObject();
            field1.addProperty("name", "Example Field 1");
            field1.addProperty("value", "Example first field.");
            fieldsArray.add(field1);

            JsonObject field2 = new JsonObject();
            field2.addProperty("name", "Example Field 2");
            field2.addProperty("value", "Example second field.");
            fieldsArray.add(field2);

            embedObject.add("fields", fieldsArray);

            JsonArray embedsArray = new JsonArray();
            embedsArray.add(embedObject);

            JsonObject payloadObject = new JsonObject();
            payloadObject.add("embeds", embedsArray);

            Gson gson = new Gson();
            String payloadJson = gson.toJson(payloadObject);
            System.out.println("Request Payload: " + payloadJson);

            byte[] outputInBytes = payloadJson.getBytes(StandardCharsets.UTF_8);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(outputInBytes);
            outputStream.flush();
            outputStream.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                System.out.println("Response Body: " + response.toString());
            } else {
                System.out.println("Error sending embed message: " + responseCode);
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                System.out.println("Response Body: " + response.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
