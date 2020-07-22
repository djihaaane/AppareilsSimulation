package com.example.Porte;

import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.Porte.api.GetDataService;
import com.example.Porte.api.Result;
import com.example.Porte.api.RetrofitClientInstance;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {
    ServerSocket serverSocket;
    private SharedPreferences sharedpreferences;
   public static String id = null;
    DevicePolicyManager deviceManger ;
    ComponentName compName ;
    Thread Thread1 = null;
    TextView tvIP, tvPort;
    TextView tvMessages;
    EditText etMessage;
    Button btnSend;
    public static String SERVER_IP = "";
    public static final int SERVER_PORT = 8080;
    IntentFilter filter;
    ScreenReceiver mReceiver;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        sharedpreferences = getApplicationContext().getSharedPreferences("ScreenState", 0); // 0 - for private mode
        tvIP = findViewById(R.id.tvIP);
        tvPort = findViewById(R.id.tvPort);
        tvMessages = findViewById(R.id.tvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);


        filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
         mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);

        try {
            SERVER_IP = getLocalIpAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                fetchData();
                System.out.println("it worksssss");
            }
        });

        Thread1 = new Thread(new Thread1());
        Thread1.start();
    }
    private String getLocalIpAddress() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress();
    }
    private PrintWriter output;
    private BufferedReader input;
    class Thread1 implements Runnable {
        @SuppressLint("InvalidWakeLockTag")
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            Socket socket;
            String etat;
            String type = "";
            try {
                serverSocket = new ServerSocket(SERVER_PORT);

                    runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        tvMessages.setText("Not connected");
                        tvIP.setText("IP: " + SERVER_IP);
                        tvPort.setText("Port: " + String.valueOf(SERVER_PORT));
                    }
                });
                while (true) {

                    try {
                    socket = serverSocket.accept();
                    DataOutputStream   os = new DataOutputStream(socket.getOutputStream());
                    InputStream input = socket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                        // reads a single character
                          etat = reader.readLine();
                         System.out.println(etat);
                        if ( etat != null) {
                         type=reader.readLine();
                            System.out.println(type);
                        }

                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putInt("id", Integer.parseInt(type));
                        editor.putInt("etat", Integer.parseInt(etat));
                        editor.apply();
                        System.out.println(sharedpreferences.getInt("id",0));
                                 if (etat.equals("1")) {
                                     runOnUiThread(new Runnable() {
                                         @Override
                                         public void run() {
                                             PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
                                             assert pm != null;
                                             PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                                                     | PowerManager.ACQUIRE_CAUSES_WAKEUP
                                                     | PowerManager.ON_AFTER_RELEASE, "INFO");
                                             wl.acquire(10*60*1000L /*10 minutes*/);


                                         }
                                     });


                                 }

                                 if (etat.equals("0")){
                                     runOnUiThread(new Runnable() {
                                         @SuppressLint("ServiceCast")
                                         @Override
                                         public void run() {

                                             deviceManger = (DevicePolicyManager)
                                                     getSystemService(Context.DEVICE_POLICY_SERVICE);
                                             compName = new ComponentName(getApplicationContext(), DeviceAdmin.class);
                                             deviceManger.lockNow();
                                         }
                                     });

                                 }

                    }

                    catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
            catch (IOException e) {
                e.printStackTrace();
            }
        }}
    private void fetchData() {
        GetDataService api = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
        Call<Result> call = api.alarme();

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
    }



    @Override
    public void onDestroy() {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("id",0);
        editor.apply();
        try{
            if(mReceiver!=null)
                unregisterReceiver(mReceiver);

        }catch(Exception e){}
        super.onDestroy();

    }
    @Override
    public void onResume() {
        super.onResume();
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {

        startService(new Intent(getApplicationContext(),ShakeService.class));
        // Add the following line to unregister the Sensor Manager onPause
       // mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }
}