package ru.nsu.veretennikov.learning;

import java.util.concurrent.CompletableFuture;

import static java.lang.Thread.interrupted;
import static java.lang.Thread.sleep;

public class E5WeatherAndPopulation {
    public static String getCoordinates(String city) throws InterruptedException {
        sleep(2000);
        return "55.75, 37.61"; // Moscow's coordinates
    }

    public static String getWeatherByCoordinates(String coordinates) throws InterruptedException {
        sleep(3000);
        return "Weather at " + coordinates + ": Sunny, temp = 25";
    }

    public static int getPopulationByCoordinates(String coordinates) throws InterruptedException {
        sleep(1000);
        return 100001;
    }

    public static void main(String[] args) {
        CompletableFuture.supplyAsync(() -> {
            try {
                String coordinates = getCoordinates("Moscow");
                System.out.println("Coordinates: " + coordinates);
                return coordinates;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).thenCompose(coordinates -> {
            CompletableFuture<String> futureWeather = CompletableFuture.supplyAsync(() -> {
                try {
                    return getWeatherByCoordinates(coordinates);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            CompletableFuture<Integer> futurePopulation = CompletableFuture.supplyAsync(() -> {
                try {
                    return getPopulationByCoordinates(coordinates);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            return CompletableFuture.allOf(futureWeather, futurePopulation)
                    .thenApply(v -> futureWeather.join() + ", Population: " + futurePopulation.join());
        }).thenAccept(System.out::println)
        .join();
    }
}
