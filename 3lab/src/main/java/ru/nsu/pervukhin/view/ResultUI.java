package ru.nsu.pervukhin.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ru.nsu.pervukhin.model.PlaceConfig;

import java.io.InputStream;
import java.util.Vector;

import static ru.nsu.pervukhin.model.ConstClass.*;

public class ResultUI extends Application {

    Pane pane = new Pane();

    @Override
    public void start(Stage stage) {
        drawBack(stage);

    }

    public void drawBack(Stage stage) {
        InputStream iconStream = getClass().getResourceAsStream(ICON_PATH);
        Image image = new Image(iconStream);
        stage.getIcons().add(image);

        stage.setScene(new Scene(pane, SCENE_SIZE_X, SCENE_SIZE_Y));
        stage.setResizable(false);
        stage.setTitle("Parser");
    }


    public void drawWeather(Double temp, String weatherIcon, String weather) {
        Image backImage = new Image(weatherIcon, SCENE_SIZE_X, SCENE_SIZE_Y, false, false);
        ImageView imageView = new ImageView();
        imageView.setImage(backImage);
        imageView.setEffect(new GaussianBlur());
        imageView.setOpacity(0.9);
        pane.getChildren().add(imageView);


        Text text = new Text();
        text.setText(weather + " " + temp);
        text.setLayoutY(50);
        text.setLayoutX(10);
        text.setFill(Color.WHITE);
        text.setStyle(WEATHER_TEXT_SETTINGS);
        pane.getChildren().add(text);
    }

    public void drawPlaces(Vector<PlaceConfig> localPlacesInfo) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPrefViewportHeight(500);
        scrollPane.setPrefViewportWidth(650);
        scrollPane.setLayoutX(25);
        scrollPane.setLayoutY(100);
        Text text = new Text();
        Text mainText = new Text();
        for (PlaceConfig place : localPlacesInfo) {
            text.setText(place.getRegion() + " " + place.getCityName() + " " + place.getLocalName() + "\n " + place.getKinds() + " \n" + "\n");
            mainText.setText(text.getText() + mainText.getText());
        }
        mainText.setStyle(FINAL_TEXT_SETTINGS);
        mainText.setFill(Color.WHITE);
        mainText.setEffect(new Glow());
        scrollPane.setContent(mainText);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; ");
        pane.getChildren().add(scrollPane);
    }
}
