package com.example.rosha.myapplication;

/**
 * Created by rosha on 30/01/2018.
 */

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.util.HashMap;
import java.util.List;


public class GetDirectionsData extends AsyncTask<Object,String,String> {

    GoogleMap mMap;
    String url;
    String googleDirectionsData;
    String duration, distance;
    String response;
    LatLng latLng;
    /*public GetDirectionsData(String response){
        this.response = response;
    }*/

    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap)objects[0];
        url = (String)objects[1];
        latLng = (LatLng)objects[2];



        DownloadUrl downloadUrl = new DownloadUrl();
        try {
            googleDirectionsData = downloadUrl.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googleDirectionsData;
    }

    @Override
    protected void onPostExecute(String s) {

        String[] directionsList;
        DataParser parser = new DataParser();

        directionsList = parser.parseDirections(s);
        displayDirection(directionsList);

    }

    public void displayDirection(String[] directionsList)
    {
        int count = directionsList.length;
        for(int i = 0;i<count;i++)
        {
            int r = response.indexOf('r');
            int g = response.indexOf('g');
            int b = response.indexOf('b');
            PolylineOptions options = new PolylineOptions();
            if(i==0)
                options.color(Color.BLUE);
            else if (i==1)
                options.color(Color.GREEN);
            else
                options.color(Color.RED);
            options.width(10);

            options.addAll(PolyUtil.decode(directionsList[i]));

            mMap.addPolyline(options);
        }
    }

}
