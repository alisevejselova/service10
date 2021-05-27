package com.example.service10;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.example.service10.utilities.Network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class GetJobs extends AsyncTask<Void, Void, String > {

    private static final String TAG = "Jobs";
    public static final String MyPREFERENCES = "MyPrefs" ;
    SharedPreferences sharedPreferences;
    private static String job ;
    Context context;
    public GetJobs(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        job = Network.getBackendInfo();
        startJob(job);
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        sharedPreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
    }

    private void startJob(String job){
                try {
            JSONArray jsonArray = new JSONArray(job); // job -> JSONArray

            for (int i=0; i<jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);  //sekoj definiran job e eden object

//                Log.d(TAG, " Jobs definition :");
//                Log.d(TAG,"date = "+jsonObject.getString("date"));
//                Log.d(TAG,"host = "+jsonObject.getString("host"));
//                Log.d(TAG,"count = "+jsonObject.getString("count"));
//                Log.d(TAG,"packetSize = "+jsonObject.getString("packetSize"));
//                Log.d(TAG,"jobPeriod = "+jsonObject.getString("jobPeriod"));
//                Log.d(TAG,"type = "+jsonObject.getString("jobType"));


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
              //za testiranje
//                try {
//                    Thread.sleep(20000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                //post to backend
                PosttoBackend(pingResult);


            }

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

    }


private void PosttoBackend(StringBuilder pingResult) {
    try {
        URL url = new URL("http://10.0.2.2:5000/postresults");
       // URL url = new URL("http://192.168.1.4:5000/postresults");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", " */* ");
        connection.setDoOutput(true);

        String jsonInputString = " {\"result\": \"" + pingResult + " \"} ";
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        Log.i("PosttoBackend", "Result: " + jsonInputString);
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            Log.i("PosttoBackend", " POST response  " + connection.getResponseCode() + " " + connection.getResponseMessage());


            //ako ne postoi konekcija neispratenite 3  rezultati kje se zacuvaaat vo sharedPreferences
            if (connection.getResponseCode() != 200) {
                SharedPreferences.Editor editor = sharedPreferences.edit();

                int counter = sharedPreferences.getInt("counter", 0);

                if (counter == 0) {
                    editor.putInt("counter", 1);
                    editor.putString("result1", pingResult.toString());
                    editor.apply();
                } else {
                    editor.putString("result" + counter, pingResult.toString());
                    if (counter == 3) {
                        counter = 1;
                    } else {
                        counter++;
                    }
                    editor.putInt("counter", counter);
                    editor.apply();
                }
                Log.i("PosttoBackend", " Rezultatot e zacuvan ");
                //postoi konekcija
            } else {
                int counter = sharedPreferences.getInt("counter", 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                //ako ima zacuvani rezultati
                if (counter != 0) {
                    StringBuilder tasks_to_send = new StringBuilder();
                    tasks_to_send.append("{\"result\": \"");
                    for (int i = 1; i <= counter; i++) {
                        String task = sharedPreferences.getString("result" + counter, "nema");
                        if (!task.equals("nema")) {
                            Log.i("PosttoBackend", " Result " + counter + "od SsharedPreferences " + task);
                            tasks_to_send.append(task).append(";");

                            editor.remove("result" + counter);
                            editor.apply();
                        }
                    }
                    editor.putInt("counter", 0);
                    editor.apply();

                    // izbrisi ; dodaden na krajot na posledniot rezultat
                    if (tasks_to_send.length() > 0)
                        tasks_to_send.deleteCharAt(tasks_to_send.length() - 1);


                    Log.i("PosttoBackend", "Site neisprateni rezultati  " + tasks_to_send.toString());

                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = jsonInputString.getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }
                    Log.i("PosttoBackend", "Result: " + jsonInputString);
                    StringBuilder unsentResponse = new StringBuilder();
                    try (BufferedReader br1 = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                        String responseLine1 = null;
                        while ((responseLine1 = br1.readLine()) != null) {
                            unsentResponse.append(responseLine1.trim());
                        }
                        Log.i("PosttoBackend", "POST response : " + connection.getResponseCode() + " " + connection.getResponseMessage());


                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
    } catch (ProtocolException e) {
        e.printStackTrace();
    } catch (MalformedURLException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }

}
}
