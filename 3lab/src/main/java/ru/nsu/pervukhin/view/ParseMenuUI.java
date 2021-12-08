package ru.nsu.pervukhin.view;

import ru.nsu.pervukhin.controller.ParseMenuController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


import java.io.InputStream;

import static ru.nsu.pervukhin.model.ConstClass.*;

public class ParseMenuUI extends Application {

    private ParseMenuController parseMenuController = new ParseMenuController();

    Pane pane = new Pane();

    public void launchGame(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage stage) {
        parseMenuController.setParseMenuUI(this);
        drawBack(stage);
        stage.show();

    }


    public void drawBack(Stage stage) {

        InputStream iconStream = getClass().getResourceAsStream(ICON_PATH);
        Image image = new Image(iconStream);
        stage.getIcons().add(image);

        stage.setScene(new Scene(pane, SCENE_SIZE_X, SCENE_SIZE_Y));
        stage.setResizable(false);
        stage.setTitle("Parser");


        Image backImage = new Image("/back.jpg", SCENE_SIZE_X, SCENE_SIZE_Y, false, false);
        ImageView imageView = new ImageView();
        imageView.setImage(backImage);
        imageView.setEffect(new GaussianBlur());
        imageView.toFront();
        pane.getChildren().add(imageView);


        Button searchButton = new Button("Search");
        searchButton.setStyle(BUTTON_TEXT_SETTINGS);
        searchButton.setLayoutX((SCENE_SIZE_X / 2) - (BUTTON_SIZE_X / 2));
        searchButton.setLayoutY(SCENE_SIZE_Y * 2 / 3);
        searchButton.setTextFill(Color.BLACK);
        searchButton.setPrefSize(BUTTON_SIZE_X, BUTTON_SIZE_Y);
        searchButton.setOpacity(0.7);
        pane.getChildren().add(searchButton);


        TextField text = new TextField();
        text.setLayoutX((SCENE_SIZE_X / 2) - (TEXT_FIELD_SIZE_X / 2));
        text.setLayoutY(SCENE_SIZE_Y / 2);
        text.setStyle(BUTTON_TEXT_SETTINGS);
        text.setPrefSize(TEXT_FIELD_SIZE_X, TEXT_FIELD_SIZE_Y);
        pane.getChildren().add(text);


        parseMenuController.clickSearchButton(stage, searchButton, text);
    }


    public void drawCases () {
        ListView<String> langsListView = parseMenuController.makeCase();
        langsListView.setOpacity(0.7);
        langsListView.setPrefSize(SCENE_SIZE_X, SCENE_SIZE_Y);
        pane.getChildren().add(langsListView);
        parseMenuController.listenTheCase(langsListView);
    }

}
