package ru.nsu.pervukhin.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import ru.nsu.pervukhin.model.Parse;
import ru.nsu.pervukhin.model.PlaceConfig;
import ru.nsu.pervukhin.view.ParseMenuUI;

import java.util.Objects;
import java.util.Vector;
import static ru.nsu.pervukhin.model.ConstClass.*;

public class ParseMenuController {

    private static Stage stage;
    private Parse parserOfLocation = new Parse();
    private Vector<PlaceConfig> places;
    private ParseMenuUI parseMenuUI;
    private Vector<PlaceConfig> localPlaces;
    private PlaceConfig weatherInPlace;
    private Vector<PlaceConfig> localPlacesInfo;

    public void setLocalPlaces(Vector<PlaceConfig> localPlaces) {
        this.localPlaces = localPlaces;
    }

    public void setWeatherInPlace(PlaceConfig weatherInPlace) {
        this.weatherInPlace = weatherInPlace;
    }

    public void setLocalPlacesInfo(Vector<PlaceConfig> localPlacesInfo) {
        this.localPlacesInfo = localPlacesInfo;
    }

    private ResultController resultController;

    public void setResultController (ResultController resultController) {this.resultController = resultController;}

    public void setParseMenuUI(ParseMenuUI parseMenuUI) {
        this.parseMenuUI = parseMenuUI;
    }

    public ParseMenuController() {
        parserOfLocation.setParseMenuController(this);
    }

    public void clickSearchButton(Stage primaryStage, Button newGameButton, TextField text) {
        newGameButton.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                try {
                    stage = primaryStage;
                    if (!Objects.equals(text.getText(), "")) {
                        parserOfLocation.findGlobalPlace(text.getText());
                    } else
                        System.out.println("Enter the text");

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public ListView<String> makeCase () {
        ObservableList<String> langs = FXCollections.observableArrayList();
        fillPlaces(langs);
        ListView<String> langsListView = new ListView<>(langs);
        return langsListView;
    }

    public void listenTheCase(ListView<String> langsListView) {
        MultipleSelectionModel<String> langsSelectionModel = langsListView.getSelectionModel();
        langsSelectionModel.selectedItemProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> changed, String oldValue, String newValue) {
                try {
                    parserOfLocation.findPlacesAndWeather(newValue);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void drawCases () {
        places = parserOfLocation.getPlaces();
        parseMenuUI.drawCases();
    }

    public ObservableList<String> fillPlaces(ObservableList<String> langs) {
        for (int i = 0; i < places.size(); i++) {
            langs.add(places.get(i).getCountry() + " "
                    + places.get(i).getPlaceName() + " " +
                    places.get(i).getCityName() + " " +
                    places.get(i).getRegion() + " " +
                    places.get(i).getState());
        }

        return langs;
    }


    public void drawResult (ResultController resultController) throws Exception {
        while (true) {
            if (parserOfLocation.getFlag() == 1) {
                setLocalPlacesInfo(parserOfLocation.getPlacesInfos());
                setWeatherInPlace(parserOfLocation.getWeatherInPlace());
                setResultController(resultController);
                resultController.drawResult(stage, weatherInPlace, localPlacesInfo );
                break;
            }
        }
    }

}
