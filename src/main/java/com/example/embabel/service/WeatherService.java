package com.example.embabel.service;

import com.embabel.agent.observability.annotation.TrackType;
import com.embabel.agent.observability.annotation.Tracked;
import com.example.embabel.agent.WeatherAgent;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WeatherService {

    private final RestTemplate restTemplate;

    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Tracked(
        value = "callOpenWeatherGeoApi",
        type = TrackType.EXTERNAL_CALL,
        description = "geo call"
    )
    public WeatherAgent.GeoResponse[] getGeo(String geoApiUrl){
        return restTemplate.getForObject(geoApiUrl, WeatherAgent.GeoResponse[].class);
    }

    @Tracked(
        value = "callOpenWeatherApi",
        type = TrackType.EXTERNAL_CALL,
        description = "weather call"
    )
    public WeatherAgent.OpenWeatherResponse getWeather(String weatherApiUrl){
        return restTemplate.getForObject(weatherApiUrl, WeatherAgent.OpenWeatherResponse.class);
    }
}
