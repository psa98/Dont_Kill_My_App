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
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static c.ponom.survivalistapplication.SharedPrefsRepository.*;
import static c.ponom.survivalistapplication.SharedPrefsRepository.DataType.STRING;


/*
Дополнительные идеи в развитие:
1. Приложение анализируя базу на пропуски кликов за прошлые сутки самостоятельно определяет адекватен
ли уровень принятых мер против засыпания (с учетом оптимизации аппарата и уровня сдк)
меры для выживания по нарастающей:
- воркер простой как сейчас
- если на нем категорически не обеспечивается - включаем форграунд сервис (хз как оно работает на 25+СДК)
- если и с форграунд сервисом не обеспечивается - предлагаем юзеру отключить оптимизацию питания для приложения.
- если он не отключил, то делаем форграунд с проигр. бесш.музыки. Продолжительность подбрирается под аппарат до работы без пропусков. Юзер предупреждается о повышенном расходе батерейки (а можно его оценить как то?).

Target СДК 31 - воркер с приоритетом + смотреть какие тут проблемы со стартом форграунда из фона. Если запретили реально - вероятно по итогам анализа тиков смотреть можно ли обеспечить работу из фона без форграунда,  если  и там все плохо - нотификация юзеру с просьбой отключить оптимизацию приложения

 */


public class App extends Application {


    public static MutableLiveData<String> liveList=new MutableLiveData<>();
    public static int tickCount;
    public static final long FREQUENT_REQUEST_PERIOD=30;
    private static final long INFREQUENT_REQUEST_PERIOD=300;
    Application application;
    public static ArrayList<Date> tickList =new ArrayList<>();
    public static String fullListString="";
    public static WorkManager workManager;
    private static SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        application =this;
        sharedPreferences=getSharedPreferences("globalSettings", Context.MODE_PRIVATE);
        workManager =WorkManager.getInstance(this);
        workManager.cancelAllWork();
        launchFrequentWorkRequest(FREQUENT_REQUEST_PERIOD);
        KeepAliveReceiver keepAliveReceiver =new KeepAliveReceiver();
        this.registerReceiver(keepAliveReceiver,new IntentFilter(Intent.ACTION_TIME_TICK));
        appendEvent("\n Relaunch"+formattedTimeStamp());
        liveList.postValue(fullListString);
    }

    static void registerTick(){
        tickList.add(new Date());
        String eventString ="\n"+ formattedTimeStamp()+",on Tick";
        fullListString=fullListString+eventString;
        appendEvent(eventString);
    }


    public static void registerBoot() {
        String eventString ="\n"+ formattedTimeStamp()+",rebooting";
        fullListString=fullListString+ eventString;
        appendEvent(eventString);
    }

    public static void registerWorkerEvent(String type) {
        String eventString ="\n"+ formattedTimeStamp()+", worker Event "+type;
        fullListString=fullListString+eventString;
        appendEvent(eventString);

    }

    static String formattedTimeStamp(){
      @SuppressLint("SimpleDateFormat")
      SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
      return formatter.format(new Date());
    };


    private static synchronized void  appendEvent (String eventString){
        String oldEventsList= getParameterString("events");
        saveParameter(oldEventsList+eventString,"events", STRING);
        liveList.postValue(oldEventsList+eventString);
    }

    public static void launchFrequentWorkRequest(long period) {
        OneTimeWorkRequest singleWorkRequest =
                new OneTimeWorkRequest.Builder(RelaunchWorkRequest.class)
                        .setInitialDelay(period,TimeUnit.SECONDS)
                        .addTag(""+period) // tag будет содержать один элемент, равный периоду (в строке)
                        .build();
        singleWorkRequest.hashCode();
        workManager.enqueue(singleWorkRequest);
    }

    public static SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }


}



