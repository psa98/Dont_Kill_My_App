package c.ponom.survivalistapplication.lifekeeper;

import android.content.Context;
import android.content.Intent;
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
    private static volatile LifeKeeper instance;
    private final KeepAliveReceiver keepAliveReceiver;
    //его тоже сделать синглтоном и получать через гетинстанс?

    private final ArrayList<MutableLiveData<Long>> subscriptions = new ArrayList<>();
    private final ArrayList<PeriodicSubscription> periodicSubscriptions = new ArrayList<>();
    private Timer timer = new Timer();
    private boolean running = false;

    private LifeKeeper() {
        keepAliveReceiver = new KeepAliveReceiver();

    }


    public synchronized static LifeKeeper getInstance() {
        if (instance == null) {
            instance = new LifeKeeper();

        }
        return instance;
    }

    public void launchRepeatingWorkRequest(long period) {
        OneTimeWorkRequest singleWorkRequest =
                new OneTimeWorkRequest.Builder(RelaunchWorkRequest.class)
                        .setInitialDelay(period, TimeUnit.SECONDS)
                        .addTag("" + period)
                        // tag будет содержать один элемент, равный периоду (в строке)
                        .build();
        workManager.enqueue(singleWorkRequest);
    }

    public void start(Context context) {
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

    public void pause(Context context) {
        running = false;
        unregisterReceivers(context);
        workManager.cancelAllWork();
    }

    /*
     *
     */
    void onIntentEvent(Intent intent) {

    }

    void emitEvents() { // todo - остальсь проверить отписку
        if (!running) return;
        long currentTimestamp = new Date().getTime();
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

    public synchronized LiveData<Long> subscribeOnAllEvents() {
        MutableLiveData<Long> liveData = new MutableLiveData<>();
        subscriptions.add(liveData);
        return liveData;
    }

    public synchronized LiveData<Long> subscribeOnPeriodicEvents(long seconds) {
        if (seconds < MINIMUM_PERIOD) seconds = MINIMUM_PERIOD;
        PeriodicSubscription periodicSubscription = new PeriodicSubscription(seconds);
        periodicSubscriptions.add(periodicSubscription);
        return periodicSubscription.liveData;

    }

    public synchronized void unsubscribeEvents(LiveData<Long> liveData) {

        //noinspection RedundantCast
        subscriptions.remove((MutableLiveData<Long>) liveData); // протестить как оно вообще
    }

    public synchronized void unsubscribePeriodicEvents(LiveData<Long> liveData) {

        PeriodicSubscription periodicSubscription = new PeriodicSubscription((MutableLiveData<Long>) liveData);
        periodicSubscriptions.remove(periodicSubscription); //todo тестить!

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

    void launchTimerTask() {
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
}
