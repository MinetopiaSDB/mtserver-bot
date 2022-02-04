package nl.mtserver.discordbot.utils;

import java.util.concurrent.CompletableFuture;

public class CompletableFutureUtil {

    private CompletableFutureUtil() {
    }

    public static <T> CompletableFuture<T> completedExceptionally(Throwable throwable) {
        final CompletableFuture<T> cf = new CompletableFuture<>();
        cf.completeExceptionally(throwable);
        return cf;
    }

}
