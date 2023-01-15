package c.ponom.survivalistapplication.model;

import static c.ponom.survivalistapplication.App.getSharedPreferences;

import android.annotation.SuppressLint;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;


@SuppressWarnings({"unused"})
@SuppressLint("ApplySharedPref")
public class SharedPrefsDAO {

    /* цель класса - обеспечить обязательное указание вида параметра в методе
     сохранения и fail fast контроль за его типом. При этом обеспечивается единая точка входа
     для сохранения всех типов  параметров
    */

    public static synchronized boolean hasParameterSet(String key) {
        return getSharedPreferences().contains(key);
    }

    public static synchronized Map<String, ?> getPreferencesAsMap() {

        Map<String, ?> map = getSharedPreferences().getAll();
        return Collections.unmodifiableMap(map);
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

    public static synchronized boolean  contains(String key){
        return getSharedPreferences().contains(key);
    }


    public static synchronized void  saveParameter(Object parameter, String key, DataType type){

        switch (type) {
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

    static  public synchronized long  getParameterLong (String key, long defValue){
        return getSharedPreferences().getLong(key, defValue);
    }

    static  public synchronized int getParameterInt (String key, int defValue){
        return getSharedPreferences().getInt(key, defValue);
    }

    static  public synchronized boolean getParameterBoolean (String key,boolean defValue){
        return getSharedPreferences().getBoolean(key, defValue);
    }

    static  public synchronized String getParameterString (String key,String defValue){
        return getSharedPreferences().getString(key, defValue);
    }

    static  public synchronized float getParameterFloat (String key, float defValue){
        return getSharedPreferences().getFloat(key, defValue);
    }

}
