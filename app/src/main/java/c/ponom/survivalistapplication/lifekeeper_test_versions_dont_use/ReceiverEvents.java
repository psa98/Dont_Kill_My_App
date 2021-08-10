package c.ponom.survivalistapplication.lifekeeper_test_versions_dont_use;

import android.content.Context;
import android.os.BatteryManager;
import android.os.PowerManager;

import static c.ponom.survivalistapplication.Application.getAppContext;

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
        receiver = keepAliveReceiver;
    }

    public synchronized static ReceiverEvents getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ReceiverEvents();
        }
        return INSTANCE;
    }


    protected String getPowerStateString() {
        PowerManager pm = (PowerManager) getAppContext().getSystemService(Context.POWER_SERVICE);
        return pm.isDeviceIdleMode() ? "doze  mode on" : "doze mode off";
    }

    public boolean getDoseModeState(Context context) {
        PowerManager pm = (PowerManager) getAppContext().getSystemService(Context.POWER_SERVICE);
        return pm.isDeviceIdleMode();
    }


    public int getBatteryState(Context context) {
        BatteryManager bm = (BatteryManager) getAppContext().getSystemService(Context.BATTERY_SERVICE);
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
