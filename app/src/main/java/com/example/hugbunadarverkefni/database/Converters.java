package com.example.hugbunadarverkefni.database;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.room.TypeConverter;
import com.example.hugbunadarverkefni.model.Comment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

public class Converters {

    @TypeConverter
    public static Long fromDate(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static String fromLongList(List<Long> list) {
        return new Gson().toJson(list);
    }

    @TypeConverter
    public static List<Long> toLongList(String json) {
        Type type = new TypeToken<List<Long>>() {}.getType();
        return new Gson().fromJson(json, type);
    }

    @TypeConverter
    public static String fromCommentList(List<Comment> comments) {
        return new Gson().toJson(comments);
    }

    @TypeConverter
    public static List<Comment> toCommentList(String json) {
        Type type = new TypeToken<List<Comment>>() {}.getType();
        return new Gson().fromJson(json, type);
    }

    @TypeConverter
    public static String fromBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    @TypeConverter
    public static Bitmap toBitmap(String base64String) {
        if (base64String == null) return null;
        byte[] bytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}

