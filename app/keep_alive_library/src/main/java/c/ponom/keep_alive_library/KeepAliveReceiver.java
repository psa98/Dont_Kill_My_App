package c.ponom.keep_alive_library;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static android.content.Intent.ACTION_BATTERY_CHANGED;
import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static android.content.Intent.ACTION_TIME_TICK;
import static android.os.PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED;
import static android.os.PowerManager.ACTION_POWER_SAVE_MODE_CHANGED;

@SuppressWarnings("unused")
public final class KeepAliveReceiver extends BroadcastReceiver {

    private volatile static KeepAliveReceiver INSTANCE;
    private final EventReceiver eventReceiver;


    public KeepAliveReceiver() {
        INSTANCE=this;
        eventReceiver = new EventReceiver();
        eventReceiver.initReceiver(this);
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

                break;
            case ACTION_TIME_TICK:
                if (eventReceiver.tickEventListener!=null)
                    eventReceiver.tickEventListener.onTickEvent();
                break;
            case ACTION_BATTERY_CHANGED:
                if (eventReceiver.batteryEventListener!=null)
                    eventReceiver.batteryEventListener.
                            onBatteryEvent(eventReceiver.getBatteryState(context,intent));
                break;
            case   ACTION_DEVICE_IDLE_MODE_CHANGED:
                final boolean doseModeState = eventReceiver.getDoseModeState(context);
                if (eventReceiver.dozeEventListener!=null)
                    eventReceiver.dozeEventListener.
                            onDozeModeChange(doseModeState);
                if (eventReceiver.internalDozeModeListener!=null)
                    eventReceiver.internalDozeModeListener.
                            onDozeModeChangeInternal(context, doseModeState);
                break;
            case   ACTION_POWER_SAVE_MODE_CHANGED:
                final boolean powerSaveState = eventReceiver.getPowerSaveMode(context);

        }
        LifeKeeper.getInstance().emitEvents();
    }
}
