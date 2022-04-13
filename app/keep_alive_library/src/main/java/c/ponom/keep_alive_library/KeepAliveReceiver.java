package c.ponom.keep_alive_library;


import static android.content.Intent.ACTION_BATTERY_CHANGED;
import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static android.content.Intent.ACTION_SCREEN_ON;
import static android.content.Intent.ACTION_TIME_TICK;
import static android.content.Intent.ACTION_USER_PRESENT;
import static android.os.PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED;
import static android.os.PowerManager.ACTION_POWER_SAVE_MODE_CHANGED;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

@SuppressWarnings("unused")
public final class KeepAliveReceiver extends BroadcastReceiver {

    private volatile static KeepAliveReceiver INSTANCE;
    private final EventReceiver eventReceiver;
    private final PowerStateUtils powerStateUtils =new PowerStateUtils();

    // должен быть публичным и без параметров, поскольку бродкаст ресивер создается самой системой
    // и одновременно должен быть синглтоном, поскольку экземпляр создается один и доступ к
    // нему надо получить.
    public KeepAliveReceiver() {
        INSTANCE=this;
        eventReceiver = new EventReceiver();
    }

    synchronized static KeepAliveReceiver getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new KeepAliveReceiver();
        }
        return INSTANCE;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final LifeKeeper lifeKeeper = LifeKeeper.getInstance();
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
                            onBatteryEvent(powerStateUtils.getCurrentBatteryCharge(context));
                break;
            case   ACTION_DEVICE_IDLE_MODE_CHANGED:
                final boolean doseModeState = powerStateUtils.getDoseModeState(context);
                if (eventReceiver.dozeEventListener!=null)
                    eventReceiver.dozeEventListener.
                            onDozeModeChange(doseModeState);
                if (eventReceiver.internalDozeModeListener!=null)
                    eventReceiver.internalDozeModeListener.
                            onDozeModeChangeInternal(context, doseModeState);
                break;
            case   ACTION_POWER_SAVE_MODE_CHANGED:
                final boolean powerSaveState = powerStateUtils.getPowerSaveMode(context);
            case ACTION_USER_PRESENT:
            case ACTION_SCREEN_ON:
                lifeKeeper.userPresentEvent();
        }
        lifeKeeper.emitEvents();
    }
}
