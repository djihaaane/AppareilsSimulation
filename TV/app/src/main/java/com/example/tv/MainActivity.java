package com.example.tv;

import androidx.annotation.MainThread;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.tv.api.GetDataService;
import com.example.tv.api.Result;
import com.example.tv.api.RetrofitClientInstance;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    ServerSocket serverSocket;
     Handler handler;
    static final int RESULT_ENABLE = 1 ;
    ComponentName compName ;

    Thread Thread1;
    public static String SERVER_IP = "";
    public static final int SERVER_PORT = 8080;
    String info;
     String id="0";
    int anInt = 1;


    static   ArrayList<Chaine> chaines = new ArrayList();
     int pos=0;
    VideoView videoView;

    public void setVideoView(VideoView videoView) {
        this.videoView = videoView;
    }

    Uri uri;
    int sound=0;
    AudioManager audioManager;
    private Bundle saved;
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        saved=savedInstanceState;
        setContentView(R.layout.activity_main);
        sharedpreferences = getApplicationContext().getSharedPreferences("ID", 0); // 0 - for private mode

        audioManager = (AudioManager) getApplicationContext().getSystemService(getApplicationContext().AUDIO_SERVICE);
        videoView = (VideoView) findViewById(R.id.tv);
        chaines.add(new Chaine(R.raw.friends, "NBC", 0));
        chaines.add(new Chaine(R.raw.domotique, "Arte", 1));
        chaines.add(new Chaine(R.raw.riyad, "Bein Sports", 2));
        chaines.add(new Chaine(R.raw.covid, "TF1", 3));
       playVideo(0,videoView);
        Thread1 = new Thread(new Thread1());
        Thread1.start();

    }

    public void playVideo(int posp,VideoView videoView) {
        runOnUiThread(new Runnable(){
            @Override
            public void run() {
                System.out.println("sdcnh;");
                uri = Uri.parse("android.resource://" + getPackageName() + "/" + chaines.get(posp).getId_Chaine());
                videoView.setVideoURI(uri);
                videoView.requestFocus();
                videoView.start();

            }

    });
    }

    private void fetchData(int Appareil_ID,int Appareil_Mode,String Appareil_Desc) {
        GetDataService api = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
        String id=String.valueOf(Appareil_ID).trim();
        String mod=String.valueOf(Appareil_Mode).trim();
        Call<Result> call = api.sendAppareilMode(mod,id);
        Call<Result> call1 = api.sendHistoriqueAppareil(id,mod,Appareil_Desc.trim());

        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                System.out.println(response.body().getMessage());

            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {

                Toast.makeText(getApplicationContext(), ""+t.getMessage().toString(), Toast.LENGTH_SHORT).show();
                System.out.println(t.getMessage());

            }
        });
        call1.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                System.out.println(response.body().getMessage());

            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {

                Toast.makeText(getApplicationContext(), ""+t.getMessage().toString(), Toast.LENGTH_SHORT).show();
                System.out.println(t.getMessage()+"0");

            }
        });
    }
    private PrintWriter output;
    private BufferedReader input;
    class Thread1 implements Runnable {
        @SuppressLint("InvalidWakeLockTag")
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            Socket socket;
            String action;
            VideoView videoView1;
            videoView1=findViewById(R.id.tv);
            Looper.prepare();
            try {
                serverSocket = new ServerSocket(SERVER_PORT);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
                while (true) {

                    try {
                        socket = serverSocket.accept();
                        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                        InputStream input = socket.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                        action = reader.readLine();  // reads a single character
                        System.out.println(action);
                        if (action.equals("ok"))
                        {
                            info = reader.readLine();
                            System.out.println(info);
                            setVideoView(videoView1);
                            playVideo(Integer.parseInt(info), videoView1);

                        }

                        if (action.equals("next"))
                        {
                            info = reader.readLine();
                            System.out.println(info);
                            setVideoView(videoView1);
                            playVideo(Integer.parseInt(info), videoView1);

                        }

                        if (action.equals("prev"))
                        {
                            info = reader.readLine();
                            System.out.println(info);
                            setVideoView(videoView1);
                            playVideo(Integer.parseInt(info), videoView1);

                        }

                        if (action.equals("1"))
                        {
                            id = reader.readLine();

                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putInt("id", Integer.parseInt(id));
                            editor.apply();
                            Intent mStartActivity = new Intent(getApplicationContext(), MainActivity.class);
                            int mPendingIntentId = 123456;
                            PendingIntent mPendingIntent = PendingIntent.getActivity(MainActivity.this, mPendingIntentId, mStartActivity,
                                    PendingIntent.FLAG_CANCEL_CURRENT);
                            startActivity(mStartActivity);
                            Process.killProcess(Process.myPid());

                        }

                        if (action.equals("0"))
                        {
                            id = reader.readLine();
                            System.out.println(id);

                            if (!id.equals("0"))
                            {
                                if (info==null)
                                {
                                    fetchData(Integer.parseInt(id),0,chaines.get(0).getNom_Chaine());

                                }else {
                                    fetchData(Integer.parseInt(id), 0, chaines.get(Integer.parseInt(info)).getNom_Chaine());
                                }
                            }
                                   finishAffinity();

                        }



                        if (action.equals("mute"))
                        {
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_PLAY_SOUND);   //here you can set custom volume.
                        }

                        if (action.equals("volup"))
                        {

                            int i= audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)+2;
                            System.out.println(i);
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,i, AudioManager.FLAG_PLAY_SOUND);   //here you can set custom volume.

                        }

                        if (action.equals("voldown"))
                        {
                            int i= audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)-2;
                            System.out.println(i);
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,i, AudioManager.FLAG_PLAY_SOUND);   //here you can set custom volume.

                        }



                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }} catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onBackPressed()
    {  System.out.println("onbaaaaaaaaackpressed");
        if (sharedpreferences.getInt("id",0)!=0)
        {
            System.out.println(sharedpreferences.getInt("id",0));
            if (info==null)
            {
                fetchData(sharedpreferences.getInt("id",0),0,chaines.get(0).getNom_Chaine());

            }else {
                fetchData(sharedpreferences.getInt("id",0), 0, chaines.get(Integer.parseInt(info)).getNom_Chaine());
            }
        }
        super.onBackPressed();

    }


    public  void onResume() {
        if (sharedpreferences.getInt("id",0)!=0)
        {
            System.out.println(sharedpreferences.getInt("id",0));
        if (info==null)
        {
            fetchData(sharedpreferences.getInt("id",0),1,chaines.get(0).getNom_Chaine());

        }else {
            fetchData(sharedpreferences.getInt("id",0), 1, chaines.get(Integer.parseInt(info)).getNom_Chaine());
        }
        }
        super.onResume();
    }

}