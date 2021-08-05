package c.ponom.survivalistapplication.lifekeeper;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import c.ponom.survivalistapplication.Application;

import static android.content.Intent.ACTION_BATTERY_CHANGED;
import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static android.content.Intent.ACTION_TIME_TICK;
import static android.os.PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED;
import static android.os.PowerManager.ACTION_POWER_SAVE_MODE_CHANGED;
import static c.ponom.survivalistapplication.Logger.registerBroadcastEvent;
import static c.ponom.survivalistapplication.Logger.registerInSkippedLogEvent;

public final class KeepAliveReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // todo тут ничего этого не будет в проде
        switch (intent.getAction()) {
            case ACTION_BOOT_COMPLETED:
                registerBroadcastEvent(" Rebooted, auto launched ");
                break;
            case ACTION_TIME_TICK:
                registerBroadcastEvent(" On Tick Event ");
                break;
            case ACTION_BATTERY_CHANGED:
                registerBroadcastEvent(" Battery event");
                break;
            case ACTION_POWER_SAVE_MODE_CHANGED:
            case ACTION_DEVICE_IDLE_MODE_CHANGED:
                registerBroadcastEvent(" Power mode "
                        + powerState());
                registerInSkippedLogEvent(" Power mode " + powerState());
        }

        LifeKeeper.getInstance().onIntentEvent(intent);
        LifeKeeper.getInstance().emitEvents();

    }

    private String powerState() {
        PowerManager pm = (PowerManager) Application.getAppContext().getSystemService(Context.POWER_SERVICE);
        return pm.isDeviceIdleMode() ? " doze  on " : " doze off ";
    }

}
