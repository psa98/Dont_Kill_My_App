package c.ponom.keep_alive_library;

import static c.ponom.keep_alive_library.SharedPreferencesRepository.DataType.BOOLEAN;
import static c.ponom.keep_alive_library.SharedPreferencesRepository.DataType.FLOAT;
import static c.ponom.keep_alive_library.SharedPreferencesRepository.DataType.INT;
import static c.ponom.keep_alive_library.SharedPreferencesRepository.DataType.LONG;
import static c.ponom.keep_alive_library.SharedPreferencesRepository.DataType.STRING;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;


@SuppressWarnings({"unused", "RedundantSuppression"})

public class SharedPreferencesRepository {

    private SharedPreferences sharedPreferences;

    /**
    * Синхронизированная обертка поверх SP
    * Цель класса - обеспечить обязательное указание вида параметра в методе
    * сохранения и fail fast контроль за его типом.
    * При этом обеспечивается единая точка входа для сохранения всех параметров
    */

    public SharedPreferencesRepository(Context context, String name) {
        sharedPreferences = context.getSharedPreferences(name,Context.MODE_PRIVATE);
    }

    private SharedPreferencesRepository() {}

    public enum DataType {
        BOOLEAN(false, Boolean.TYPE),
        FLOAT(0f, Float.TYPE),
        INT(0, Integer.TYPE),
        LONG(0L, Long.TYPE),
        STRING("", String.class);
        private final Object defaultValue;
        DataType(Object defaultValue, Type type) {
            this.defaultValue = defaultValue;
        }
    }

    public synchronized boolean hasParameterSet(String key) {
        return sharedPreferences.contains(key);
    }

    public synchronized Map<String, ?> getPreferencesAsMap() {
        Map<String, ?> map = sharedPreferences.getAll();
        return Collections.unmodifiableMap(map);
    }

    public synchronized void  saveParameter(Object parameter, String key, DataType parameterType){

        switch (parameterType) {
            case  BOOLEAN:
                sharedPreferences.edit().putBoolean(key, (boolean)parameter).apply();
                break;
            case FLOAT:
                sharedPreferences.edit().putFloat(key, (float) parameter).apply();
                break;
            case  LONG:
                sharedPreferences.edit().putLong(key, (long)parameter).apply();
                break;
            case  INT:
                sharedPreferences.edit().putInt(key, (int)parameter).apply();
                break;
            case  STRING:
                sharedPreferences.edit().putString(key, (String)parameter).apply();
        }
    }

    public synchronized long  getParameterLong (String key){
        return sharedPreferences.getLong(key, (long) LONG.defaultValue);
    }

    public synchronized int getParameterInt (String key){
        return sharedPreferences.getInt(key, (int) INT.defaultValue);
    }

    public synchronized boolean getParameterBoolean (String key){
        return sharedPreferences.getBoolean(key, (boolean) BOOLEAN.defaultValue);
    }

    @NotNull
    public synchronized String getParameterString (String key){
        return sharedPreferences.getString(key, (String) STRING.defaultValue);
    }

    public synchronized float getParameterFloat (String key){
        return sharedPreferences.getFloat(key, (float) FLOAT.defaultValue);
    }

    @Nullable
    public synchronized Long  getParameterLongOrNull (String key){
        if (sharedPreferences.contains(key))
            return sharedPreferences.getLong(key, (long) LONG.defaultValue);
        else return null;
    }

    @Nullable
    public synchronized Integer getParameterIntOrNull (String key){
        if (sharedPreferences.contains(key)) {
            return sharedPreferences.getInt(key, (int) INT.defaultValue);
        }
        else return null;
    }

    @Nullable
    public synchronized Boolean getParameterBooleanOrNull (String key){
        if (sharedPreferences.contains(key))
            return sharedPreferences.getBoolean(key, (boolean) BOOLEAN.defaultValue);
        else return null;
    }

    @Nullable
    public synchronized String getParameterStringOrNull (String key){
        if (sharedPreferences.contains(key))
            return sharedPreferences.getString(key, (String) STRING.defaultValue);
        else return null;
    }

    @Nullable
    public synchronized Float getParameterFloatOrNull (String key){
        if (sharedPreferences.contains(key))
            return sharedPreferences.getFloat(key, (float) FLOAT.defaultValue);
        else return null;
    }
}
