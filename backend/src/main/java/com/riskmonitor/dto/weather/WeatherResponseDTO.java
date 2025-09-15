package com.riskmonitor.dto.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.riskmonitor.dto.Rain;
import com.riskmonitor.dto.Snow;
import com.riskmonitor.dto.weather.MainDTO;
import com.riskmonitor.dto.weather.WeatherDTO;
import com.riskmonitor.dto.weather.WindDTO;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherResponseDTO {
    private WeatherDTO[] weather;
    private MainDTO main;
    private WindDTO wind;
    private Rain rain;
    private Snow snow;

    // Getters and setters
    public WeatherDTO[] getWeather() {
        return weather;
    }

    public void setWeather(WeatherDTO[] weather) {
        this.weather = weather;
    }

    public MainDTO getMain() {
        return main;
    }

    public void setMain(MainDTO main) {
        this.main = main;
    }

    public WindDTO getWind() {
        return wind;
    }

    public void setWind(WindDTO wind) {
        this.wind = wind;
    }

    public Rain getRain() {
        return rain;
    }

    public void setRain(Rain rain) {
        this.rain = rain;
    }

    public Snow getSnow() {
        return snow;
    }

    public void setSnow(Snow snow) {
        this.snow = snow;
    }
}
