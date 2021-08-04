package c.ponom.survivalistapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import c.ponom.survivalistapplication.lifekeeper.LifeKeeper;

import static c.ponom.survivalistapplication.App.TAG;
import static c.ponom.survivalistapplication.SharedPrefsRepository.getParameterString;

public class BackgroundService extends Service {
    public BackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not yet implemented");
    }


    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        LifeKeeper.getInstance().start(this);
        LifeKeeper.getInstance().subscribeOnEvents()
                .observeForever(time -> Log.e(TAG, "detected event in service "));

        LifeKeeper.getInstance().subscribeOnPeriodicEvents(60)
                .observeForever(time -> Log.e(TAG, "detected periodic event - 60 s in service"));

        LifeKeeper.getInstance().subscribeOnPeriodicEvents(90)
                .observeForever(time -> Log.e(TAG, "detected periodic event - 90 s in service "));


        String oldSkippedEventsList = getParameterString("skipped");
        String oldEventsList = getParameterString("events");
        Logger.appendEvent("\n" + Logger.formattedTimeStamp() + " Service relaunched ");
        Logger.liveEventsList.postValue(oldEventsList);
        Logger.liveSkippedEventsList.postValue(oldSkippedEventsList);

    }


}