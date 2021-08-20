package c.ponom.keep_alive_library;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class EventReceiver {
    DozeModeListener dozeEventListener;
    BatteryEventListener batteryEventListener;
    TickEventListener tickEventListener;
    private volatile static EventReceiver INSTANCE;
    InternalDozeModeListener internalDozeModeListener;
    static KeepAliveReceiver receiver;


    EventReceiver() {
        INSTANCE=this;
    }

    /**
     * Возвращает состояние заряда батареи в процентах.
     *
     * */

    public float getCurrentBatteryCharge(@NotNull Context context) {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, intentFilter);
        if (batteryStatus==null) return 0f;
        final int DefaultValue = 0;
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL,
                DefaultValue);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE,
                100);

        return  calculateBatteryPercentage(level, scale);
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
     * На SDK 23 и ранее не поддерживается
     * */
    public boolean getDoseModeState(@NotNull Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return pm.isDeviceIdleMode();
        } else return false;
    }

    public boolean getPowerSaveMode(@NotNull Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return pm.isPowerSaveMode();
        else return false;

    }


    void initReceiver(KeepAliveReceiver keepAliveReceiver) {
        if (keepAliveReceiver==null)
            throw new IllegalStateException("Wrong EventReceiver init");
        receiver = keepAliveReceiver;
    }


    /**
     * Возвращает состояние заряда батареи в процентах.
     *
     * */
    float getBatteryState(Context context, Intent intent) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {
            int level = (intent != null) ? intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) : 0;
            return (float) level;
        }

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

    void setInternalDozeModeListener(InternalDozeModeListener eventListener) {
        internalDozeModeListener= eventListener;
    }


    private  float  calculateBatteryPercentage(int level, int scale) {
        return Math.round((level / (float) scale) * 1000)/10f;
    }


}
