package c.ponom.survivalistapplication;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import c.ponom.keep_alive_library.LifeKeeperAPI;


public class App extends Application {


    public static final String TAG = "LifeKeeper";
    private static SharedPreferences sharedPreferences;
    public static final boolean debugMode = true;
    public static SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences("globalSettings", Context.MODE_PRIVATE);
        LifeKeeperAPI.start(this,true);
        BackgroundWorker backGroundWorker = new BackgroundWorker();
        backGroundWorker.backgroundProcessorInit();
    }
}



