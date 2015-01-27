package com.example.gateswitch;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;


public class MainActivity extends Activity implements OnClickListener {

    private Button offBtn, onBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onBtn = (Button) findViewById(R.id.on_btn);
        onBtn.setOnClickListener(this);
        offBtn = (Button) findViewById(R.id.off_btn);
        offBtn.setOnClickListener(this);

        //super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_main);

        showUserSettings();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.on_btn:
                callSwitchGate(true);
                break;
            case R.id.off_btn:
                callSwitchGate(false);
                break;
            default:
                break;
        }
    }


    private void callSwitchGate(final boolean open) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                switchGate(switchGateCallback, open);
            }
        });
        thread.start();
    }

    private void switchGate(SwitchGateInterface callBack, boolean open) {
        // Create a new HttpClient and Post Header
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        String user_name = sharedPrefs.getString("prefUsername", "NULL");
        String password = sharedPrefs.getString("prefUserpass", "NULL");
        String host = sharedPrefs.getString("prefHost", "NULL");
        String ActionstringOn = sharedPrefs.getString("prefActionstringOn", "NULL");
        String ActionstringOff = sharedPrefs.getString("prefActionstringOff", "NULL");
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(host);
        String authorizationString = "Basic " + Base64.encodeToString((user_name + ":" + password).getBytes(), Base64.NO_WRAP); //this line is diffe
        httppost.setHeader("Authorization", authorizationString);
        try {
            httppost.setHeader("Content-type", "application/json");
            httppost.setHeader("Accept", "application/json");

            String json = "";
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("actionString", (open ? ActionstringOn : ActionstringOff));
            json = jsonObject.toString();
            StringEntity se = new StringEntity(json);
            httppost.setEntity(se);

            HttpResponse response = httpclient.execute(httppost);
            String responseString = new BasicResponseHandler().handleResponse(response);
            boolean httpSuccess = responseString != null && responseString.length() > 0;
            if (httpSuccess) {
                callBack.onSuccess(open, responseString);
            }
        } catch (Exception e) {
            callBack.onFail(open, e.getMessage());
        }
    }

    final SwitchGateInterface switchGateCallback = new SwitchGateInterface() {

        @Override
        public void onSuccess(boolean open, String content) {
            System.out.println(content);
            showMessage("Switch " + (open ? "set to ON" : "set to OFF") + ".");
        }

        @Override
        public void onFail(boolean open, String error) {
            System.out.println(error);
            showMessage("System error! Could not " + (open ? "open" : "close") + ".");
        }

        private void showMessage(final String message) {
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    exitAfter(3000);
                }
            });
        }

        private void exitAfter(final long time) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(time);
                    } catch (Exception e) {
                    }
                    System.exit(0);
                }
            }).start();
        }

    };

    public interface SwitchGateInterface {

        public void onSuccess(boolean open, String content);

        public void onFail(boolean open, String error);
    }


    private static final int RESULT_SETTINGS = 1;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_settings:
                Intent i = new Intent(this, UserSettingsActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                break;

        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SETTINGS:
                showUserSettings();
                break;

        }

    }

    private void showUserSettings() {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        StringBuilder builder = new StringBuilder();

        builder.append("\n Username: "
                + sharedPrefs.getString("prefUsername", "NULL"));



        TextView settingsTextView = (TextView) findViewById(R.id.textUserSettings);

        settingsTextView.setText(builder.toString());
    }

}
