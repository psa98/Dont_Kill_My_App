package c.ponom.keep_alive_library;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;

import org.jetbrains.annotations.NotNull;

public class PowerStateUtils {

    public float getCurrentBatteryCharge(@NotNull Context context) {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, intentFilter);
        if (batteryStatus==null) return 0f;
        final int DefaultValue = 0;
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL,
                DefaultValue);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE,
                100);
        return  calculateBatteryPercentage(level, scale);
    }

    /**
     * Возвращает статус текущего режима экономии заряда.
     * В doze mode большинство бродкастов и воркеров заблокированы и не будут вызываться
     * за пределами "окон" предоставленных системой.
     * Ранее SDK 23 не поддерживается
     * */
    public boolean getDoseModeState(@NotNull Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return pm.isDeviceIdleMode();
        } else return false;
    }

    public boolean getPowerSaveMode(@NotNull Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return pm.isPowerSaveMode();
        else return false;

    }
    private  float  calculateBatteryPercentage(int level, int scale) {
        return Math.round((level / (float) scale) * 1000)/10f;
    }
}


