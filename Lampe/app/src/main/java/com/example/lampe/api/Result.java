package com.example.lampe.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Belal on 14/04/17.
 */

public class Result {

    @SerializedName("error")
    @Expose
    private String error;
    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("User")
    @Expose
    private String User;

    public String getError() {
        return error;
    }

    public Result() {
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser() {
        return User;
    }

    public void setUser(String user) {
        User = user;
    }
}