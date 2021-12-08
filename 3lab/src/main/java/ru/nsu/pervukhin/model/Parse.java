package ru.nsu.pervukhin.model;


import com.fasterxml.jackson.databind.ObjectMapper;
import ru.nsu.pervukhin.controller.ParseMenuController;
import ru.nsu.pervukhin.controller.ResultController;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


import java.net.HttpURLConnection;

import java.net.URL;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class Parse {


    private Vector<PlaceConfig> places = new Vector<>();
    private Vector<PlaceConfig> localPlaces = new Vector<>();
    private ParseMenuController parseMenuController;
    private PlaceConfig weatherInPlace;
    private Vector<PlaceConfig> placesInfos = new Vector<>();
    private ResultController resultController = new ResultController();
    private int flag = 0;

    public PlaceConfig getWeatherInPlace() {
        return weatherInPlace;
    }

    public Vector<PlaceConfig> getPlacesInfos() {
        return placesInfos;
    }

    public void setParseMenuController(ParseMenuController parseMenuController) {
        this.parseMenuController = parseMenuController;
    }

    public int getFlag() {
        return flag;
    }

    public Vector<PlaceConfig> getPlaces() {
        return places;
    }

    public void findGlobalPlace(String text) throws ExecutionException, InterruptedException {
        CompletableFuture<Void> globalPlaces = CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL("https://graphhopper.com/api/1/geocode?q=" + text.replaceAll(" ", "%20") + "&key=24cf0ac9-dd29-453d-826d-17306ab6b5ee");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();
                int responsecode = conn.getResponseCode();
                if (responsecode != 200) {
                    throw new RuntimeException("HttpResponseCode: " + responsecode);
                } else {
                    String inline = "";
                    Scanner scanner = new Scanner(url.openStream());
                    while (scanner.hasNext()) {
                        inline += scanner.nextLine();
                    }
                    scanner.close();


                    JSONParser parser = new JSONParser();
                    JSONObject data_obj = (JSONObject) parser.parse(inline);
                    JSONArray arr = (JSONArray) data_obj.get("hits");
                    ObjectMapper mapper = new ObjectMapper();
                    for (int i = 0; i < arr.size(); i++) {
                        JSONObject new_obj = (JSONObject) arr.get(i);
                        if (new_obj.get("name").equals(text)) {
                            JSONObject new_obj1 = (JSONObject) new_obj.get("point");
                            String JSONString = "{\"longitude\":" + new_obj1.get("lng") + ",\"latitude\":" + new_obj1.get("lat") + ",\"state\":\"" + new_obj.get("osm_value") + "\"" + ",\"country\":\"" + new_obj.get("country") + "\"" + ",\"region\":\"" + new_obj.get("state") + "\"" + ",\"lang\":\"" + data_obj.get("locale") + "\"" + ",\"placeName\":\"" + new_obj.get("name") + "\"" + ",\"cityName\":\"" + new_obj.get("city") + "\"" + "}";
                            PlaceConfig placeConfig = mapper.readValue(JSONString, PlaceConfig.class);
                            places.add(placeConfig);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        globalPlaces.get();
        globalPlaces.thenRun(() -> {
            try {
                parseMenuController.drawCases();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    public void findPlacesAndWeather(String string) throws Exception {
        PlaceConfig ourPlace = new PlaceConfig();

        for (int i = 0; i < places.size(); i++) {
            if (string.contains(places.get(i).getCountry())
                    && string.contains(places.get(i).getPlaceName())
                    && string.contains(places.get(i).getRegion())
                    && string.contains(places.get(i).getState())
                    && string.contains(places.get(i).getCityName())) {
                ourPlace = places.get(i);
                break;
            }
        }

        PlaceConfig finalOurPlace = ourPlace;
        CompletableFuture<Void> globalPlaces = CompletableFuture.runAsync(() -> {
            try {
                findPlaces(finalOurPlace);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).thenRun(() -> {
            try {
                findPlaceInfo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        CompletableFuture<Void> weatherPlaces = CompletableFuture.runAsync(() -> {
            try {
                findWeather(finalOurPlace);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        globalPlaces.get();
        weatherPlaces.get();
        parseMenuController.drawResult(resultController);
    }


    public void findPlaces(PlaceConfig place) throws Exception {
        URL url = new URL("https://api.opentripmap.com/0.1/" + place.getLang() + "/places/radius?radius=2000&lon=" + place.getLongitude() + "&lat=" + place.getLatitude() + "&apikey=5ae2e3f221c38a28845f05b6b8936a18cad9844abd36348fd3b80a42");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        int responsecode = conn.getResponseCode();
        if (responsecode != 200) {
            throw new RuntimeException("HttpResponseCode: " + responsecode);
        } else {
            String inline = "";
            Scanner scanner = new Scanner(url.openStream());
            while (scanner.hasNext()) {
                inline += scanner.nextLine();
            }
            scanner.close();

            JSONParser parser = new JSONParser();
            JSONObject data_obj = (JSONObject) parser.parse(inline);
            JSONArray arr = (JSONArray) data_obj.get("features");


            ObjectMapper mapper = new ObjectMapper();
            String JSONString;
            PlaceConfig localPlace;
            for (int i = 0; i < arr.size(); i++) {
                JSONObject new_obj = (JSONObject) arr.get(i);
                JSONObject new_obj1 = (JSONObject) new_obj.get("properties");
                JSONString = "{\"xID\":\"" + new_obj1.get("xid") + "\"" + ",\"localName\":\"" + new_obj1.get("name").toString().replaceAll("\"", "\\\\\"") + "\"}";
                localPlace = mapper.readValue(JSONString, PlaceConfig.class);
                localPlace.setCityName(place.getPlaceName());
                localPlace.setLang(place.getLang());
                localPlace.setPlaceName(place.getPlaceName());
                localPlace.setRegion(place.getRegion());
                localPlace.setState(place.getState());
                localPlaces.add(localPlace);
            }
        }
    }

    public void findWeather(PlaceConfig place) throws Exception {
        URL url = new URL("https://api.openweathermap.org/data/2.5/find?lat=" + place.getLatitude() + "&lon=" + place.getLongitude() + "&cnt=5&appid=05f5349dba75b50a35b84db5984d8801");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        int responsecode = conn.getResponseCode();
        if (responsecode != 200) {
            throw new RuntimeException("HttpResponseCode: " + responsecode);
        } else {
            String inline = "";
            Scanner scanner = new Scanner(url.openStream());
            while (scanner.hasNext()) {
                inline += scanner.nextLine();
            }
            scanner.close();

            JSONParser parser = new JSONParser();
            JSONObject data_obj = (JSONObject) parser.parse(inline);


            JSONArray arr = (JSONArray) data_obj.get("list");

            JSONObject new_obj = (JSONObject) arr.get(0);
            JSONObject new_obj1 = (JSONObject) new_obj.get("main");

            JSONArray arr1 = (JSONArray) new_obj.get("weather");
            JSONObject new_obj2 = (JSONObject) arr1.get(0);

            ObjectMapper mapper = new ObjectMapper();

            String JSONString = "{\"weather\":\"" + new_obj2.get("description") + "\"" + ",\"temp\":" + new_obj1.get("temp") + "}";
            weatherInPlace = mapper.readValue(JSONString, PlaceConfig.class);
            weatherInPlace.setTemp(Math.round((weatherInPlace.getTemp() - 273.0) * 10) / 10.0);
        }
    }


    public void findPlaceInfo() throws ExecutionException, InterruptedException {


        for (int i = 0; i < localPlaces.size(); i++) {
            PlaceConfig placeConfig = localPlaces.get(i);
            findingInfo(placeConfig);
        }
        flag = 1;
    }


    private void findingInfo(PlaceConfig placeConfig) throws ExecutionException, InterruptedException {
        CompletableFuture<Void> findInfo = CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL("https://api.opentripmap.com/0.1/" + placeConfig.getLang() + "/places/xid/" + placeConfig.getxID() + "?apikey=5ae2e3f221c38a28845f05b6b8936a18cad9844abd36348fd3b80a42");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();
                int responsecode = conn.getResponseCode();
                if (responsecode != 200) {
                    throw new RuntimeException("HttpResponseCode: " + responsecode);
                } else {
                    String inline = "";
                    Scanner scanner = new Scanner(url.openStream());
                    while (scanner.hasNext()) {
                        inline += scanner.nextLine();
                    }
                    scanner.close();


                    JSONParser parser = new JSONParser();
                    JSONObject data_obj = (JSONObject) parser.parse(inline);

                    ObjectMapper mapper = new ObjectMapper();
                    String JSONString;
                    PlaceConfig placesInfo;
                    JSONString = "{\"kinds\":\"" + data_obj.get("kinds") + "\"}";
                    placesInfo = mapper.readValue(JSONString, PlaceConfig.class);
                    placesInfo.setLocalName(placeConfig.getLocalName());
                    placesInfo.setState(placeConfig.getState());
                    placesInfo.setCityName(placeConfig.getCityName());
                    placesInfo.setRegion(placeConfig.getRegion());
                    placesInfo.setState(placeConfig.getState());
                    placesInfos.add(placesInfo);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        findInfo.get();
    }

}
