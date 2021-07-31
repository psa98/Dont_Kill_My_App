package c.ponom.survivalistapplication;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static android.content.Intent.ACTION_TIME_TICK;


public class KeepAliveReceiver extends BroadcastReceiver {



    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case ACTION_BOOT_COMPLETED:
                App.registerBoot();
                break;
            case ACTION_TIME_TICK:
                App.tickCount++;
                App.registerTick();

        }

    }

}
