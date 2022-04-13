package c.ponom.survivalistapplication;

import static c.ponom.survivalistapplication.Application.TAG;
import static c.ponom.survivalistapplication.Application.debugMode;

import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.Date;

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
     * Фактически данный класс  используется только для разгрузки Application от излишнего стартового  кода
     */
    public void backgroundProcessorInit() {
        EventReceiver eventReceiver = EventReceiver.getInstance();

        LifeKeeperAPI.subscribeOnAllEvents()
                .observeForever(time ->{
                    if (debugMode) Log.e(TAG, "some  event detected");});

        LiveData<Long> liveData60s = LifeKeeperAPI.subscribeOnPeriodicEvents(60);
        liveData60s.  observeForever(time ->{
            Logger.appendEvent("\n"+Logger.formattedTimeStamp()+
                    " Periodic Event  logged in receiver - 60s");

                    if (debugMode) Log.e(TAG, "detected periodic event - 60 s");});

        LiveData<Long> liveData90s = LifeKeeperAPI.subscribeOnPeriodicEvents(90);
        liveData90s.observeForever(time ->{
                    Logger.appendEvent("\n"+Logger.formattedTimeStamp()+
                    " Periodic Event  logged in receiver - 90s");
                    if (debugMode) Log.e(TAG, "detected periodic event - 90 s");});

        LifeKeeperAPI.setEventListener(timestamp -> {
            if (debugMode) Log.e(TAG, "onEvent"+ new Date());
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

        //String oldSkippedEventsList = getParameterString("skipped");
        //String oldEventsList = getParameterString("events");
        Logger.appendEvent("\n" + Logger.formattedTimeStamp()
                + " Application relaunched ");

        Logger.registerInSkippedLogEvent("\n" + Logger.formattedTimeStamp()
                + " Application relaunched ");

        // todo Logger.liveEventsList.postValue(oldEventsList);
        //Logger.liveSkippedEventsList.postValue(oldSkippedEventsList);
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
