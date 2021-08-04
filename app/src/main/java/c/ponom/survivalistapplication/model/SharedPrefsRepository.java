package c.ponom.survivalistapplication.model;

import android.annotation.SuppressLint;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

import static c.ponom.survivalistapplication.Application.getSharedPreferences;


@SuppressWarnings({"unused", "RedundantSuppression"})
@SuppressLint("ApplySharedPref")
public class SharedPrefsRepository {


    static public synchronized boolean hasParameterSet(String key) {
        return getSharedPreferences().contains(key);
    }

    public static synchronized Map<String, ?> getPreferencesAsMap() {

        Map<String, ?> map = getSharedPreferences().getAll();
        return Collections.unmodifiableMap(map);
    }

    @SuppressWarnings("FieldCanBeLocal")
    public enum DataType {

        BOOLEAN(false, Boolean.TYPE),
        FLOAT(0f, Float.TYPE),
        INT(0, Integer.TYPE),
        LONG(0L, Long.TYPE),
        STRING("", String.class);

        private final Object defaultValue;
        private final Type type;

        DataType(Object defaultValue, Type type) {
            this.defaultValue = defaultValue;
            this.type = type;
        }
    }

    // цель всего этого - обеспечить обязательное указание вида параметра в методе
    // сохранения и fail fast контроль за его типом. При этом обеспечивается единая точка входа
    // для всех параметров

    public static synchronized void  saveParameter(Object parameter, String key, DataType parameterType){

        switch (parameterType) {
            case  BOOLEAN:
                getSharedPreferences().edit().putBoolean(key, (boolean)parameter).commit();
                break;
            case FLOAT:
                getSharedPreferences().edit().putFloat(key, (float) parameter).commit();
                break;
            case  LONG:
                getSharedPreferences().edit().putLong(key, (long)parameter).commit();
                break;
            case  INT:
                getSharedPreferences().edit().putInt(key, (int)parameter).commit();
                break;
           case  STRING:
               getSharedPreferences().edit().putString(key, (String)parameter).commit();
        }
    }

    static  public synchronized long  getParameterLong (String key){
        return getSharedPreferences().getLong(key, (long) DataType.LONG.defaultValue);
    }

    static  public synchronized int getParameterInt (String key){
        return getSharedPreferences().getInt(key, (int) DataType.INT.defaultValue);
    }

    static  public synchronized boolean getParameterBoolean (String key){
        return getSharedPreferences().getBoolean(key, (boolean) DataType.BOOLEAN.defaultValue);
    }

    static  public synchronized String getParameterString (String key){
        return getSharedPreferences().getString(key, (String) DataType.STRING.defaultValue);
    }

    static  public synchronized float getParameterFloat (String key){
        return getSharedPreferences().getFloat(key, (float) DataType.FLOAT.defaultValue);
    }


}
