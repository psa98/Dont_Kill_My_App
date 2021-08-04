package c.ponom.survivalistapplication;

import android.content.Context;
import android.content.SharedPreferences;


public class Application extends android.app.Application {


    public static final String TAG = "LifeKeeper";
    private static SharedPreferences sharedPreferences;
    private static android.app.Application thisApplication;

    public static SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public static android.app.Application getAppContext() {
        return thisApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        thisApplication = this;
        sharedPreferences = getSharedPreferences("globalSettings", Context.MODE_PRIVATE);
        MyBackGroundWork myBackGroundWork = new MyBackGroundWork();
        myBackGroundWork.launchLifeKeeper(this);
        myBackGroundWork.backgroundProcessor();
    }
}



