package com.example.guessthecelebrityupdated;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    String download_html;
    ArrayList<String> CelebUrls = new ArrayList<String>();
    ArrayList<String> CelebNames = new ArrayList<String>();
    ImageView imageView;
    String[] celeb_ans=new  String[4];
    Button button;
    Button button2;
    Button button3;
    Button button4;
    int correctAnswer;
    int chosenCeleb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        DownloadTask downloadTask = new DownloadTask();
        try {
           download_html = downloadTask.execute("http://www.posh24.se/kandisar").get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] split_data = download_html.split("<div class=\"listedArticles\">");

        Pattern p = Pattern.compile("img src=\"(.*?)\"");
        Matcher m = p.matcher(split_data[0]);

        while (m.find()){
            CelebUrls.add(m.group(1));
        }

        p= Pattern.compile("alt=\"(.*?)\"");
        m=p.matcher(split_data[0]);

        while (m.find()){
            CelebNames.add(m.group(1));
        }
        newQuestion();

    }

    public void newQuestion(){
        Random random = new Random();
        chosenCeleb = random.nextInt(CelebUrls.size());
        ImageDownloader imageDownloader = new ImageDownloader();
        try {
           Bitmap myBitmap = imageDownloader.execute(CelebUrls.get(chosenCeleb)).get();
           imageView.setImageBitmap(myBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        correctAnswer = random.nextInt(4);
        for (int i=0; i<4; i++){
            if(i==correctAnswer){
                celeb_ans[i]=CelebNames.get(chosenCeleb);
            }
            else {
                int incorrec_chosen_celeb = random.nextInt(CelebUrls.size());
                while (incorrec_chosen_celeb==chosenCeleb){
                    incorrec_chosen_celeb = random.nextInt(CelebUrls.size());
                }
                celeb_ans[i]=CelebNames.get(incorrec_chosen_celeb);
            }
        }

        button.setText(celeb_ans[0]);
        button2.setText(celeb_ans[1]);
        button3.setText(celeb_ans[2]);
        button4.setText(celeb_ans[3]);



    }

    private class ImageDownloader extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();
                InputStream in = httpURLConnection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                return myBitmap;
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }

        }
    }

    private class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection httpURLConnection;
            InputStream in = null;
            try {
                url = new URL(urls[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                in = httpURLConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data!=-1){
                    char current = (char) data;
                    result+=current;
                    data = reader.read();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }


            return result;
        }
    }

    public void answerChosen(View view){
        if(view.getTag().toString().equals(Integer.toString(correctAnswer))){
            Toast.makeText(getApplicationContext(),"Correct", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getApplicationContext(), "Wrong. Correct Answer is "+CelebNames.get(chosenCeleb), Toast.LENGTH_SHORT).show();
        }
        newQuestion();
    }
}
