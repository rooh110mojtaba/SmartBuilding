package ir.rooh110mojtaba.smartbuilding;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import model.roomData;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String urlString;
    private String urlParameters;
    private Float fontSize;

    private ProgressDialog progress;

    private ListView dataLV;
    private ArrayList<roomData> allRoomData;

    private boolean debug = true;
    private boolean isConnected = false;
    private boolean continue_or_stop = false;

    private Handler mHandler;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        url_init();

        new requestData(MainActivity.this).execute();

        continue_or_stop = true;

        mHandler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (continue_or_stop) {
                    try {
                        if(isConnected) {
                            Thread.sleep(1000); // every 1 seconds
                        } else {
                            Thread.sleep(10000); // every 10 seconds
                        }
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                url_refresh();
                                new requestData(getApplicationContext()).execute();
                            }
                        });
                    } catch (Exception e) {
                        if(debug)   {System.out.println("Error");}
                    }
                }
            }
        }).start();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void url_refresh(){
        //urlString = "http://192.168.1.2/mypost/mypost.php";
        urlString = sharedPreferences.getString("url", "http://192.168.1.100:8000/sensors/load_realtime/");
        urlParameters = sharedPreferences.getString("url_parameters", "");
    }

    public void url_init(){
        //urlString = "http://192.168.1.2/mypost/mypost.php";
        urlString = sharedPreferences.getString("url", "http://192.168.1.100:8000/sensors/load_realtime/");
        urlParameters = sharedPreferences.getString("url_parameters", "");
    }

    // request data from server
    // by posting some variables to a php file
    // and receiving data from php file
    // and convert it to json object
    // and use data
    private class requestData extends AsyncTask<String, Void, JSONObject> {

        private final Context context;

        public requestData(Context c){
            this.context = c;
        }

        protected void onPreExecute(){
            progress= new ProgressDialog(context);
            progress.setMessage("Loading");
            if(!continue_or_stop) {
                progress.show();
            }
        }

        @Override
        protected JSONObject doInBackground(String... params) {

            JSONObject json = null;

            try {

                URL url = new URL(urlString);

                isConnected = false;
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();

                //System.out.println("url response code " + connection.getResponseCode());
                connection.setRequestMethod("POST");
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);
                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                isConnected = true;
                dStream.writeBytes(urlParameters);
                dStream.flush();
                dStream.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                final StringBuilder responseOutput = new StringBuilder();
                while((line = br.readLine()) != null ) {
                    responseOutput.append(line);
                }
                br.close();

                try {
                    json = new JSONObject(responseOutput.toString());
                } catch (JSONException e){
                    try {
                        json = new JSONObject("{'TMP':\"?\",'LUM':\"?\",'HUM':\"?\",'NUM':\"?\",'DST':\"?\"}");
                    } catch (JSONException e1){
                        if(debug) {System.out.println("Error3");}
                    }
                    e.printStackTrace();
                    System.out.println("Error: cant create json object from response output");
                }


            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                if(debug) {System.out.println("Error1");}
            } catch (IOException e){
                if(debug) {System.out.println("Error2");}
                try {
                    json = new JSONObject("{'TMP':\"0\",'LUM':\"0\",'HUM':\"0\",'NUM':\"0\",'DST':\"0\"}");
                } catch (JSONException e1){
                    if(debug) {System.out.println("Error3");}
                }
            }

            return json;
        }

        protected void onPostExecute(JSONObject json) {
            if(progress.isShowing()) {
                progress.dismiss();
            }

            dataLV = (ListView) findViewById(R.id.dataListView);
            setAllRoomData(json);
            DataListViewAdapter dataListViewAdapter = new DataListViewAdapter(allRoomData, getApplicationContext());
            fontSize = Float.valueOf(sharedPreferences.getString("font_size", "30"));
            dataListViewAdapter.setFontSize(fontSize);
            dataLV.setAdapter(dataListViewAdapter);

        }

    }

    public void setAllRoomData(JSONObject json){
        allRoomData = new ArrayList<>();
        roomData roomData = new roomData(getApplicationContext());
        try {
            roomData.setId(1);
            roomData.setCode("TMP");
            roomData.setName(getResources().getString(R.string.tmpString));
            roomData.setValue(Float.valueOf(json.getString("TMP")));
            roomData.setUnit(getResources().getString(R.string.tmpUnitString));
            if(roomData.getValue() >= 0 && roomData.getValue() <= 10) {
                roomData.setImgName("tmp_1");
            }else if(roomData.getValue() >= 10 && roomData.getValue() <= 20) {
                roomData.setImgName("tmp_2");
            }else if(roomData.getValue() >= 20 && roomData.getValue() <= 30) {
                roomData.setImgName("tmp_3");
            }else if(roomData.getValue() >= 30 && roomData.getValue() <= 60) {
                roomData.setImgName("tmp_4");
            }else{
                roomData.setImgName("tmp_1");
            }
        } catch (JSONException e){
            System.out.println("Error in reading tmp");
        }
        allRoomData.add(roomData);
        roomData = new roomData(getApplicationContext());
        try {
            roomData.setId(2);
            roomData.setCode("LUM");
            roomData.setName(getResources().getString(R.string.lumString));
            roomData.setValue(Float.valueOf(json.getString("LUM")));
            roomData.setUnit(getResources().getString(R.string.lumUnitString));
            if(roomData.getValue() >= 0 && roomData.getValue() <= 5) {
                roomData.setImgName("lum_" + (int) roomData.getValue());
            }else{
                roomData.setImgName("lum_0");
            }
        } catch (JSONException e){
            System.out.println("Error in reading lum");
        }
        allRoomData.add(roomData);
        roomData = new roomData(getApplicationContext());
        try {
            roomData.setId(3);
            roomData.setCode("HUM");
            roomData.setName(getResources().getString(R.string.humString));
            roomData.setValue(Float.valueOf(json.getString("HUM")));
            roomData.setUnit(getResources().getString(R.string.humUnitString));
            roomData.setImgName("hum");
        } catch (JSONException e){
            System.out.println("Error in reading hum");
        }
        allRoomData.add(roomData);
        roomData = new roomData(getApplicationContext());
        try {
            roomData.setId(4);
            roomData.setCode("NUM");
            roomData.setName(getResources().getString(R.string.numString));
            roomData.setValue(Float.valueOf(json.getString("NUM")), true);
            roomData.setUnit(getResources().getString(R.string.numUnitString));
            roomData.setImgName("num");
        } catch (JSONException e){
            System.out.println("Error in reading num");
        }
        allRoomData.add(roomData);
        roomData = new roomData(getApplicationContext());
        try {
            roomData.setId(5);
            roomData.setCode("DST");
            roomData.setName(getResources().getString(R.string.dstString));
            roomData.setValue(Float.valueOf(json.getString("DST")), true);
            roomData.setUnit("");
            if(roomData.getValue() == 0 || roomData.getValue() == 1) {
                roomData.setImgName("dst_" + (int) roomData.getValue());
            }else{
                roomData.setImgName("dst_0");
            }
        } catch (JSONException e){
            System.out.println("Error in reading dst");
        }
        allRoomData.add(roomData);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the main; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.nav_settings){
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

}
