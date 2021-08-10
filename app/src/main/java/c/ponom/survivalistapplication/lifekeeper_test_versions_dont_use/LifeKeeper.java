package c.ponom.survivalistapplication.lifekeeper_test_versions_dont_use;

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
public final class LifeKeeper {
    private static final long FREQUENT_REQUEST_PERIOD = 55;
    private static final long INFREQUENT_REQUEST_PERIOD = 240;
    private static final long MINIMUM_PERIOD = 60;
    private static final long TIMER_TASK_PERIOD = 15;
    private  WorkManager workManager;
    private static volatile LifeKeeper INSTANCE;
    private final ArrayList<MutableLiveData<Long>> subscriptions = new ArrayList<>();
    private final ArrayList<PeriodicSubscription> periodicSubscriptions = new ArrayList<>();
    private Timer timer = new Timer();
    private boolean running = false;
    LifeKeeperEventsListener eventListener;



     private LifeKeeper() {

    }


    public synchronized static LifeKeeper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LifeKeeper();

        }
        return INSTANCE;
    }

    public final synchronized void launchRepeatingWorkRequest(long period) {
        OneTimeWorkRequest singleWorkRequest =
                new OneTimeWorkRequest.Builder(RelaunchWorkRequest.class)
                        .setInitialDelay(period, TimeUnit.SECONDS)
                        .addTag("seconds=" + period)
                        .build();

        workManager.enqueue(singleWorkRequest);
    }

    public final synchronized void start(Context context) {
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

    public final synchronized void pause(Context context) {
        running = false;
        unregisterReceivers(context);
        workManager.cancelAllWork();
    }


    final synchronized void emitEvents() {

        if (!running) return;
        long currentTimestamp = new Date().getTime();
        onEachEvent(currentTimestamp);
        checkAllEventsSubscriptions(currentTimestamp);
        checkPeriodicSubscriptions(currentTimestamp);
        launchTimerTask();

    }

    private void checkPeriodicSubscriptions(long currentTimestamp) {
        PeriodicSubscription liveDataPeriodic;
        for (int i = 0; i < periodicSubscriptions.size(); i++) {
            liveDataPeriodic = periodicSubscriptions.get(i);
            if (liveDataPeriodic == null) continue;
            final long secondsBetween = (currentTimestamp -
                    liveDataPeriodic.previousSubscriptionEventTimestamp)/1000;
            if (secondsBetween>= liveDataPeriodic.periodicity) {
                setLiveDataFromMainThread(liveDataPeriodic.liveData,currentTimestamp);
                Logger.appendEvent("\n" + Logger.formattedTimeStamp()
                        + " Periodic event #" + (i + 1) + " " + liveDataPeriodic.periodicity + " s");
                liveDataPeriodic.previousSubscriptionEventTimestamp = currentTimestamp;
                periodicSubscriptions.set(i, liveDataPeriodic);
            }
        }
    }

    private void checkAllEventsSubscriptions(long currentTimestamp) {
        for (MutableLiveData<Long> liveData :
                subscriptions) {
            if (liveData != null) setLiveDataFromMainThread(liveData,currentTimestamp);
        }
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


    public synchronized void unsubscribeEvents(LiveData<Long> liveData) {
        //noinspection RedundantCast
        subscriptions.remove((MutableLiveData<Long>) liveData);
    }

    public  synchronized void unsubscribePeriodicEvents(LiveData<Long> liveData) {
        PeriodicSubscription periodicSubscription = new PeriodicSubscription((MutableLiveData<Long>) liveData);
        periodicSubscriptions.remove(periodicSubscription);
    }



    private void registerReceivers(Context context) {
        KeepAliveReceiver keepAliveReceiver =KeepAliveReceiver.getInstance();
        context.registerReceiver(keepAliveReceiver, new IntentFilter(ACTION_TIME_TICK));
        context.registerReceiver(keepAliveReceiver, new IntentFilter(ACTION_BATTERY_CHANGED));
        context.registerReceiver(keepAliveReceiver, new IntentFilter(ACTION_POWER_SAVE_MODE_CHANGED));
        context.registerReceiver(keepAliveReceiver, new IntentFilter(ACTION_DEVICE_IDLE_MODE_CHANGED));
    }

     private void unregisterReceivers(Context context) {
        context.unregisterReceiver(KeepAliveReceiver.getInstance());
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
        private long periodicity;
        private long previousSubscriptionEventTimestamp;

        public PeriodicSubscription(MutableLiveData<Long> liveData) {
            this.liveData = liveData;
        }

        public PeriodicSubscription(long seconds) {
            liveData = new MutableLiveData<>();
            periodicity = seconds;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (!(obj instanceof PeriodicSubscription)) return false;
            return this.liveData == ((PeriodicSubscription)(obj)).liveData;
        }
    }

    public synchronized  void setEventListener(LifeKeeperEventsListener eventListener) {
        this.eventListener = eventListener;
    }

    public interface LifeKeeperEventsListener {
        void  onEvent(long timestamp);
    }


    private synchronized    void onEachEvent(long timestamp){
     if (eventListener!=null)eventListener.onEvent(timestamp);
    }


    private synchronized void setLiveDataFromMainThread(MutableLiveData<Long> liveData, long currentTimestamp) {

        final Looper mainLooper = Looper.getMainLooper();
        if (mainLooper.isCurrentThread())
            liveData.setValue(currentTimestamp);
        else{
            // переброска исполнения для воркеров в main thread, иначе LiveData.set не работает
            Handler handler = new Handler(mainLooper);
            handler.post(() -> liveData.setValue(currentTimestamp));
        }

    }
}
