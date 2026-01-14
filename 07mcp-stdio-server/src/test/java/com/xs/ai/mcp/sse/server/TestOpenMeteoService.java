package com.xs.ai.mcp.sse.server;

/**
 * Simple test to check if the service methods work
 */
public class TestOpenMeteoService {
    public static void main(String[] args) {
        OpenMeteoService service = new OpenMeteoService();

        System.out.println("Testing getWeatherForecastByLocation...");
        try {
            String weather = service.getWeatherForecastByLocation(39.9042, 116.4074);
            System.out.println("SUCCESS: Weather result length = " + weather.length());
            System.out.println("First 200 chars: " + weather.substring(0, Math.min(200, weather.length())));
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\nTesting getAirQuality...");
        try {
            String airQuality = service.getAirQuality(39.9042, 116.4074);
            System.out.println("SUCCESS: AirQuality result length = " + airQuality.length());
            System.out.println("First 200 chars: " + airQuality.substring(0, Math.min(200, airQuality.length())));
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
