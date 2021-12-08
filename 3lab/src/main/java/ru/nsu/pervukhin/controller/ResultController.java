package ru.nsu.pervukhin.controller;

import javafx.stage.Stage;
import ru.nsu.pervukhin.model.PlaceConfig;
import ru.nsu.pervukhin.view.ResultUI;

import java.util.Vector;

import static ru.nsu.pervukhin.model.ConstClass.*;
import static ru.nsu.pervukhin.model.ConstClass.WAY_TO_SNOW_WEATHER;

public class ResultController {

    public void drawResult(Stage stage, PlaceConfig weatherInPlace, Vector<PlaceConfig> localPlacesInfo) {
        String str = checkWeather(weatherInPlace.getWeather());
        ResultUI resultUI = new ResultUI();
        resultUI.start(stage);
        resultUI.drawWeather(weatherInPlace.getTemp(), str, weatherInPlace.getWeather());
        resultUI.drawPlaces(localPlacesInfo);
    }

    public String checkWeather(String string) {
        if (string.contains("rain"))
            return WAY_TO_RAIN_WEATHER;
        if (string.contains("cloud"))
            return WAY_TO_CLOUD_WEATHER;
        if (string.contains("clear"))
            return WAY_TO_CLEAR_WEATHER;
        if (string.contains("snow"))
            return WAY_TO_SNOW_WEATHER;
        return "";
    }
}
