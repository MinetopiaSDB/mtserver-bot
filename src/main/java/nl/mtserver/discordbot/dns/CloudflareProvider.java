package nl.mtserver.discordbot.dns;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import nl.mtserver.discordbot.utils.CompletableFutureUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CloudflareProvider implements DNSProvider {

    private final int providerId;
    private final String zoneId, domain, authEmail, authKey;
    private final HttpClient client;

    public CloudflareProvider(int providerId, String zoneId, String domain, String authEmail, String authKey) {
        this.providerId = providerId;
        this.zoneId = zoneId;
        this.domain = domain;
        this.authEmail = authEmail;
        this.authKey = authKey;

        this.client = HttpClient.newHttpClient();
    }

    public int getDNSProviderId() {
        return this.providerId;
    }

    @Override
    public CompletableFuture<List<String>> createSubdomain(String subdomain, String ip, int port) {
        return createRecord("A", subdomain + "-ipv4." + domain, ip, false).thenCompose(zoneId -> {
            if (zoneId == null) {
                return CompletableFuture.completedFuture(new ArrayList<>());
            }

            JsonObject data = new JsonObject();
            data.addProperty("priority", 1);
            data.addProperty("weight", 1);
            data.addProperty("port", port);
            data.addProperty("target", subdomain + "-ipv4." + domain);
            data.addProperty("service", "_minecraft");
            data.addProperty("proto", "_tcp");
            data.addProperty("name", subdomain + "." + domain);

            return createRecord("SRV", subdomain + "." + domain, ip, data, false).thenApply(recordId -> {
                if (recordId == null) {
                    // TODO: delete A record?
                }
                return Arrays.asList(zoneId, recordId);
            });
        });
    }

    @Override
    public CompletableFuture<String> createRecord(String type, String name, String content, boolean proxied) throws RuntimeException {
        return createRecord(type, name, content, null, proxied);
    }

    @Override
    public CompletableFuture<String> createRecord(String type, String name, String content, JsonObject data, boolean proxied) throws RuntimeException {
        JsonObject body = new JsonObject();
        body.addProperty("type", type);
        body.addProperty("name", name);
        body.addProperty("content", content);
        body.addProperty("proxied", proxied);

        if (data != null)
            body.add("data", data);

        return request("/zones/" + zoneId + "/dns_records", "POST", body.getAsJsonObject())
                .thenCompose(jsonElement -> {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    if (jsonObject.get("success").getAsBoolean()) {
                        // No errors, so subdomain was created successfully
                        JsonObject result = jsonObject.getAsJsonObject("result");
                        return CompletableFuture.completedFuture(result.get("id").getAsString());
                    }

                    JsonArray errors = jsonObject.get("errors").getAsJsonArray();
                    // Throw errors from Cloudflare API as runtime exception
                    String errorMessage = StreamSupport.stream(errors.spliterator(), false)
                            .map(JsonElement::getAsJsonObject)
                            .map(o -> o.get("message").getAsString())
                            .collect(Collectors.joining(", "));
                    return CompletableFutureUtil.completedExceptionally(new RuntimeException(errorMessage));
                });
    }

    private CompletableFuture<JsonElement> request(String endpoint, String method, JsonElement body) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .method(method, HttpRequest.BodyPublishers.ofString(body.toString()))
                    .uri(new URI("https://api.cloudflare.com/client/v4" + endpoint))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .header("X-Auth-Email", authEmail)
                    .header("X-Auth-Key", authKey)
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(JsonParser::parseString);
        } catch (URISyntaxException ex) {
            return CompletableFutureUtil.completedExceptionally(ex);
        }
    }
}
