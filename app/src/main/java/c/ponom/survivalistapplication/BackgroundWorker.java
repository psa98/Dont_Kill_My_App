package c.ponom.survivalistapplication;

import android.util.Log;

import androidx.lifecycle.LiveData;

import java.time.Instant;
import java.util.Date;

import c.ponom.survivalistapplication.lifekeeper_test_versions_dont_use.KeepAliveReceiver;
import c.ponom.survivalistapplication.lifekeeper_test_versions_dont_use.LifeKeeper;

import static c.ponom.survivalistapplication.Application.TAG;
import static c.ponom.survivalistapplication.Application.debugMode;
import static c.ponom.survivalistapplication.model.SharedPrefsRepository.DataType.STRING;
import static c.ponom.survivalistapplication.model.SharedPrefsRepository.getParameterString;
import static c.ponom.survivalistapplication.model.SharedPrefsRepository.saveParameter;

public class BackgroundWorker {


    /**
     *  тут можно к примеру, инициировать для последедующего обзора
     * observe forever лайфдаты через LifeKeeper.subscribe..., выполнить другие однократные действия
     *  и реализовать все другие обработчики
     * метод так же вызывается при (автозапуске) если в onCreate Application класса есть
     * MyBackgroundWork myBackGroundWork = new MyBackgroundWork();
     * myBackGroundWork.backgroundProcessorSetup();
     *
     * Фактически данный класс  используется только для разгрузки Application от излишнего стартового  кода
     */

    public void backgroundProcessorSetup() {
        KeepAliveReceiver keepAliveReceiver=KeepAliveReceiver.getInstance();
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

        keepAliveReceiver.setBatteryEventListener(percentCharged -> Logger.appendEvent("broadcast event logged - battery event," +
                " charge = "+percentCharged+ " %"));

        keepAliveReceiver.setRebootListener(() -> Logger.appendEvent("broadcast event logged - reboot event"));

        keepAliveReceiver.setTickEventListener(() -> Logger.appendEvent("broadcast event logged - tick event"));


        KeepAliveReceiver.getInstance().setDozeModeListener(mode -> saveParameter(
                "TEST - get interface event on doze mode status, mode = "
                        +mode, "skipped", STRING));

        String oldSkippedEventsList = getParameterString("skipped");
        String oldEventsList = getParameterString("events");
        Logger.appendEvent("\n" + Logger.formattedTimeStamp() + " Service relaunched ");
        Logger.liveEventsList.postValue(oldEventsList);
        Logger.liveSkippedEventsList.postValue(oldSkippedEventsList);

    }



}
