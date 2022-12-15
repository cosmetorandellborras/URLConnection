package com.example.internetconnection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.internetconnection.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        binding.downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myClickHandler(view);
            }
        });

    }
    public void myClickHandler(View view){
        String stringURL = binding.URLEditText.getText().toString();
        ConnectivityManager con = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = con.getActiveNetworkInfo();
        if(nInfo !=null && nInfo.isConnected()){
            new DownloadWebpageText().execute(stringURL);
        }else {
            binding.textView.setText("No network connection available");
        }
    }

    private class DownloadWebpageText extends AsyncTask <String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try{
                return downloadUrl(urls[0]);
            } catch (IOException e){
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        @Override
        protected void onPostExecute(String result) {
            binding.textView.setText(result);
        }

    }
    private String downloadUrl(String myURL) throws IOException{
        InputStream is = null;
        int len = 500;

        try{
            URL url = new URL(myURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.connect();
            //int response = conn.getResponseCode();
            is = conn.getInputStream();

            //String contentAsString = readIt(is,len);
            String contentAsString = reader(is);
            return contentAsString;


        }finally {
            if(is != null){
                is.close();
            }
        }
    }
    public String readIt(InputStream stream,int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream,"UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }
    public String reader(InputStream stream) throws IOException {
        String tag = "";
        String textReturn = "";
        String tagToFind = "<div id="+(char)34+"articleContent"+(char)34+">";
        boolean find = false;
        boolean write = false;
        int ch = stream.read();
        while(ch!=-1 && !find){
            if (write){
                if(Character.toString((char)ch).equals("/")){
                    find = true;
                }
                else{
                    textReturn = textReturn + Character.toString((char)ch);
                }
            }
            else if(Character.toString((char)ch).equals("<")){
                tag = tag + Character.toString((char)ch);
            }
            else if (Character.toString((char)ch).equals(">")){
                tag = tag + Character.toString((char)ch);
                if(tag.equals(tagToFind)){
                    write = true;
                }
                tag = "";
            }
            else if(tag.length()!=0){
                tag = tag + Character.toString((char)ch);
            }
            ch = stream.read();
        }
        textReturn = textReturn.trim();
        textReturn = textReturn.substring(3,textReturn.length()-1);

        return textReturn;
    }

}