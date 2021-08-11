package c.ponom.survivalistapplication.lifekeeper_test_versions_dont_use;

import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;

public class ReceiverEvents {
    DozeModeListener dozeEventListener;
    BatteryEventListener batteryEventListener;
    TickEventListener tickEventListener;
    private volatile static ReceiverEvents INSTANCE;

    static KeepAliveReceiver receiver;
    ReceiverEvents() {
        INSTANCE=this;
    }

    void initReceiver(KeepAliveReceiver  keepAliveReceiver) {
        if (keepAliveReceiver==null)
            throw new IllegalStateException("Wrong EventReceiver init");
        receiver = keepAliveReceiver;
    }

    public synchronized static ReceiverEvents getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ReceiverEvents();
        }
        return INSTANCE;
    }




    public boolean getDoseModeState(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return pm.isDeviceIdleMode();
        } else return false;
    }


    public int getBatteryState(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);

            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {
            //noinspection UnnecessaryLocalVariable
            int level = (intent != null) ? intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) : 0;
            return level;
        }
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
