package c.ponom.keep_alive_library;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.PowerManager;

import static android.content.Intent.ACTION_BATTERY_CHANGED;
import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static android.content.Intent.ACTION_TIME_TICK;
import static android.os.PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED;

@SuppressWarnings("unused")
public final class KeepAliveReceiver extends BroadcastReceiver {

    private volatile static KeepAliveReceiver INSTANCE;
    private DozeModeListener dozeEventListener;
    private BatteryEventListener batteryEventListener;
    private TickEventListener tickEventListener;


    public KeepAliveReceiver() {
        //это требуется для возможности старта ресивера системой, так что сделать его
        // private как в классическом синглтоне нельзя. В принципе в итоге экземпляр будет
        // создан уже при ребуте если имеется соответствующее разрешение
        INSTANCE=this;
    }

    public synchronized static KeepAliveReceiver getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new KeepAliveReceiver();

        }
        return INSTANCE;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        //мы контекст получаем при первом же событии, так что NPE ожидать не следует
        switch (intent.getAction()) {
            case ACTION_BOOT_COMPLETED:
                //
                break;
            case ACTION_TIME_TICK:
                if (tickEventListener!=null) tickEventListener.onTickEvent();
                break;
            case ACTION_BATTERY_CHANGED:
                if (batteryEventListener!=null) batteryEventListener.
                        onBatteryEvent(getBatteryState(context));
                break;
            case   ACTION_DEVICE_IDLE_MODE_CHANGED:
                if (dozeEventListener!=null)  dozeEventListener.
                        onDozeModeChange(getDoseModeState(context));

        }
        LifeKeeper.getInstance().emitEvents();
    }


    private boolean getDoseModeState(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return pm.isDeviceIdleMode();
    }


    private int getBatteryState(Context context) {
        BatteryManager bm = (BatteryManager)  context.getSystemService(Context.BATTERY_SERVICE);
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }

    public synchronized  void setDozeModeListener(DozeModeListener eventListener) {
        dozeEventListener = eventListener;
    }

     public synchronized  void setBatteryEventListener(BatteryEventListener eventListener) {
        batteryEventListener = eventListener;
    }

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
