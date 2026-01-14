/*
 * Copyright 2025-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author brianxiadong
 */
package com.xs.ai.mcp.sse.server;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * 利用OpenMeteo的免费天气API提供天气服务
 * 该API无需API密钥，可以直接使用
 */
@Service
public class OpenMeteoService {

    private static final String WEATHER_API_URL = "https://api.open-meteo.com/v1/forecast";
    private static final String AIR_QUALITY_API_URL = "https://air-quality-api.open-meteo.com/v1/air-quality";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenMeteoService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    // OpenMeteo天气数据模型
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WeatherData(
            @JsonProperty("latitude") Double latitude,
            @JsonProperty("longitude") Double longitude,
            @JsonProperty("timezone") String timezone,
            @JsonProperty("current") CurrentWeather current,
            @JsonProperty("daily") DailyForecast daily,
            @JsonProperty("current_units") CurrentUnits currentUnits) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record CurrentWeather(
                @JsonProperty("time") String time,
                @JsonProperty("temperature_2m") Double temperature,
                @JsonProperty("apparent_temperature") Double feelsLike,
                @JsonProperty("relative_humidity_2m") Integer humidity,
                @JsonProperty("precipitation") Double precipitation,
                @JsonProperty("weather_code") Integer weatherCode,
                @JsonProperty("wind_speed_10m") Double windSpeed,
                @JsonProperty("wind_direction_10m") Integer windDirection) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record CurrentUnits(
                @JsonProperty("time") String timeUnit,
                @JsonProperty("temperature_2m") String temperatureUnit,
                @JsonProperty("relative_humidity_2m") String humidityUnit,
                @JsonProperty("wind_speed_10m") String windSpeedUnit) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record DailyForecast(
                @JsonProperty("time") List<String> time,
                @JsonProperty("temperature_2m_max") List<Double> tempMax,
                @JsonProperty("temperature_2m_min") List<Double> tempMin,
                @JsonProperty("precipitation_sum") List<Double> precipitationSum,
                @JsonProperty("weather_code") List<Integer> weatherCode,
                @JsonProperty("wind_speed_10m_max") List<Double> windSpeedMax,
                @JsonProperty("wind_direction_10m_dominant") List<Integer> windDirection) {
        }
    }

    /**
     * 获取天气代码对应的描述
     */
    private String getWeatherDescription(int code) {
        return switch (code) {
            case 0 -> "晴朗";
            case 1, 2, 3 -> "多云";
            case 45, 48 -> "雾";
            case 51, 53, 55 -> "毛毛雨";
            case 56, 57 -> "冻雨";
            case 61, 63, 65 -> "雨";
            case 66, 67 -> "冻雨";
            case 71, 73, 75 -> "雪";
            case 77 -> "雪粒";
            case 80, 81, 82 -> "阵雨";
            case 85, 86 -> "阵雪";
            case 95 -> "雷暴";
            case 96, 99 -> "雷暴伴有冰雹";
            default -> "未知天气";
        };
    }

    /**
     * 获取风向描述
     */
    private String getWindDirection(int degrees) {
        if (degrees >= 337.5 || degrees < 22.5)
            return "北风";
        if (degrees >= 22.5 && degrees < 67.5)
            return "东北风";
        if (degrees >= 67.5 && degrees < 112.5)
            return "东风";
        if (degrees >= 112.5 && degrees < 157.5)
            return "东南风";
        if (degrees >= 157.5 && degrees < 202.5)
            return "南风";
        if (degrees >= 202.5 && degrees < 247.5)
            return "西南风";
        if (degrees >= 247.5 && degrees < 292.5)
            return "西风";
        return "西北风";
    }

    /**
     * 获取指定经纬度的天气预报
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 指定位置的天气预报
     */
    @Tool(description = "获取指定经纬度的天气预报,根据位置自动推算经纬度")
    public String getWeatherForecastByLocation(
              double latitude,
              double longitude) {
        try {
            String url = WEATHER_API_URL +
                    "?latitude=" + latitude +
                    "&longitude=" + longitude +
                    "&current=temperature_2m,relative_humidity_2m,precipitation,weather_code,wind_speed_10m,wind_direction_10m" +
                    "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,weather_code,wind_speed_10m_max,wind_direction_10m_dominant" +
                    "&timezone=auto";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return "获取天气信息失败: HTTP " + response.statusCode();
            }

            WeatherData data = objectMapper.readValue(response.body(), WeatherData.class);
            return formatWeatherData(data);

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return "获取天气信息失败: " + e.getMessage();
        }
    }

    /**
     * 格式化天气数据为易读的中文格式
     */
    private String formatWeatherData(WeatherData data) {
        StringBuilder sb = new StringBuilder();

        if (data.current() != null) {
            var current = data.current();
            var units = data.currentUnits();

            sb.append("📍 位置: 纬度 ").append(data.latitude())
                    .append(", 经度 ").append(data.longitude()).append("\n");
            sb.append("🕐 当前时间: ").append(current.time()).append("\n");
            sb.append("🌡️ 当前温度: ").append(current.temperature())
                    .append(units != null ? units.temperatureUnit() : "°C").append("\n");
            sb.append("🤴 体感温度: ").append(current.feelsLike()).append("°C\n");
            sb.append("💧 湿度: ").append(current.humidity()).append("%\n");
            sb.append("🌧️ 降水量: ").append(current.precipitation()).append(" mm\n");
            sb.append("☁️ 天气: ").append(getWeatherDescription(current.weatherCode())).append("\n");
            sb.append("💨 风速: ").append(current.windSpeed())
                    .append(units != null ? units.windSpeedUnit() : "km/h")
                    .append(" ").append(getWindDirection(current.windDirection())).append("\n");
        }

        if (data.daily() != null && data.daily().time() != null && !data.daily().time().isEmpty()) {
            var daily = data.daily();
            sb.append("\n📅 未来几天预报:\n");

            int days = Math.min(7, daily.time().size());
            for (int i = 0; i < days; i++) {
                sb.append("  ").append(daily.time().get(i))
                        .append(": ").append(getWeatherDescription(daily.weatherCode().get(i)))
                        .append(", 最高 ").append(daily.tempMax().get(i)).append("°C")
                        .append(", 最低 ").append(daily.tempMin().get(i)).append("°C");
                if (daily.precipitationSum() != null && i < daily.precipitationSum().size()) {
                    sb.append(", 降水 ").append(daily.precipitationSum().get(i)).append(" mm");
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    // OpenMeteo空气质量数据模型
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AirQualityData(
            @JsonProperty("latitude") Double latitude,
            @JsonProperty("longitude") Double longitude,
            @JsonProperty("timezone") String timezone,
            @JsonProperty("current") CurrentAirQuality current,
            @JsonProperty("hourly") HourlyAirQuality hourly,
            @JsonProperty("hourly_units") HourlyUnits hourlyUnits) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record CurrentAirQuality(
                @JsonProperty("time") String time,
                @JsonProperty("pm10") Double pm10,
                @JsonProperty("pm2_5") Double pm25,
                @JsonProperty("carbon_monoxide") Double carbonMonoxide,
                @JsonProperty("nitrogen_dioxide") Double nitrogenDioxide,
                @JsonProperty("sulphur_dioxide") Double sulphurDioxide,
                @JsonProperty("ozone") Double ozone,
                @JsonProperty("european_aqi") Integer europeanAqi,
                @JsonProperty("us_aqi") Integer usAqi) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record HourlyAirQuality(
                @JsonProperty("time") List<String> time,
                @JsonProperty("pm10") List<Double> pm10,
                @JsonProperty("pm2_5") List<Double> pm25,
                @JsonProperty("carbon_monoxide") List<Double> carbonMonoxide,
                @JsonProperty("nitrogen_dioxide") List<Double> nitrogenDioxide,
                @JsonProperty("sulphur_dioxide") List<Double> sulphurDioxide,
                @JsonProperty("ozone") List<Double> ozone,
                @JsonProperty("european_aqi") List<Integer> europeanAqi,
                @JsonProperty("us_aqi") List<Integer> usAqi) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record HourlyUnits(
                @JsonProperty("time") String time,
                @JsonProperty("pm10") String pm10,
                @JsonProperty("pm2_5") String pm25,
                @JsonProperty("carbon_monoxide") String carbonMonoxide,
                @JsonProperty("nitrogen_dioxide") String nitrogenDioxide,
                @JsonProperty("sulphur_dioxide") String sulphurDioxide,
                @JsonProperty("ozone") String ozone,
                @JsonProperty("european_aqi") String europeanAqi,
                @JsonProperty("us_aqi") String usAqi) {
        }
    }

    /**
     * 获取空气质量指数描述
     */
    private String getAirQualityDescription(int aqi) {
        if (aqi <= 50)
            return "优";
        if (aqi <= 100)
            return "良";
        if (aqi <= 150)
            return "轻度污染";
        if (aqi <= 200)
            return "中度污染";
        if (aqi <= 300)
            return "重度污染";
        return "严重污染";
    }

    /**
     * 获取指定经纬度的空气质量信息
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 指定位置的空气质量信息
     */
    @Tool(description = "获取指定位置的空气质量信息,根据位置自动推算经纬度")
    public String getAirQuality(
            @ToolParam(description = "纬度") double latitude,
            @ToolParam(description = "经度") double longitude) {
        try {
            String url = AIR_QUALITY_API_URL +
                    "?latitude=" + latitude +
                    "&longitude=" + longitude +
                    "&current=pm10,pm2_5,carbon_monoxide,nitrogen_dioxide,sulphur_dioxide,ozone,european_aqi,us_aqi" +
                    "&timezone=auto";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return "获取空气质量信息失败: HTTP " + response.statusCode();
            }

            AirQualityData data = objectMapper.readValue(response.body(), AirQualityData.class);
            return formatAirQualityData(data);

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return "获取空气质量信息失败: " + e.getMessage();
        }
    }

    /**
     * 格式化空气质量数据为易读的中文格式
     */
    private String formatAirQualityData(AirQualityData data) {
        StringBuilder sb = new StringBuilder();

        sb.append("📍 位置: 纬度 ").append(data.latitude())
                .append(", 经度 ").append(data.longitude()).append("\n");

        if (data.current() != null) {
            var current = data.current();

            sb.append("🕐 更新时间: ").append(current.time()).append("\n\n");

            sb.append("🌫️ 空气质量指数 (AQI):\n");
            if (current.europeanAqi() != null) {
                sb.append("  欧洲AQI: ").append(current.europeanAqi())
                        .append(" (").append(getAirQualityDescription(current.europeanAqi())).append(")\n");
            }
            if (current.usAqi() != null) {
                sb.append("  美国AQI: ").append(current.usAqi())
                        .append(" (").append(getAirQualityDescription(current.usAqi())).append(")\n");
            }

            sb.append("\n🔬 污染物浓度:\n");

            if (current.pm25() != null) {
                sb.append("  PM2.5: ").append(String.format("%.1f", current.pm25())).append(" μg/m³\n");
            }
            if (current.pm10() != null) {
                sb.append("  PM10: ").append(String.format("%.1f", current.pm10())).append(" μg/m³\n");
            }
            if (current.carbonMonoxide() != null) {
                sb.append("  一氧化碳 (CO): ").append(String.format("%.1f", current.carbonMonoxide()))
                        .append(" μg/m³\n");
            }
            if (current.nitrogenDioxide() != null) {
                sb.append("  二氧化氮 (NO₂): ").append(String.format("%.1f", current.nitrogenDioxide()))
                        .append(" μg/m³\n");
            }
            if (current.sulphurDioxide() != null) {
                sb.append("  二氧化硫 (SO₂): ").append(String.format("%.1f", current.sulphurDioxide()))
                        .append(" μg/m³\n");
            }
            if (current.ozone() != null) {
                sb.append("  臭氧 (O₃): ").append(String.format("%.1f", current.ozone()))
                        .append(" μg/m³\n");
            }
        }

        return sb.toString();
    }

}