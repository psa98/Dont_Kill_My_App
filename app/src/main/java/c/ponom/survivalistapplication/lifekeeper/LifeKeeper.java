package c.ponom.survivalistapplication.lifekeeper;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import c.ponom.survivalistapplication.Logger;

import static android.content.Intent.ACTION_BATTERY_CHANGED;
import static android.content.Intent.ACTION_TIME_TICK;
import static android.os.PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED;
import static android.os.PowerManager.ACTION_POWER_SAVE_MODE_CHANGED;


@SuppressWarnings({"unused", "RedundantSuppression"})
public  class LifeKeeper {
    private static final long FREQUENT_REQUEST_PERIOD = 55;
    private static final long INFREQUENT_REQUEST_PERIOD = 240;
    private static final long MINIMUM_PERIOD = 60;
    private static final long TIMER_TASK_PERIOD = 15;
    private  WorkManager workManager;
    private static volatile LifeKeeper instance;
    private final KeepAliveReceiver keepAliveReceiver;
    //его тоже сделать синглтоном и получать через гетинстанс?

    private final ArrayList<MutableLiveData<Long>> subscriptions = new ArrayList<>();
    private final ArrayList<PeriodicSubscription> periodicSubscriptions = new ArrayList<>();
    private Timer timer = new Timer();
    private boolean running = false;


     LifeKeeper() {
        keepAliveReceiver = new KeepAliveReceiver();

    }


    public synchronized static LifeKeeper getInstance() {
        if (instance == null) {
            instance = new LifeKeeper();

        }
        return instance;
    }

    public final void launchRepeatingWorkRequest(long period) {
        OneTimeWorkRequest singleWorkRequest =
                new OneTimeWorkRequest.Builder(RelaunchWorkRequest.class)
                        .setInitialDelay(period, TimeUnit.SECONDS)
                        .addTag("seconds=" + period)
                        .build();
        workManager.enqueue(singleWorkRequest);
    }

    public final void start(Context context) {
        running = true;
        registerReceivers(context);
        workManager = WorkManager.getInstance(context);
        workManager.cancelAllWork();
        launchRepeatingWorkRequest(FREQUENT_REQUEST_PERIOD);
        launchRepeatingWorkRequest(INFREQUENT_REQUEST_PERIOD);
        launchTimerTask();
    }


    /*
     *
     */

    public final  void pause(Context context) {
        running = false;
        unregisterReceivers(context);
        workManager.cancelAllWork();
    }


    final void emitEvents() {

        if (!running) return;

        long currentTimestamp = new Date().getTime();
        onEachEvent(currentTimestamp);
        for (MutableLiveData<Long> liveData :
                subscriptions) {
            if (liveData != null) liveData.setValue(currentTimestamp);
        }
        PeriodicSubscription liveDataPeriodic;
        for (int i = 0; i < periodicSubscriptions.size(); i++) {
            liveDataPeriodic = periodicSubscriptions.get(i);
            if (liveDataPeriodic == null) continue;
            if (liveDataPeriodic.liveData != null &&
                    ((currentTimestamp - liveDataPeriodic.previousSubscriptionEventTimestamp)
                            >= liveDataPeriodic.periodicity * 1000)) {
                liveDataPeriodic.liveData.setValue(currentTimestamp);
                Logger.appendEvent("\n" + Logger.formattedTimeStamp()
                        + " Periodic event #" + (i + 1) + " " + liveDataPeriodic.periodicity + " s");
                liveDataPeriodic.previousSubscriptionEventTimestamp = currentTimestamp;
                periodicSubscriptions.set(i, liveDataPeriodic);
            }
        }
        launchTimerTask();

    }

    public final synchronized LiveData<Long> subscribeOnAllEvents() {
        MutableLiveData<Long> liveData = new MutableLiveData<>();
        subscriptions.add(liveData);
        return liveData;
    }

    /*
    * Данные в возвращаемой лайфдате (unix time события) обновляются при первой возможности,
    * с максимально возможной частотой, позволяемой системой но не чаще заданного периода, и
    * (минимум - 60 секунд).
    * Гарантий вызова события с заданной частотй дать невозможно, система может убить или
    * остановить приложение в любой момент. Типичные задержки вызова  (заданное время+задержка)
    *  в doze mode по итогам тестов могут достигать 3 минут, при длительном нахождение в режиме-
    * 20 минут. При нахождении приложеня во фронте - не более 5-15 секунд. Для китайских телефонов
    * можно ожидать очень проблемной работы в фоне без отключения режима экономии для приложения
    *
    * */
    public final synchronized LiveData<Long> subscribeOnPeriodicEvents(long seconds) {
        if (seconds < MINIMUM_PERIOD) seconds = MINIMUM_PERIOD;
        PeriodicSubscription periodicSubscription = new PeriodicSubscription(seconds);
        periodicSubscriptions.add(periodicSubscription);
        return periodicSubscription.liveData;

    }

    // todo - сделаны приватными до завершения тестирования
    private synchronized void unsubscribeEvents(LiveData<Long> liveData) {
        //noinspection RedundantCast
        subscriptions.remove((MutableLiveData<Long>) liveData); // протестить как оно вообще
    }

    private  synchronized void unsubscribePeriodicEvents(LiveData<Long> liveData) {
        PeriodicSubscription periodicSubscription = new PeriodicSubscription((MutableLiveData<Long>) liveData);
        periodicSubscriptions.remove(periodicSubscription);
    }



    private void registerReceivers(Context context) {
        context.registerReceiver(keepAliveReceiver, new IntentFilter(ACTION_TIME_TICK));
        context.registerReceiver(keepAliveReceiver, new IntentFilter(ACTION_BATTERY_CHANGED));
        context.registerReceiver(keepAliveReceiver, new IntentFilter(ACTION_POWER_SAVE_MODE_CHANGED));
        context.registerReceiver(keepAliveReceiver, new IntentFilter(ACTION_DEVICE_IDLE_MODE_CHANGED));
    }

     private void unregisterReceivers(Context context) {
        context.unregisterReceiver(keepAliveReceiver);
    }

    final void launchTimerTask() {
        if (!running) return;
        timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // переброска исполнения в main thread, иначе LiveData.set не работает
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    Logger.appendEvent("\n" + Logger.formattedTimeStamp() + " Timer task");
                    emitEvents();

                });
            }
        }, TIMER_TASK_PERIOD * 1000);
    }

    private static class PeriodicSubscription {
        private final MutableLiveData<Long> liveData;
        private long previousSubscriptionEventTimestamp;
        private long periodicity;

        public PeriodicSubscription(MutableLiveData<Long> liveData) {
            this.liveData = liveData;
        }

        public PeriodicSubscription(long seconds) {
            liveData = new MutableLiveData<>();
            periodicity = seconds;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (!(obj instanceof MutableLiveData)) return false;
            return this.liveData == obj;
        }
    }

    /*
    * переопределите класс и метод  если надо получать события максимально часто и  без обработки
    *
    */
    void onEachEvent(long timestamp){

    }

}
