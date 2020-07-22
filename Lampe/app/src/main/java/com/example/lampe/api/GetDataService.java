package com.example.lampe.api;


import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GetDataService {

    @POST("RetrofitSmartDar/public/sendAppareilEtat")
    Call<Result> sendAppareilMode(
            @Query("Appareil_Mode") String Appareil_Mode,
            @Query("Appareil_ID") String Appareil_ID);

    @POST("RetrofitSmartDar/public/sendHistoriqueAppareil")
    Call<Result> sendHistoriqueAppareil(
            @Query("Historique_Appareil_ID") String Historique_Scenario_ID, @Query("Historique_Mode") String Appareil_Mode, @Query("Historique_Desc") String Historique_Desc);

}
