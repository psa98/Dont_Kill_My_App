package c.ponom.keep_alive_library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;


@SuppressWarnings({"unused", "RedundantSuppression"})
@SuppressLint("ApplySharedPref")
public class SharedPreferencesRepository {

    private SharedPreferences sharedPreferences;

    // цель класса - обеспечить обязательное указание вида параметра в методе
    // сохранения и fail fast контроль за его типом. При этом обеспечивается единая точка входа
    // для сохранения всех параметров

    public SharedPreferencesRepository(Context context) {
        sharedPreferences = context.getSharedPreferences("live_keeper_library",Context.MODE_PRIVATE);
    }

    private SharedPreferencesRepository() {

    }
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
                sharedPreferences.edit().putBoolean(key, (boolean)parameter).commit();
                break;
            case FLOAT:
                sharedPreferences.edit().putFloat(key, (float) parameter).commit();
                break;
            case  LONG:
                sharedPreferences.edit().putLong(key, (long)parameter).commit();
                break;
            case  INT:
                sharedPreferences.edit().putInt(key, (int)parameter).commit();
                break;
            case  STRING:
                sharedPreferences.edit().putString(key, (String)parameter).commit();
        }
    }

    public synchronized long  getParameterLong (String key){
        return sharedPreferences.getLong(key, (long) DataType.LONG.defaultValue);
    }

    public synchronized int getParameterInt (String key){
        return sharedPreferences.getInt(key, (int) DataType.INT.defaultValue);
    }

    public synchronized boolean getParameterBoolean (String key){
        return sharedPreferences.getBoolean(key, (boolean) DataType.BOOLEAN.defaultValue);
    }

    public synchronized String getParameterString (String key){
        return sharedPreferences.getString(key, (String) DataType.STRING.defaultValue);
    }

    public synchronized float getParameterFloat (String key){
        return sharedPreferences.getFloat(key, (float) DataType.FLOAT.defaultValue);
    }
}
