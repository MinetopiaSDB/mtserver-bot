package nl.mtserver.discordbot.dns;

import com.google.gson.JsonElement;

import java.util.concurrent.CompletableFuture;

public interface DNSProvider {

    public CompletableFuture<Boolean> createSubdomain(String subdomain, String ip, int port);

    public CompletableFuture<Boolean> createRecord(String type, String name, String content, boolean proxied);

    public CompletableFuture<Boolean> createRecord(String type, String name, String content, Integer priority, boolean proxied);

}
