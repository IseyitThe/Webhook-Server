package me.seyit.webhookserver;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WebhookServer {
    private static final String WEBHOOK_FILE = "webhook_info.json";
    private static String WEBHOOK_URL;

    public static void main(String[] args) {
        try {
            loadWebhookUrl();
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/", new RootHandler());
            server.createContext("/webhook", new WebhookHandler());
            server.setExecutor(null);
            server.start();

            System.out.println("Server started on port 8080");

            String serverip = getServerIPAddress();
            System.out.println("Server: " + serverip + ":8080/webhook" );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadWebhookUrl() {
        File file = new File(WEBHOOK_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
                WEBHOOK_URL = jsonObject.get("webhook_url").getAsString();
                if (WEBHOOK_URL.equals("PLACE_WEBHOOK_HERE")) {
                    System.out.println("Please update the webhook URL in the webhook file.");
                    System.exit(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Create Json file
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("webhook_url", "PLACE_WEBHOOK_HERE");
            try (Writer writer = new FileWriter(WEBHOOK_FILE)) {
                Gson gson = new Gson();
                gson.toJson(jsonObject, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Webhook file created. Please update the webhook URL and restart the server.");
            System.exit(0);
        }
    }

    private static String getServerIPAddress() throws IOException {
        URL url = new URL("https://api.ipify.org");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String ipAddress = reader.readLine();
            reader.close();
            return ipAddress;
        } else {
            throw new IOException("Failed to get server IP address: " + responseCode);
        }
    }

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "This is the webhook server from https://github.com/IseyitThe/Webhook-Server";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class WebhookHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }

                JsonObject receivedPayload = new Gson().fromJson(requestBody.toString(), JsonObject.class);
                sendEmbedToDiscordWebhook(receivedPayload);

                exchange.sendResponseHeaders(200, 0);
                OutputStream os = exchange.getResponseBody();
                os.write("OK".getBytes());
                os.close();
            } else {
                String response = "This is the webhook endpoint from https://github.com/IseyitThe/Webhook-Server";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }

        private void sendEmbedToDiscordWebhook(JsonObject receivedPayload) {
            try {
                URL url = new URL(WEBHOOK_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");

                JsonArray embeds = receivedPayload.getAsJsonArray("embeds");
                if (embeds != null) {
                    JsonObject payloadObject = new JsonObject();
                    payloadObject.add("embeds", embeds);

                    Gson gson = new Gson();
                    String payloadJson = gson.toJson(payloadObject);
                    System.out.println("Request Payload: " + payloadJson);

                    byte[] outputInBytes = payloadJson.getBytes(StandardCharsets.UTF_8);
                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(outputInBytes);
                    outputStream.flush();
                    outputStream.close();

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                        System.out.println("Embed message sent successfully!");
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
                } else {
                    System.out.println("Invalid payload: no embeds found.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
