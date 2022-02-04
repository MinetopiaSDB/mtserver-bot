package nl.mtserver.discordbot.dns;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class CloudflareProvider implements DNSProvider {

    private final String zoneId, domain, authEmail, authKey;
    private final HttpClient client;

    public CloudflareProvider(String zoneId, String domain, String authEmail, String authKey) {
        this.zoneId = zoneId;
        this.domain = domain;
        this.authEmail = authEmail;
        this.authKey = authKey;

        this.client = HttpClient.newHttpClient();
    }

    @Override
    public CompletableFuture<Boolean> createSubdomain(String subdomain, String ip, int port) {
        return createRecord("A", subdomain + "-ipv4." + domain, ip,false).thenApplyAsync(success -> {
            if (!success) {
                return false;
            }
            return createRecord("SRV", subdomain + "." + domain, ip, port, true).thenApply(srvSuccess -> {
                if (!srvSuccess) {
                    // TODO: delete A record?
                }
                return srvSuccess;
            }).join();
        });
    }

    @Override
    public CompletableFuture<Boolean> createRecord(String type, String name, String content, boolean proxied) throws RuntimeException {
        return createRecord(type, name, content, null, proxied);
    }

    @Override
    public CompletableFuture<Boolean> createRecord(String type, String name, String content, Integer priority, boolean proxied) throws RuntimeException {
        JsonObject body = new JsonObject();
        body.addProperty("type", type);
        body.addProperty("name", name);
        body.addProperty("content", content);
        body.addProperty("proxied", proxied);

        if (priority != null)
            body.addProperty("priority", priority);

        return request("/zones/" + zoneId + "/dns_records", "POST", body.getAsJsonObject()).thenApply(jsonElement -> {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (!jsonObject.get("success").getAsBoolean()) {
                JsonArray errors = jsonObject.get("errors").getAsJsonArray();
                // Throw errors from Cloudflare API as runtime exception
                errors.forEach(error -> {
                    throw new RuntimeException(error.getAsJsonObject().get("message").getAsString());
                });
                return false;
            }
            // No errors, so subdomain was created successfully
            return true;
        });
    }

    private CompletableFuture<JsonElement> request(String endpoint, String method, JsonElement body) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.cloudflare.com/client/v4/" + endpoint))
                    .method(method, HttpRequest.BodyPublishers.ofString(body.toString()))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .header("X-Auth-Email", authEmail)
                    .header("X-Auth-Key", authKey)
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(httpResponse -> JsonParser.parseString(httpResponse.body()));
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
