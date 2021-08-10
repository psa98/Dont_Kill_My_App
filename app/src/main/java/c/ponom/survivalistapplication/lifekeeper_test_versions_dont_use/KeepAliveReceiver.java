package c.ponom.survivalistapplication.lifekeeper_test_versions_dont_use;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static android.content.Intent.ACTION_BATTERY_CHANGED;
import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static android.content.Intent.ACTION_TIME_TICK;
import static android.os.PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED;
import static c.ponom.survivalistapplication.Logger.formattedTimeStamp;
import static c.ponom.survivalistapplication.Logger.registerBroadcastEvent;

public final class KeepAliveReceiver extends BroadcastReceiver {

    private volatile static KeepAliveReceiver INSTANCE;

    private final ReceiverEvents receiverEvents;


    public KeepAliveReceiver() {
        INSTANCE=this;
        receiverEvents = new ReceiverEvents();
        receiverEvents.initReceiver(this);
    }

    synchronized static KeepAliveReceiver getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new KeepAliveReceiver();
        }
        return INSTANCE;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case ACTION_BOOT_COMPLETED:

                registerBroadcastEvent(" Rebooted, auto launched ");
                break;
            case ACTION_TIME_TICK:
                if (receiverEvents.tickEventListener!=null)
                    receiverEvents.tickEventListener.onTickEvent();
                registerBroadcastEvent(" On Tick Event ");
                break;
            case ACTION_BATTERY_CHANGED:
                if (receiverEvents.batteryEventListener!=null)
                    receiverEvents.batteryEventListener.
                            onBatteryEvent(receiverEvents.getBatteryState(context));
                registerBroadcastEvent(" Battery event");
                break;
            case ACTION_DEVICE_IDLE_MODE_CHANGED:
                registerBroadcastEvent("\n"+ formattedTimeStamp()+ " Power mode "
                        + receiverEvents.getPowerStateString());
                if (receiverEvents.dozeEventListener!=null)
                    receiverEvents.dozeEventListener.
                            onDozeModeChange(receiverEvents.getDoseModeState(context));
        }
        LifeKeeper.getInstance().emitEvents();
    }
}
