package com.example.Porte;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.Porte.api.GetDataService;
import com.example.Porte.api.Result;
import com.example.Porte.api.RetrofitClientInstance;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScreenReceiver extends BroadcastReceiver {
    int id;

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences sharedpreferences = context.getSharedPreferences("ScreenState", 0);
        id = sharedpreferences.getInt("id", 0);
        // 0 - for private mode
        if (id != 0) {
            if (Objects.requireNonNull(intent.getAction()).equals(Intent.ACTION_SCREEN_OFF)) {
                // DO WHATEVER YOU NEED TO DO HERE
                fetchData(id, 0, "");
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                // AND DO WHATEVER YOU NEED TO DO HERE
                fetchData(id, 1, "");
            }
            Intent i = new Intent(context, MainActivity.class);
            context.startService(i);
        }
    }
      public void fetchData(int Appareil_ID,int Appareil_Mode,String Appareil_Desc) {
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

                System.out.println(t.getMessage());

            }
        });
    }


}
