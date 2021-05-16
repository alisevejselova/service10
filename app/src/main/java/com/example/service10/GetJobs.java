package com.example.service10;

import android.os.AsyncTask;
import android.util.Log;

import com.example.service10.utilities.Network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class GetJobs extends AsyncTask<Void, Void, String > {

    private static final String TAG = "Jobs";
    public GetJobs(){}


    @Override
    protected String doInBackground(Void... voids) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Network.getBackendInfo();
    }

    // se povikuva posle doInBackgroud ,  i go prima rezultatot sto go vraka doInBackground
    @Override
    protected void onPostExecute(String s){
        super.onPostExecute(s);

        try {
            JSONArray jsonArray = new JSONArray(s); // s -> JSONArray

            for (int i=0; i<jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);  //sekoj definiran job e eden object

                Log.d(TAG, " Jobs definition :");
                Log.d(TAG,"date = "+jsonObject.getString("date"));
                Log.d(TAG,"host = "+jsonObject.getString("host"));
                Log.d(TAG,"count = "+jsonObject.getString("count"));
                Log.d(TAG,"packetSize = "+jsonObject.getString("packetSize"));
                Log.d(TAG,"jobPeriod = "+jsonObject.getString("jobPeriod"));
                Log.d(TAG,"type = "+jsonObject.getString("jobType"));


                String pingCmd = "ping  -c  " + jsonObject.getString("count") + " -s " +jsonObject.getString("packetSize") +" " + jsonObject.getString("host");
                StringBuilder pingResult = new StringBuilder();

                Runtime r = Runtime.getRuntime();
                Process p = r.exec(pingCmd);
                BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    pingResult.append(inputLine);
                }
                in.close();
                Log.d(TAG,"Job result :  " + pingResult);


            }

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }


    }
}
