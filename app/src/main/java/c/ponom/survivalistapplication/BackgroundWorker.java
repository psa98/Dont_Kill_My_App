package c.ponom.survivalistapplication;

import android.util.Log;

import androidx.lifecycle.LiveData;

import java.time.Instant;
import java.util.Date;

import c.ponom.survivalistapplication.lifekeeper_test_versions_dont_use.LifeKeeper;
import c.ponom.survivalistapplication.lifekeeper_test_versions_dont_use.ReceiverEvents;

import static c.ponom.survivalistapplication.Application.TAG;
import static c.ponom.survivalistapplication.Application.debugMode;
import static c.ponom.survivalistapplication.model.SharedPrefsRepository.getParameterString;

public class BackgroundWorker {


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
    public void backgroundProcessorSetup() {
        ReceiverEvents receiverEvents = ReceiverEvents.getInstance();
        LifeKeeper lifeKeeper = LifeKeeper.getInstance();
        lifeKeeper.subscribeOnAllEvents()
                .observeForever(time ->{
                    if (debugMode) Log.e(TAG, "detected event in service ");});

        LiveData<Long> liveData60s = lifeKeeper.subscribeOnPeriodicEvents(60);
        liveData60s.  observeForever(time ->{
                    if (debugMode) Log.e(TAG, "detected periodic event - 60 s");});

        LiveData<Long> liveData90s = lifeKeeper.subscribeOnPeriodicEvents(90);
        liveData90s.observeForever(time ->{
                    if (debugMode) Log.e(TAG, "detected periodic event - 90 s");});

        lifeKeeper.setEventListener(timestamp -> {
            if (debugMode) Log.e(TAG, "onEvent"+Date.from ( Instant.ofEpochSecond(timestamp/1000)));
        });
        receiverEvents.setBatteryEventListener(percentCharged -> {
                Logger.appendEvent("\n broadcast event logged in receiver - battery event," +
                " charge = "+percentCharged+ " %");
            Log.i(TAG, "broadcast event logged  in receiver - battery event," +
                    " charge = "+percentCharged+ " %");
        });
        receiverEvents.setTickEventListener(()-> {
            Logger.appendEvent("\n"+Logger.formattedTimeStamp()+
                    " tick event  logged in receiver");
            Log.i(TAG, "tick event  logged in receiver");
        });
        receiverEvents.setDozeModeListener(mode ->
                Logger.appendEvent("\n"+Logger.formattedTimeStamp()+
                        " Doze mode event logged in receiver, mode="+mode));

        String oldSkippedEventsList = getParameterString("skipped");
        String oldEventsList = getParameterString("events");
        Logger.appendEvent("\n" + Logger.formattedTimeStamp() + " Service relaunched ");
        Logger.liveEventsList.postValue(oldEventsList);
        Logger.liveSkippedEventsList.postValue(oldSkippedEventsList);
    }
}
