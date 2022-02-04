package nl.mtserver.discordbot.dns;

import java.util.concurrent.CompletableFuture;

public class CfUtil {

    private CfUtil() {
    }

    public static <T> CompletableFuture<T> completedExceptionally(Throwable throwable) {
        final CompletableFuture<T> cf = new CompletableFuture<>();
        cf.completeExceptionally(throwable);
        return cf;
    }

}
