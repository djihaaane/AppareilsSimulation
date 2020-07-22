package com.example.lampe;

import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.CameraProfile;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lampe.api.GetDataService;
import com.example.lampe.api.Result;
import com.example.lampe.api.RetrofitClientInstance;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Executable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    ServerSocket serverSocket;

    static final int RESULT_ENABLE = 1 ;
    ComponentName compName ;

    Thread Thread1 = null;
    TextView tvIP, tvPort;
    TextView tvMessages;
    public static String SERVER_IP = "";
    public static final int SERVER_PORT = 8080;
    CameraManager camManager;
    String cameraId = null;
    IntentFilter filter;
    String id;
    CameraManager.TorchCallback torchCallback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvIP = findViewById(R.id.tvIP);
        tvPort = findViewById(R.id.tvPort);
        tvMessages = findViewById(R.id.tvConnectionStatus);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

            torchCallback = new CameraManager.TorchCallback() {
                @Override
                public void onTorchModeUnavailable(String cameraId) {
                    super.onTorchModeUnavailable(cameraId);
                }

                @Override
                public void onTorchModeChanged(String cameraId, boolean enabled) {
                    super.onTorchModeChanged(cameraId, enabled);
                    if (id!=null)
                    if (enabled)
                    {
                     fetchData(Integer.parseInt(id),1,"");
                    }
                    else
                    {
                        fetchData(Integer.parseInt(id),0,"");
                    }
                }
            };
            camManager = (CameraManager) getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
            assert camManager != null;
            camManager.registerTorchCallback(torchCallback, null);

        }

        try {
            SERVER_IP = getLocalIpAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

         filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);


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
                        etat = reader.readLine();  // reads a single character
                        System.out.println(etat);
                        if ( etat != null) {
                            id=reader.readLine();
                            System.out.println(id);
                        }


                        if (etat.equals("1")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                                        try {
                                            cameraId = camManager.getCameraIdList()[0];
                                            camManager.setTorchMode(cameraId, true);
                                            //Turn ON
                                        } catch (CameraAccessException e) {
                                            e.printStackTrace();
                                        }
                                    }}
                            });


                        }

                        if (etat.equals("0")){
                            runOnUiThread(new Runnable() {
                                @SuppressLint("ServiceCast")
                                @Override
                                public void run() {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                                        String cameraId = null;
                                        try {
                                            cameraId = camManager.getCameraIdList()[0];
                                            camManager.setTorchMode(cameraId, false);   //Turn OFF
                                        } catch (CameraAccessException e) {
                                            e.printStackTrace();
                                        }}
                                }
                            });

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }} catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                System.out.println(t.getMessage());

            }
        });
    }
}