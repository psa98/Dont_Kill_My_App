package c.ponom.keep_alive_library;

import android.content.Context;

@SuppressWarnings("unused")
public class EventReceiver {
    DozeModeListener dozeEventListener;
    BatteryEventListener batteryEventListener;
    TickEventListener tickEventListener;
    private volatile static EventReceiver INSTANCE;
    InternalDozeModeListener internalDozeModeListener;




    EventReceiver() {
        INSTANCE=this;
    }

    /**
     * Возвращает состояние заряда батареи в процентах.
     *
     * */


    public synchronized static EventReceiver getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EventReceiver();
        }
        return INSTANCE;
    }



    /**
     * Установить слушатель на соответствующий тип событий
     * */
    public synchronized  void setDozeModeListener(DozeModeListener eventListener) {
        dozeEventListener = eventListener;
    }

    /**
     * Установить слушатель на соответствующий тип событий. Возвращается % заряда батареи.<BR>
     * Во время заряда батареи данное событие может вызываться через каждые 10 секунд, в doze
     * при разряде - несколько раз в час в лучшем случае
     * */
    public synchronized  void setBatteryEventListener(BatteryEventListener eventListener) {
        batteryEventListener = eventListener;
    }

    /**
     * Установить слушатель на соответствующий тип событий. Пока приложение находится на переднем
     * плане событие гарантированно вызывается в начале каждой минуты. В иных случаях никаких
     * временных гарантий вызова не предоставляется.
     * */
    public synchronized  void setTickEventListener(TickEventListener eventListener) {
        tickEventListener = eventListener;
    }

    void setInternalDozeModeListener(InternalDozeModeListener eventListener) {
        internalDozeModeListener= eventListener;
    }


    public interface DozeModeListener {
        void  onDozeModeChange(boolean mode);
    }

    public interface BatteryEventListener {
        void  onBatteryEvent(float percentCharged);
    }


    public interface TickEventListener {
        void  onTickEvent();
    }

    interface InternalDozeModeListener {
        void onDozeModeChangeInternal(Context context, boolean mode);
    }

}


