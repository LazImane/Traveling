package com.example.traveling;

import static android.provider.Settings.System.getString;

import android.app.Activity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MVPMapSearch {

    public static String cultureQuery(int rad, double lat, double lon) {
        return "[out:json];" +
                "(" +
                "node[\"tourism\"=\"museum\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                "way[\"tourism\"=\"museum\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                "relation[\"tourism\"=\"museum\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                "node[\"tourism\"=\"gallery\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                "way[\"tourism\"=\"gallery\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                "node[\"historic\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                "way[\"historic\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                "relation[\"historic\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                "node[\"tourism\"=\"attraction\"][\"name\"][\"amenity\"!=\"\"](around:" + rad + "," + lat + "," + lon + ");" +
                "way[\"tourism\"=\"attraction\"][\"name\"][\"amenity\"!=\"\"](around:" + rad + "," + lat + "," + lon + ");" +
                "relation[\"tourism\"=\"attraction\"][\"name\"][\"amenity\"!=\"\"](around:" + rad + "," + lat + "," + lon + ");" +
                "node[\"amenity\"=\"arts_centre\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                "way[\"amenity\"=\"arts_centre\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                "relation[\"amenity\"=\"arts_centre\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                ");" +
                "out center 30;";
    }

    public static String discoveryQuery(int rad, double lat, double lon) {
        return "[out:json];" +
                "(" +
                "node[\"leisure\"=\"park\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                "way[\"leisure\"=\"park\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                "relation[\"leisure\"=\"park\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                "node[\"natural\"=\"wood\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                "way[\"natural\"=\"wood\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                "relation[\"natural\"=\"wood\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                "node[\"natural\"=\"water\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                "way[\"natural\"=\"water\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                "relation[\"natural\"=\"water\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                "node[\"natural\"=\"beach\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                "node[\"tourism\"=\"viewpoint\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                "node[\"leisure\"=\"nature_reserve\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                "way[\"leisure\"=\"nature_reserve\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                "relation[\"leisure\"=\"nature_reserve\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                ");";
    }

    public static String activityQuery(int rad, double lat, double lon){
            return"[out:json];" +
                    "("+
                    "node[\"tourism\"=\"amusement_park\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                    "way[\"tourism\"=\"amusement_park\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                    "relation[\"tourism\"=\"amusement_park\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                    "node[\"leisure\"=\"bowling_alley\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                    "way[\"leisure\"=\"bowling_alley\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                    "node[\"leisure\"=\"amusement_arcade\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                    "node[\"leisure\"=\"escape_game\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                    ");"+
                    "out center 30;";
    }

    public static String restaurantQuery(int rad, double lat, double lon){
        return "[out:json];" +
                        "(" +
                        "node[\"amenity\"=\"restaurant\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                        "way[\"amenity\"=\"restaurant\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                        "relation[\"amenity\"=\"restaurant\"][\"name\"](around:" + rad + "," + lat + "," + lon + ");" +
                        ");" +
                        "out center 30;";
    }



    public static void getCoordinates(Activity context, String location_name) {

        new Thread(() -> {
            System.out.println(MVPMapSearch.getCoordinatesThread(context, location_name));

        }).start();
    }

    public static List<Double> getCoordinatesThread(Activity context, String placeName) {
        List<Double> coords = new ArrayList<>();

        try {
            String encodedPlace = URLEncoder.encode(placeName, "UTF-8");

            String urlString = "https://nominatim.openstreetmap.org/search?q="
                    + encodedPlace
                    + "&format=json&limit=1";

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent",
                    "TravelingApp/1.0 (android student project - contact: "
                            + context.getString(R.string.my_mail) + ")"); // required by Nominatim
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                return coords; // empty list if request failed
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            conn.disconnect();

            JSONArray jsonArray = new JSONArray(response.toString());

            if (jsonArray.length() > 0) {
                JSONObject first = jsonArray.getJSONObject(0);

                double lat = Double.parseDouble(first.getString("lat"));
                double lon = Double.parseDouble(first.getString("lon"));

                coords.add(lat);
                coords.add(lon);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return coords;
    }

    public static void test(Activity context, int queryType) {
        String query;
        // Paris coordinates
        double lat = 48.8566;
        double lon = 2.3522;
        int radius = 1000; // 1km

        if(queryType == 0) query = restaurantQuery(radius, lat, lon);
        else if(queryType == 1) query = cultureQuery(radius, lat, lon);
        else if(queryType == 2) query = discoveryQuery(radius, lat, lon);
        else if(queryType == 3) query = activityQuery(radius, lat, lon);
        else query = "";

        new Thread(() -> {
            String results = MVPMapSearch.searchPlaces(context, query);
            System.out.println(results);

        }).start();
    }
    private static String searchPlaces(Activity context, String query) {
        StringBuilder result = new StringBuilder();

        try {

            String encodedQuery = URLEncoder.encode(query, "UTF-8");

            String urlString = "https://overpass-api.de/api/interpreter?data=" + encodedQuery;

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");

            conn.setRequestProperty(
                    "User-Agent",
                    "TravelingApp/1.0 (android student project - contact: "
                            + context.getString(R.string.my_mail) + ")"
            );

            int responseCode = conn.getResponseCode();
            System.out.println("HTTP CODE: " + responseCode);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();

            JSONObject root = new JSONObject(response.toString());
            JSONArray elements = root.getJSONArray("elements");

            for (int i = 0; i < elements.length(); i++) {

                JSONObject place = elements.getJSONObject(i);
                JSONObject tags = place.optJSONObject("tags");

                if (tags == null) continue;

                String name = tags.optString("name", "Unnamed");

                double pLat;
                double pLon;

                // Nodes have direct lat/lon, ways/relations use "center"
                if (place.has("lat") && place.has("lon")) {
                    pLat = place.getDouble("lat");
                    pLon = place.getDouble("lon");
                } else {
                    JSONObject center = place.getJSONObject("center");
                    pLat = center.getDouble("lat");
                    pLon = center.getDouble("lon");
                }

                result.append("Name: ")
                        .append(name)
                        .append("\nLatitude: ")
                        .append(pLat)
                        .append("\nLongitude: ")
                        .append(pLon)
                        .append("\n\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result.toString();
    }
}