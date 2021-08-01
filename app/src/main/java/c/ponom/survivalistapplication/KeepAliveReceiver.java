package c.ponom.survivalistapplication;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;

import static android.content.Intent.ACTION_BATTERY_CHANGED;
import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static android.content.Intent.ACTION_TIME_TICK;
import static android.os.PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED;
import static android.os.PowerManager.ACTION_POWER_SAVE_MODE_CHANGED;

public class KeepAliveReceiver extends BroadcastReceiver {

// идея для регистрации ресивера, ловящего по возможности вообще все доступные бродкасты
// https://stackoverflow.com/questions/2403759/create-an-intentfilter-in-android-that-matches-all-intents

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case ACTION_BOOT_COMPLETED:
                App.registerBroadcastEvent("Rebooted ");
                break;
            case ACTION_TIME_TICK:
                App.registerBroadcastEvent(" On Tick Event ");
                break;
            case ACTION_BATTERY_CHANGED:
                App.registerBroadcastEvent(" Battery event");

                Bundle b = intent.getExtras();
                break;
            case ACTION_POWER_SAVE_MODE_CHANGED:
            case ACTION_DEVICE_IDLE_MODE_CHANGED:
                App.registerBroadcastEvent(" Power save mode changed:"
                        + powerState());
                App.registerInSkippedLogEvent(" Power save mode changed:" + powerState());
        }

    }

    private String powerState() {
        PowerManager pm = (PowerManager) App.getAppContext().getSystemService(Context.POWER_SERVICE);
        return pm.isPowerSaveMode() ? " power save  on " : " power save off " +
                (pm.isDeviceIdleMode() ? " doze  on " : " doze off ");
    }

}
