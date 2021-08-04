package c.ponom.survivalistapplication;

import android.util.Log;

import c.ponom.survivalistapplication.lifekeeper.BackgroundWork;
import c.ponom.survivalistapplication.lifekeeper.LifeKeeper;

import static c.ponom.survivalistapplication.Application.TAG;
import static c.ponom.survivalistapplication.model.SharedPrefsRepository.getParameterString;

public class MyBackGroundWork extends BackgroundWork {


    /**
     * если переопределить метод тут можно, к примеру, инициировать для последедующего обзора
     * observe forever лайфдаты через LifeKeeper.subscribe..., выполнить другие однократные действия
     * метод так же вызывается при ребуте  если в onCreate Application класса есть
     * MyBackGroundWork myBackGroundWork = new MyBackGroundWork();
     * myBackGroundWork.launchLifeKeeper(this);
     * myBackGroundWork.initBackgroundWorkProcessor();
     * <p>
     * можно оставить метод пустым и просто обсервить в нужной точке кода лайфдаты,
     * полученные от LifeKeeper после его ручного запуска статическим методом
     */
    @Override
    public void backgroundProcessor() {
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
