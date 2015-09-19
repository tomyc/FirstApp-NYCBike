package net.cieplak.firstapp_nycitybike;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity {

    ArrayList<Station> stationArrayList;
    StationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stationArrayList = new ArrayList<Station>();
        new JSONAsyncTask().execute("http://www.citibikenyc.com/stations/json");
        ListView listView = (ListView)findViewById(R.id.list);
        adapter = new StationAdapter(getApplicationContext(),R.layout.wiersz,stationArrayList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(),"Ostatnia aktualizacja: "+stationArrayList.get(position).getLastCommunicationTime(),Toast.LENGTH_LONG).show();
            }
        });

    }

    class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Trwa ładowanie danych");
            dialog.setTitle("Nawiązywanie połączenia");
            dialog.show();
            dialog.setCancelable(false);
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            try {
                HttpGet httppost = new HttpGet(urls[0]);
                HttpClient httpClient = new DefaultHttpClient();

                HttpResponse response = httpClient.execute(httppost);

                int status = response.getStatusLine().getStatusCode();
                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    JSONObject jasonObj = new JSONObject(data);
                    JSONArray jsonArray = jasonObj.getJSONArray("stationBeanList");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        Station station = new Station();

                        station.setStationName(object.getString("stationName"));
                        station.setAvailableDocks(object.getString("availableDocks"));
                        station.setAvailableBikes(object.getString("availableBikes"));
                        station.setStatusValue(object.getString("statusValue"));
                        station.setLastCommunicationTime(object.getString("lastCommunicationTime"));
                        stationArrayList.add(station);
                    }
                    return true;
                }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return false;
        }


        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            dialog.cancel();
            adapter.notifyDataSetChanged();
            if (aBoolean==false){
                Toast.makeText(getApplicationContext(),"Nie można pobrać danych",Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
