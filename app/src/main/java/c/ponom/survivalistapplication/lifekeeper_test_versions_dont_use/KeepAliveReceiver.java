package c.ponom.survivalistapplication.lifekeeper_test_versions_dont_use;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.PowerManager;

import static android.content.Intent.ACTION_BATTERY_CHANGED;
import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static android.content.Intent.ACTION_TIME_TICK;
import static android.os.PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED;
import static android.os.PowerManager.ACTION_POWER_SAVE_MODE_CHANGED;
import static c.ponom.survivalistapplication.Application.getAppContext;
import static c.ponom.survivalistapplication.Logger.formattedTimeStamp;
import static c.ponom.survivalistapplication.Logger.registerBroadcastEvent;
import static c.ponom.survivalistapplication.Logger.registerInSkippedLogEvent;

public final class KeepAliveReceiver extends BroadcastReceiver {

    private volatile static KeepAliveReceiver INSTANCE;
    private DozeModeListener dozeEventListener;
    private RebootEventListener rebootEventListener;
    private BatteryEventListener batteryEventListener;
    private TickEventListener tickEventListener;
    private final ReceiverMediator receiverMediator;


    public KeepAliveReceiver() {
        //это требуется для возможности старта ресивера системой, так что сделать его
        // private как в классическом синглтоне нельзя. В принципе в итоге экземпляр будет
        // создан уже при ребуте
        INSTANCE=this;
        receiverMediator = new ReceiverMediator();
        receiverMediator.initReceiver(this);

    }

    public synchronized static KeepAliveReceiver getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new KeepAliveReceiver();

        }
        return INSTANCE;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        // todo тут ничего этого не будет в проде будет одно событие
        switch (intent.getAction()) {
            case ACTION_BOOT_COMPLETED:
                if (rebootEventListener!=null) rebootEventListener.onReboot();
                registerBroadcastEvent(" Rebooted, auto launched ");
                break;
            case ACTION_TIME_TICK:
                if (tickEventListener!=null) tickEventListener.onTickEvent();
                registerBroadcastEvent(" On Tick Event ");
                break;
            case ACTION_BATTERY_CHANGED:
                if (batteryEventListener!=null) batteryEventListener.onBatteryEvent(getBatteryState());
                registerBroadcastEvent(" Battery event");
                break;
            case ACTION_POWER_SAVE_MODE_CHANGED:
                ///
                break;
            case ACTION_DEVICE_IDLE_MODE_CHANGED:
                registerBroadcastEvent("\n"+ formattedTimeStamp()+ " Power mode "
                        + getPowerStateString());
                registerInSkippedLogEvent("\n Power mode " + getPowerStateString());
                if (dozeEventListener!=null)  dozeEventListener.onDozeModeChange(getDoseModeState());

        }

        LifeKeeper.getInstance().emitEvents();

    }

    private String getPowerStateString() {
        PowerManager pm = (PowerManager) getAppContext().getSystemService(Context.POWER_SERVICE);
        return pm.isDeviceIdleMode() ? "doze  mode on" : "doze mode off";
    }

    public boolean getDoseModeState() {
        PowerManager pm = (PowerManager) getAppContext().getSystemService(Context.POWER_SERVICE);
        return pm.isDeviceIdleMode();
    }


    public int getBatteryState() {
        BatteryManager bm = (BatteryManager) getAppContext().getSystemService(Context.BATTERY_SERVICE);
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }

    public synchronized  void setDozeModeListener(DozeModeListener eventListener) {
        dozeEventListener = eventListener;
    }

    public synchronized  void setRebootListener(RebootEventListener eventListener) {
        rebootEventListener = eventListener;
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

    public interface RebootEventListener {
        void  onReboot();
    }
    public interface TickEventListener {
        void  onTickEvent();
    }


    interface Connection {
        void getConnection(KeepAliveReceiver receiver);
    }

}
