# ServerSide Discord Webhook
Serverside discord webhook system, people can't get webhook

## Usage:
- If using Windows, manually open port `8080`
- Change `http://localhost:8080/webhook` to your server IP in `WebhookClient`
- Build Project
- Run `server.jar` on Server. This creates a file named `webhook_info.json`
- Edit Webhook in `webhook_info.json` to your webhook, `{"webhook_url":"PLACE_WEBHOOK_HERE"}`

## Information:
- **Webhook-Server only supports embed**

## Build
- with JDK 17 `gradle build` and `gradle shadowJar` 
