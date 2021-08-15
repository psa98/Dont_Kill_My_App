package c.ponom.survivalistapplication;

import android.content.Context;
import android.content.SharedPreferences;

import c.ponom.keep_alive_library.LifeKeeper;


public class Application extends android.app.Application {


    public static final String TAG = "LifeKeeper";
    private static SharedPreferences sharedPreferences;
    private static android.app.Application thisApplication;
    public static final boolean debugMode = (BuildConfig.DEBUG);

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
        LifeKeeper.getInstance().start(this);
        BackgroundWorker backGroundWorker = new BackgroundWorker();
        backGroundWorker.backgroundProcessorSetup();
        Logger.registerInSkippedLogEvent("\n"+Logger.formattedTimeStamp()
                +" Application relaunched");
    }
}



