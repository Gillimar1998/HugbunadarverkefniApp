package com.example.hugbunadarverkefni.api;

import com.example.hugbunadarverkefni.model.LoginRequest;
import com.example.hugbunadarverkefni.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserApiService {
    @POST("/users/login")
    Call<User> login(@Body LoginRequest loginRequest);

    @POST("/users")
    Call<User> registerUser(@Body User user);
}