package com.namhto.weather;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Weather {

    public String json;
    private String city;
    private String country;
    public List<InstantWeather> ls = new ArrayList();

    public Weather(String json) {
        this.json = json;
    }

    public void parse() throws FileNotFoundException, IOException, ParseException {

        JSONParser parser = new JSONParser();
        JSONObject jsonobject = (JSONObject) parser.parse(this.json);

        JSONObject city = (JSONObject) jsonobject.get("city");
        this.city = (String) city.get("name");
        this.country = (String) city.get("country");

        JSONArray list = (JSONArray) jsonobject.get("list");

        Iterator<JSONObject> it = list.iterator();

        while(it.hasNext()) {

                JSONObject o = it.next();

                JSONObject main = (JSONObject) o.get("main");
                int temp = (int) (((Number) main.get("temp")).intValue() - 273.15);  //Conversion kelvin celcius

                JSONArray weather = (JSONArray) o.get("weather");
                String sky = (String) ((JSONObject) weather.get(0)).get("main");

                Long time = (Long) o.get("dt");
                Timestamp ts = new Timestamp(time*1000);

                ls.add(new InstantWeather(temp, sky, ts));
        }
    }

    public String getCity() {
            return city;
    }

    public void setCity(String city) {
            this.city = city;
    }

    public String getCountry() {
            return country;
    }

    public void setCountry(String country) {
            this.country = country;
    }

}
