package c.ponom.survivalistapplication;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;


public class App extends Application {


    public static final String TAG = "LifeKeeper";
    private static SharedPreferences sharedPreferences;
    private static Application application;

    public static SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public static Application getAppContext() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        sharedPreferences = getSharedPreferences("globalSettings", Context.MODE_PRIVATE);
        startService(new Intent(this, BackgroundService.class));
    }


}



