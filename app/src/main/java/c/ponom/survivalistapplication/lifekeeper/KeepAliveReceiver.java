package c.ponom.survivalistapplication.lifekeeper;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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

    private KeepAliveReceiver() {
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
                registerBroadcastEvent("\n"+ formattedTimeStamp()+ " Power mode "
                        + powerState());
                registerInSkippedLogEvent("\n Power mode " + powerState());
        }

        LifeKeeper.getInstance().emitEvents();

    }

    private String powerState() {
        PowerManager pm = (PowerManager) getAppContext().getSystemService(Context.POWER_SERVICE);
        return pm.isDeviceIdleMode() ? " doze  on \n" : " doze off \n";
    }

}
