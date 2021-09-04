package c.ponom.keep_alive_library;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import c.ponom.keep_alive_library.SharedPreferencesRepository.DataType;

import static android.content.Intent.ACTION_BATTERY_CHANGED;
import static android.content.Intent.ACTION_BATTERY_LOW;
import static android.content.Intent.ACTION_CONFIGURATION_CHANGED;
import static android.content.Intent.ACTION_SCREEN_ON;
import static android.content.Intent.ACTION_TIME_TICK;
import static android.content.Intent.ACTION_USER_PRESENT;
import static android.content.Intent.ACTION_USER_UNLOCKED;
import static android.os.PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED;
import static android.os.PowerManager.ACTION_POWER_SAVE_MODE_CHANGED;

@SuppressWarnings({"unused", "RedundantSuppression"})
public final class LifeKeeper {

    private static volatile LifeKeeper INSTANCE;

    private static final long FREQUENT_REQUEST_PERIOD = 55;
    private static final long INFREQUENT_REQUEST_PERIOD = 230;
    private static final long RARE_REQUEST_PERIOD = 960;
    private static final long MINIMUM_LIFEDATA_PERIOD = 60;
    private static final long TIMER_TASK_PERIOD = 15;
    public static float AUDIO_VOLUME = 0.01f;
    public static int AUDIO_PAUSE = 220;
    private  static WorkManager workManager;
    private Timer timer = new Timer();
    boolean running = false;
    private LifeKeeperEventsListener eventListener;
    private SilencePlayer audioPlayer;
    private boolean useAudioHack = false;
    private boolean initialized =false;
    private SharedPreferencesRepository sharedPreferencesRepository;
    private int minBatteryOptimizationLevel=15;
    private final ArrayList<MutableLiveData<Long>> subscriptions = new ArrayList<>();
    private final ArrayList<PeriodicSubscription> periodicSubscriptions = new ArrayList<>();
    private final KeepAliveReceiver keepAliveReceiver= KeepAliveReceiver.getInstance();


    private LifeKeeper() {
        EventReceiver instance = EventReceiver.getInstance();
        instance.setInternalDozeModeListener((context, mode) -> changeDozeAudiohackState(context,mode));
        //noinspection deprecation
        workManager=WorkManager.getInstance();
        // про этот вариант без  параметра:
        // may be null in unusual circumstances where you have disabled automatic initialization
        // and have failed to manually call initialize(Context, Configuration).
        //noinspection ConstantConditions
        if (workManager!=null) workManager.cancelAllWork();
    }


    public static LifeKeeper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LifeKeeper();
        }
        return INSTANCE;
    }

    /** При значении параметра  extremeDozeAvoidance =true, будет использоваться дополнительный
     * метод поддержания активности приложения в режиме экономии энергии.  Его использование может
     * привести к увеличению расхода батареи. Параметр игнорируется для SDK < 21
     */
    boolean isUsingExtremeDozeAvoidance() {
        return useAudioHack;
    }

    /** При значении параметра  extremeDozeAvoidance =true, будет использоваться дополнительный
     * метод поддержания активности приложения в режиме экономии энергии.  Его использование может
     * привести к увеличению расхода батареи. Параметр игнорируется для SDK < 21
     */
    void setExtremeDozeAvoidanceMode(boolean extremeDozeAvoidance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            useAudioHack=extremeDozeAvoidance;
        } else useAudioHack =false;
    }



    /**
     * Вызовите этот метод из application класса для обеспечения выживания  приложения в фоне.
     * Oно так же будет автоматически перезапускаться при перезагрузке. При этом (и при любом перезапуске)
     * гарантируется вызов метода onCreate(..) Application класса, где можно возобновить необходимые
     * подписки на события.<BR>
     * При значении параметра  extremeDozeAvoidance =true, будет использоваться дополнительный
     * метод поддержания активности приложения в режиме экономии энергии, до достижения заданного
     * уровня разрядки батареи (15% по умолчанию).  Его использование может
     * привести к увеличению расхода батареи в DozeMode. Параметр игнорируется для SDK < 21
     * При необходимости выполнять периодические фоновые действия  возможна установка слушателей
     * на периодические события через setEventListener(), или подписка на обновление лайфдат через
     * subscribeOnPeriodicEvents (...) subscribeOnAllEvents(), или на получение конкретных интентов
     * в классе EventReceiver через установку слушателей типа setBatteryEventListener(...)
     */
    public void start(Context context, boolean extremeDozeAvoidance) {
        initLifeKeeper(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            useAudioHack=extremeDozeAvoidance;
        } else useAudioHack=false;
        running = true;
        registerReceivers(context);
        workManager = WorkManager.getInstance(context);
        workManager.cancelAllWork();
        launchRepeatingWorkRequest(FREQUENT_REQUEST_PERIOD);
        launchRepeatingWorkRequest(INFREQUENT_REQUEST_PERIOD);
        launchRepeatingWorkRequest(RARE_REQUEST_PERIOD);
        launchTimerTask();
    }



    /**
     * Вызовите этот метод из application класса для обеспечения выживания  приложения в фоне,
     * без примерения  агрессивного метода  сохранения жизни приложения(аудиохака)
     * Ппри любом перезапуске  гарантируется вызов метода onCreate(..) Application класса,
     * где можно возобновить необходимые подписки на события.<BR>
     * При необходимости выполнять периодические фоновые действия  возможна установка слушателей
     * на периодические события через setEventListener(), или подписка на обновление лайфдат через
     * subscribeOnPeriodicEvents (...) subscribeOnAllEvents(), или на получение конкретных интентов
     * в классе EventReceiver через установку слушателей типа setBatteryEventListener(...)
     */
    public void start(Context context){
        start(context,false);
    }

    /**
     * Временная приостановка работы ресиверов и эмиттеров событий, и фоновой работы методов
     * сохранения жизни приложения
     * Не отключает автоматический запуск  приложения при перезагрузке телефона  в будущем
     */
    void pause(Context context) {
        initLifeKeeper(context);
        running = false;
        unregisterReceivers(context);
        workManager.cancelAllWork();
    }

    /**
     * Подготовка класса к работе. Позволяет выполнить последующую подписку на получение событий
     * через получение liveData объектов. Не меняет текущего уровня поддержки активности приложения
     * и не запускает поддержку методов сохранения жизни приложения.
     * Вызов метода не обязателен,можно сразу использовать вызов метода start(Context...).
     */
    public void init(Context context) {
        initLifeKeeper(context);
    }

    /**
     * Данные в возвращаемой лайфдате (unix time время в мс) обновляются при первой возможности,
     * с максимально возможной частотой, позволяемой системой.<BR>
     * Гарантий вызова события с заданной частотой дать невозможно, система может убить или
     * остановить приложение в любой момент. По итогам тестирования, в doze  mode частота обновления
     * лайфдаты может упасть до 3 минут, при длительном нахождении в режиме экономии энергии -
     * 20 минут. При нахождении приложения во фронте или устройства на зарядке  типичная частота
     * обновления лайфдаты вырастает до  одного вызова каждые 5-15 секунд,  при включенной
     * экстремальной  оптимизации doze mode игнорируется и можно ожидать высокой частоты вызовов
     * 5-15 секунд. <BR> Для китайских телефонов  можно ожидать очень проблемной работы в фоне без
     * отключения режима экономии для приложения
     * */
    @NotNull
    public LiveData<Long> subscribeOnAllEvents() {
        checkState();
        MutableLiveData<Long> liveData = new MutableLiveData<>();
        subscriptions.add(liveData);
        return liveData;
    }




    /**
     * Данные в возвращаемой лайфдате (unix time время в мс) обновляются при первой возможности,
     * с максимально возможной частотой, позволяемой системой, но не чаще заданного периода
     * (минимум - 60 секунд).<BR>
     * Гарантий вызова события с заданной частотой дать невозможно, система может убить или
     * остановить приложение в любой момент. Типичные задержки вызова  (заданное время+задержка)
     *  в doze mode по итогам тестов могут достигать 3 минут, при длительном нахождение в режиме-
     * 20 минут. При нахождении приложения во фронте - не более 5-15 секунд, при включенной экстремальной
     * оптимизации - несколько раз в минуту, игнорируя doze mode  Для китайских телефонов
     * можно ожидать очень проблемной работы в фоне без отключения режима экономии для приложения
     *
     * */

    public LiveData<Long> subscribeOnPeriodicEvents(long seconds) {
        checkState();
        if (seconds < MINIMUM_LIFEDATA_PERIOD) seconds = MINIMUM_LIFEDATA_PERIOD;
        PeriodicSubscription periodicSubscription = new PeriodicSubscription(seconds);
        periodicSubscriptions.add(periodicSubscription);
        return periodicSubscription.liveData;
    }


    public void unsubscribeEvents(LiveData<Long> liveData) {
        checkState();
        //noinspection RedundantCast
        subscriptions.remove((MutableLiveData<Long>) liveData);
    }

    public void unsubscribePeriodicEvents(LiveData<Long> liveData) {
        checkState();
        PeriodicSubscription periodicSubscription = new PeriodicSubscription((MutableLiveData<Long>) liveData);
        periodicSubscriptions.remove(periodicSubscription);
    }


    /**
     * Вызов слушателя осуществляется  при первой возможности, с максимально возможной частотой,
     * позволяемой системой.<BR>
     *  Гарантий вызова события с заданной частотй дать невозможно, система может убить или
     * остановить приложение в любой момент. Ожидаемая частота вызова при телефоне на зарядке
     * и/или приложении во фронте - раз в 5-10 секунд.  Типичный период вызова  в doze mode по
     * итогам тестов может достигать одного раза в  3 - 10 - 20 минут, при включенной экстремальной
     * оптимизации - несколько раз в минуту, игнорируя doze mode
     * */

    public void setEventListener(LifeKeeperEventsListener eventListener) {
        checkState();
        this.eventListener = eventListener;
    }

    public int getMinBatteryOptimizationLevel() {
        return minBatteryOptimizationLevel;
    }

    /**
     * @param minBatteryOptimizationLevel
     * Уровень заряда батареи в %, ниже которого прекращается использование агрессивного метода
     * сохранения жизни приложения, по умолчанию 15%
     */
    public void setMinBatteryOptimizationLevel(int minBatteryOptimizationLevel) {
        this.minBatteryOptimizationLevel = minBatteryOptimizationLevel;
    }

    final void launchTimerTask() {
        if (!running) return;
        timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                emitEvents();

            }
        }, TIMER_TASK_PERIOD * 1000);
    }


    final synchronized void launchRepeatingWorkRequest(long period) {
        OneTimeWorkRequest singleWorkRequest =
                new OneTimeWorkRequest.Builder(RelaunchWorkRequest.class)
                        .setInitialDelay(period, TimeUnit.SECONDS)
                        .addTag("seconds=" + period)
                        .build();
        workManager.enqueue(singleWorkRequest);
    }


    final synchronized void emitEvents() {
        if (!running) return;
        long currentInstant = new Date().getTime();
        if (sharedPreferencesRepository!=null) {
            sharedPreferencesRepository.saveParameter(currentInstant, "lastEventDate",
                    DataType.LONG);
        }
        // запись в SP  намекает системе что приложение делает что-то полезное
        onEachEvent(currentInstant);
        checkAllEventsSubscriptions(currentInstant);
        checkPeriodicSubscriptions(currentInstant);
        launchTimerTask();
    }

    private void registerReceivers(@NotNull Context context) {


        context.registerReceiver(keepAliveReceiver, new IntentFilter(ACTION_TIME_TICK));
        context.registerReceiver(keepAliveReceiver, new IntentFilter(ACTION_SCREEN_ON));
        context.registerReceiver(keepAliveReceiver, new IntentFilter(ACTION_BATTERY_CHANGED));
        context.registerReceiver(keepAliveReceiver, new IntentFilter(ACTION_BATTERY_LOW));
        context.registerReceiver(keepAliveReceiver, new IntentFilter(ACTION_USER_PRESENT));
        context.registerReceiver(keepAliveReceiver, new IntentFilter(ACTION_CONFIGURATION_CHANGED));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.registerReceiver(keepAliveReceiver, new IntentFilter(ACTION_POWER_SAVE_MODE_CHANGED));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.registerReceiver(keepAliveReceiver, new IntentFilter(ACTION_DEVICE_IDLE_MODE_CHANGED));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.registerReceiver(keepAliveReceiver, new IntentFilter(ACTION_USER_UNLOCKED));
        }
    }

    private void unregisterReceivers(@NotNull Context context) {
        context.unregisterReceiver(keepAliveReceiver);
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
                liveDataPeriodic.previousSubscriptionEventTimestamp = currentTimestamp;
                periodicSubscriptions.set(i, liveDataPeriodic);
            }
        }
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


    /*  метод вызывается  с максимально возможной частотой, позволяемой системой, типичный параметр
     * по итогам тестов - от 5-10 секунд на зарядке телефона до каждые 5-10 и до 20 минут в doze mode
     */
    public interface LifeKeeperEventsListener {
        void  onEvent(long timestamp);
    }


    private synchronized   void onEachEvent(long timestamp){
        if (eventListener!=null)eventListener.onEvent(timestamp);
    }


    private synchronized void setLiveDataFromMainThread(MutableLiveData<Long> liveData,
                                                        long currentTimestamp) {
        final Looper mainLooper = Looper.getMainLooper();
        if (Looper.myLooper() == mainLooper)
            liveData.setValue(currentTimestamp);
        else{
            // переброска исполнения для воркеров в main thread, иначе LiveData.set не работает
            Handler handler = new Handler(mainLooper);
            handler.post(() -> liveData.setValue(currentTimestamp));
        }
    }

    private void checkAllEventsSubscriptions(long currentTimestamp) {
        for (MutableLiveData<Long> liveData :
                subscriptions) {
            if (liveData != null) setLiveDataFromMainThread(liveData,currentTimestamp);
        }
    }



    private void changeDozeAudiohackState(Context context, boolean enabled) {
        //подход такой - при переходе в режим on - немедленно включаем звук, если прочие условия выполняются.
        // при выключении - останавливаем проигрыватель
        final EventReceiver eventReceiver = EventReceiver.getInstance();
        if (eventReceiver.getCurrentBatteryCharge(context)<minBatteryOptimizationLevel)
            return;
        if (!useAudioHack||!running) return;
        if (enabled) audioPlayer.launchNewPeriodicPlay(context, R.raw.silence2minwav, AUDIO_PAUSE);
        else audioPlayer.stopPlayer();
    }

    private void initLifeKeeper(Context context) {
        if (initialized) return;
        sharedPreferencesRepository = new SharedPreferencesRepository(context);
        initialized = true;
        audioPlayer = new SilencePlayer(context, R.raw.silence2minwav);
        audioPlayer.setVolume(AUDIO_VOLUME);
    }

    private void checkState() {
        if (initialized=false)
            throw new
                    IllegalStateException("LifeKeeper isn't initialized." +
                    " Use init(Context context) or start(Context context....) before accessing API");
    }
}
