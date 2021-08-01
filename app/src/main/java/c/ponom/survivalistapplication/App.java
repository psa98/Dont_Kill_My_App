package c.ponom.survivalistapplication;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import androidx.lifecycle.MutableLiveData;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static c.ponom.survivalistapplication.SharedPrefsRepository.DataType.STRING;
import static c.ponom.survivalistapplication.SharedPrefsRepository.getParameterString;
import static c.ponom.survivalistapplication.SharedPrefsRepository.saveParameter;


public class App extends Application {

/*Дополнительные идеи в развитие:
Приложение анализируя базу на пропуски кликов за прошлые сутки самостоятельно определяет адекватен
ли уровень принятых мер против засыпания (с учетом оптимизации аппарата и уровня сдк)
меры для выживания по нарастающей:
- воркер простой как сейчас
- если на нем категорически не обеспечивается - включаем форграунд сервис (хз как оно работает на 25+СДК)
- если и с форграунд сервисом не обеспечивается - предлагаем юзеру отключить оптимизацию питания для приложения.
- если он не отключил, то делаем форграунд с проигр. бесш.музыки. Продолжительность подбрирается под аппарат до работы без пропусков. Юзер предупреждается о повышенном расходе батерейки (а можно его оценить как то?).
Target СДК 31 - воркер с приоритетом + смотреть какие тут проблемы со стартом форграунда из фона. Если запретили реально - вероятно по итогам анализа тиков смотреть можно ли обеспечить работу из фона без форграунда,  если  и там все плохо - нотификация юзеру с просьбой отключить оптимизацию приложения
 */

    public static final long FREQUENT_REQUEST_PERIOD = 55;
    private static final long INFREQUENT_REQUEST_PERIOD = 240;
    private static final long EVENT_WAS_SKIPPED_TIME = 300;
    // минимальное время между сработкой эвентов, которое фиксируется в логе как пропуск по вине оптимизации питания


    public static MutableLiveData<String> liveEventsList = new MutableLiveData<>();
    public static MutableLiveData<String> liveSkippedEventsList = new MutableLiveData<>();


    public static WorkManager workManager;
    private static SharedPreferences sharedPreferences;
    static Date lastEventDate = new Date();
    private static Application application;

    public static Application getAppContext() {
        return application;
    }

    public static void registerBroadcastEvent(String intentTypeMessage) {
        String eventString = "\n" + formattedTimeStamp() + intentTypeMessage;
        appendEvent(eventString);
    }

    private static synchronized void appendEvent(String eventString) {
        String oldEventsList = getParameterString("events");
        saveParameter(oldEventsList + eventString, "events", STRING);
        liveEventsList.postValue(oldEventsList + eventString);
        Date currentTimeDate = new Date();
        long secondsBetween = currentTimeDate.getTime() / 1000 - lastEventDate.getTime() / 1000;
        if (secondsBetween > EVENT_WAS_SKIPPED_TIME) {
            String oldSkippedEventsList = getParameterString("skipped");
            String skippedEventDescription =
                    "No timer events registered between " +
                            formatDate(lastEventDate) +
                            " and  " +
                            formatDate(currentTimeDate) +
                            " for " + secondsBetween + "s \n";
            saveParameter(oldSkippedEventsList + skippedEventDescription, "skipped", STRING);
            liveSkippedEventsList.postValue(oldSkippedEventsList + skippedEventDescription);
        }
        lastEventDate = currentTimeDate;
    }

    public static void registerWorkerEvent(String type) {
        String eventString = "\n" + formattedTimeStamp() + ", worker event " + type;
        appendEvent(eventString);

    }

    static String formattedTimeStamp() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(new Date());
    }

    public static void launchRepeatingWorkRequest(long period) {
        OneTimeWorkRequest singleWorkRequest =
                new OneTimeWorkRequest.Builder(RelaunchWorkRequest.class)
                        .setInitialDelay(period, TimeUnit.SECONDS)
                        .addTag("" + period)
                        // tag будет содержать один элемент, равный периоду (в строке)
                        .build();
        workManager.enqueue(singleWorkRequest);
    }

    private static String formatDate(Date date) {
        return new SimpleDateFormat("dd/MM  HH:mm:ss",
                Locale.getDefault()).format(date);
    }

    public static SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    static synchronized void registerInSkippedLogEvent(String event) {
        String oldSkippedEventsList = getParameterString("skipped");
        saveParameter(oldSkippedEventsList + event, "skipped", STRING);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        sharedPreferences = getSharedPreferences("globalSettings", Context.MODE_PRIVATE);
        workManager = WorkManager.getInstance(this);
        workManager.cancelAllWork();
        launchRepeatingWorkRequest(FREQUENT_REQUEST_PERIOD);
        launchRepeatingWorkRequest(INFREQUENT_REQUEST_PERIOD);
        KeepAliveReceiver keepAliveReceiver = new KeepAliveReceiver();
        this.registerReceiver(keepAliveReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        this.registerReceiver(keepAliveReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        appendEvent("\n Relaunch: " + formattedTimeStamp());
        String oldSkippedEventsList = getParameterString("skipped");
        String oldEventsList = getParameterString("events");
        liveEventsList.postValue(oldEventsList);
        liveSkippedEventsList.postValue(oldSkippedEventsList);
    }
}



