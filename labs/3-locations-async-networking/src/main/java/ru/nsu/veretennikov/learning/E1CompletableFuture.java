package ru.nsu.veretennikov.learning;

import java.util.concurrent.CompletableFuture;

public class E1CompletableFuture {
    public static void main(String[] args) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("I am working in the Thread: " + Thread.currentThread().getName());
            try {
                Thread.sleep(2000);
            } catch (Exception ignored) {}
            return "Done!";
        });

        String result = future.join();
        System.out.println(result);
    }
}