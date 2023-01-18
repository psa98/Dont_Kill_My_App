package c.ponom.survivalistapplication;

import static java.lang.System.currentTimeMillis;
import static java.text.DateFormat.MEDIUM;
import static c.ponom.survivalistapplication.App.TAG;
import static c.ponom.survivalistapplication.App.debugMode;
import static c.ponom.survivalistapplication.App.getSharedPreferences;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import c.ponom.keep_alive_library.BackgroundProcessor;
import c.ponom.keep_alive_library.EventReceiver;
import c.ponom.keep_alive_library.LifeKeeperAPI;

public class BackgroundWorker extends BackgroundProcessor {


    /**
     *  тут можно к примеру, инициировать для последующего обзора через
     * observe forever лайфдаты через LifeKeeper.subscribe..., выполнить другие однократные действия
     *  и реализовать все другие обработчики
     * метод так же вызывается при (автозапуске) если в onCreate Application класса есть
     * MyBackgroundWork myBackGroundWork = new MyBackgroundWork();
     * myBackGroundWork.backgroundProcessorSetup();
     *
     * Фактически данный класс используется только для разгрузки Application от излишнего стартового кода
     */
    public void backgroundProcessorInit() {
        EventReceiver eventReceiver = EventReceiver.getInstance();

        LifeKeeperAPI.subscribeOnAllEvents()
                .observeForever(time ->{
                    if (debugMode) Log.i(TAG, "some  event detected");});

        LiveData<Long> liveData60s = LifeKeeperAPI.subscribeOnPeriodicEvents(60);
        liveData60s.  observeForever(time ->{
            Logger.appendEvent("\n"+Logger.formattedTimeStamp()+
                    " Periodic Event  logged in receiver - 60s");

                    if (debugMode) Log.i(TAG, "detected periodic event - 60 s");});

        LiveData<Long> liveData90s = LifeKeeperAPI.subscribeOnPeriodicEvents(90);
        liveData90s.observeForever(time ->{
                    Logger.appendEvent("\n"+Logger.formattedTimeStamp()+
                    " Periodic Event  logged in receiver - 90s");
                    if (debugMode) Log.i(TAG, "detected periodic event - 90 s");});

        LiveData<Long> liveData12h = LifeKeeperAPI.subscribeOnPeriodicEvents(3600*12);

        liveData12h.observeForever(time ->{
            String period = calculatePeriodFromLast(time);
            Logger.appendEvent("\n"+Logger.formattedTimeStamp()+
                    " Rare Event  logged in receiver - 12h, after "+period+"h time");
            Logger.registerInSkippedLogEvent("\n"+Logger.formattedTimeStamp()+
                    " Rare Event  logged in receiver - 12h, after "+period+"h time");
            if (debugMode) Log.i(TAG, "detected periodic event - 12h");
            setLast12hEventTime();
        });

        LifeKeeperAPI.setEventListener(timestamp -> {
            if (debugMode) Log.i(TAG, "onEvent"+ new Date());
        });
        eventReceiver.setBatteryEventListener(percentCharged -> {
                Logger.appendEvent("\n"
                        +Logger.formattedTimeStamp()+
                        " Broadcast event logged in receiver - battery event,"
                        + " charge = "+percentCharged+ " %");
            Log.i(TAG, "broadcast event logged  in receiver - battery event," +
                    " charge = "+percentCharged+ " %");
        });
        eventReceiver.setTickEventListener(()-> {
            Logger.appendEvent("\n"+Logger.formattedTimeStamp()+
                    " Tick event  logged in receiver");
            Log.i(TAG, "tick event  logged in receiver");
        });
        eventReceiver.setDozeModeListener(mode -> {
            Logger.appendEvent("\n" + Logger.formattedTimeStamp() +
                    " Doze mode event logged in receiver, mode=" + mode);
            Logger.registerInSkippedLogEvent("\n"+Logger.formattedTimeStamp()
                    +" Doze mode event logged in receiver, mode="+mode);
        });

        Logger.appendEvent("\n" + Logger.formattedTimeStamp()
                + " Application relaunched ");

        Logger.registerInSkippedLogEvent("\n" + Logger.formattedTimeStamp()
                + " Application relaunched ");

    }

    @NonNull
    private String calculatePeriodFromLast(Long time) {
        int offset= TimeZone.getDefault().getOffset(time);
        DateFormat format= DateFormat.getTimeInstance(MEDIUM);
        long days = (time - last12hEventTime()-offset)/(24*3600*1000);
        long rest = (time - last12hEventTime()-offset)%(24*3600*1000);
        return days+" d " + format.format(new Date(rest));
    }



    Long last12hEventTime() {
        return getSharedPreferences().getLong("last_12h", currentTimeMillis());
    }

    void setLast12hEventTime() {
        getSharedPreferences().edit().putLong("last_12h",currentTimeMillis()).apply();
    }


    @Override
    public void backgroundProcessorStart() {

    }

    @Override
    public void backgroundProcessorStop() {

    }

    @Override
    public void backgroundProcessorRelease() {

    }
}
