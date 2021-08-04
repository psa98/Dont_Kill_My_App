package c.ponom.survivalistapplication.lifekeeper;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import c.ponom.survivalistapplication.App;

import static android.content.Intent.ACTION_BATTERY_CHANGED;
import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static android.content.Intent.ACTION_TIME_TICK;
import static android.os.PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED;
import static android.os.PowerManager.ACTION_POWER_SAVE_MODE_CHANGED;
import static c.ponom.survivalistapplication.Logger.registerBroadcastEvent;
import static c.ponom.survivalistapplication.Logger.registerInSkippedLogEvent;

public class KeepAliveReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case ACTION_BOOT_COMPLETED:
                registerBroadcastEvent(" Rebooted, auto launched ");
                Log.i("!!! DON'T KILL", "Don't kill my app - restarted after");
                break;
            case ACTION_TIME_TICK:
                registerBroadcastEvent(" On Tick Event ");
                break;
            case ACTION_BATTERY_CHANGED:
                registerBroadcastEvent(" Battery event");
                break;
            case ACTION_POWER_SAVE_MODE_CHANGED:
            case ACTION_DEVICE_IDLE_MODE_CHANGED:
                registerBroadcastEvent(" Power save mode changed:"
                        + powerState());
                registerInSkippedLogEvent(" Power mode " + powerState());
        }

        LifeKeeper.getInstance().onIntentEvent(intent);
        LifeKeeper.getInstance().emitEvents();

    }

    private String powerState() {
        PowerManager pm = (PowerManager) App.getAppContext().getSystemService(Context.POWER_SERVICE);
        return pm.isDeviceIdleMode() ? " doze  on " : " doze off ";
    }

}
