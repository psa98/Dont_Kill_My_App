package c.ponom.keep_alive_library;

import android.content.Context;
import android.os.BatteryManager;
import android.os.PowerManager;

@SuppressWarnings("unused")
public class EventReceiver {
    DozeModeListener dozeEventListener;
    BatteryEventListener batteryEventListener;
    TickEventListener tickEventListener;
    private volatile static EventReceiver INSTANCE;

    static KeepAliveReceiver receiver;
    EventReceiver() {
        INSTANCE=this;
    }

    void initReceiver(KeepAliveReceiver  keepAliveReceiver) {
        if (keepAliveReceiver==null)
            throw new IllegalStateException("Wrong EventReceiver init");
        receiver = keepAliveReceiver;
    }

    public synchronized static EventReceiver getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EventReceiver();
        }
        return INSTANCE;
    }


    /**
     * Возвращает статус текущего  режима экономии заряда. В doze mode большинство бродкастов и воркеров
     * заблокированы и не будут вызыватья за пределами "окон" предоставленных системой.
     * */
    public boolean getDoseModeState(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return pm.isDeviceIdleMode();
    }

    /**
     * Возвращает состояние заряда батареи в процентах
     * */
    public int getBatteryState(Context context) {
        BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
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
     * при разряде - несколько раз в час
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

    public interface DozeModeListener {
        void  onDozeModeChange(boolean mode);
    }

    public interface BatteryEventListener {
        void  onBatteryEvent(int percentCharged);
    }


    public interface TickEventListener {
        void  onTickEvent();
    }


}
