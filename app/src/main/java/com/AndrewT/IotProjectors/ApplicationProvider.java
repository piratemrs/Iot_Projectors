package com.AndrewT.IotProjectors;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

public class ApplicationProvider extends Application {

    private static Context sApplicationContext;

    @Override
    public void onCreate() {
        super.onCreate();

        sApplicationContext = getApplicationContext();

    }

    public static Context getContext() {
        return sApplicationContext;
    }

 public static void showToast(String msg) {
     Toast.makeText(getContext(),msg,Toast.LENGTH_SHORT).show();
}
}
