package c.ponom.survivalistapplication;

import android.util.Log;

import c.ponom.survivalistapplication.lifekeeper.LifeKeeper;

import static c.ponom.survivalistapplication.Application.TAG;
import static c.ponom.survivalistapplication.Application.debugMode;
import static c.ponom.survivalistapplication.model.SharedPrefsRepository.getParameterString;

public class MyBackgroundWork  {


    /**
     *  тут можно к примеру, инициировать для последедующего обзора
     * observe forever лайфдаты через LifeKeeper.subscribe..., выполнить другие однократные действия
     * метод так же вызывается при ребуте  если в onCreate Application класса есть
     * MyBackgroundWork myBackGroundWork = new MyBackgroundWork();
     * myBackGroundWork.backgroundProcessorSetup();
     *
     * Фактически данный класс  используется только для разгрузки Application от излишнего кода
     */

    public void backgroundProcessorSetup() {
        LifeKeeper lifeKeeper = LifeKeeper.getInstance();
        lifeKeeper.subscribeOnAllEvents()
                .observeForever(time ->{
                    if (debugMode) Log.e(TAG, "detected event in service ");});

        lifeKeeper.subscribeOnPeriodicEvents(60)
                .observeForever(time ->{
                    if (debugMode) Log.e(TAG, "detected periodic event - 60 s");});

        lifeKeeper.subscribeOnPeriodicEvents(90)
                .observeForever(time ->{
                    if (debugMode) Log.e(TAG, "detected periodic event - 90 s");});


        String oldSkippedEventsList = getParameterString("skipped");
        String oldEventsList = getParameterString("events");
        Logger.appendEvent("\n" + Logger.formattedTimeStamp() + " Service relaunched ");
        Logger.liveEventsList.postValue(oldEventsList);
        Logger.liveSkippedEventsList.postValue(oldSkippedEventsList);

    }


}
