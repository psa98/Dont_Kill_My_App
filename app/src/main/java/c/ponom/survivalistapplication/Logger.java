package c.ponom.survivalistapplication;

import static java.text.DateFormat.MEDIUM;
import static c.ponom.survivalistapplication.App.TAG;
import static c.ponom.survivalistapplication.App.debugMode;
import static c.ponom.survivalistapplication.model.SharedPrefsDAO.DataType.LONG;
import static c.ponom.survivalistapplication.model.SharedPrefsDAO.DataType.STRING;
import static c.ponom.survivalistapplication.model.SharedPrefsDAO.getParameterLong;
import static c.ponom.survivalistapplication.model.SharedPrefsDAO.getParameterString;
import static c.ponom.survivalistapplication.model.SharedPrefsDAO.saveParameter;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import c.ponom.survivalistapplication.model.SharedPrefsDAO;

public class Logger {


    private static final long EVENT_WAS_SKIPPED_TIME = 360;
    private static final int MAX_LOG_SIZE = 500000;
    private  static final int MIN_LOG_SIZE = 300000;

    static final MutableLiveData<String> liveEventsList = new MutableLiveData<>();
    static final MutableLiveData<String> liveSkippedEventsList = new MutableLiveData<>();


    public static synchronized void appendEvent(String eventString) {
        String oldEventsList = getParameterString("events");
        if (oldEventsList.length() > MAX_LOG_SIZE) oldEventsList =
                // режем лог - размер SP ограничен примерно парой мегабайт оказывается
                oldEventsList.substring(oldEventsList.length() - MIN_LOG_SIZE);
        String logString = oldEventsList + eventString;
        saveParameter(logString, "events", STRING);
        liveEventsList.postValue(logString);
        testForSkippedEvents();
        if (debugMode)Log.i(TAG, "LiveKeeper event:"+eventString);
    }

    private static void testForSkippedEvents() {
        Date currentTimeDate = new Date();
        Date lastEventDate = new Date();
        if (SharedPrefsDAO.hasParameterSet("lastEvent"))
            lastEventDate.setTime(getParameterLong("lastEvent"));
        final long timeNow = currentTimeDate.getTime();
        SharedPrefsDAO.saveParameter(timeNow, "lastEvent", LONG);
        long secondsBetween = timeNow / 1000 - lastEventDate.getTime() / 1000;
        if (debugMode) Log.i(TAG, "testForSkippedEvents: seconds "+ secondsBetween);
        if (secondsBetween > EVENT_WAS_SKIPPED_TIME) {
            String oldSkippedEventsList = getParameterString("skipped");
            String skippedEventDescription =
                    "\nNo timer events registered between " +
                            formatDate(lastEventDate) +
                            " and  " +
                            formatDate(currentTimeDate) +
                            " for " +
                            calculatePeriodString(lastEventDate.getTime(),currentTimeDate.getTime());
            if (debugMode)Log.i(TAG, "LiveKeeper event:"+skippedEventDescription);
            String appendedLog = oldSkippedEventsList + skippedEventDescription;

            if (appendedLog.length() > MAX_LOG_SIZE) {
                appendedLog =appendedLog.substring(appendedLog.length() - MIN_LOG_SIZE);
            }
                    // режем лог - размер SP ограничен
            saveParameter(appendedLog, "skipped", STRING);
            liveSkippedEventsList.postValue(appendedLog);
        }
    }


    public static String formattedTimeStamp() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(new Date());
    }

    public static synchronized void registerInSkippedLogEvent(String event) {
        String oldSkippedEventsList = getParameterString("skipped");
        saveParameter(oldSkippedEventsList + event, "skipped", STRING);
        liveSkippedEventsList.postValue(oldSkippedEventsList + event);
    }


    private static String formatDate(Date date) {
        return new SimpleDateFormat("dd/MM HH:mm:ss",
                Locale.getDefault()).format(date);
    }

    public static void refreshLists() {
        String oldEventsList = getParameterString("events");
        liveEventsList.postValue(oldEventsList);
        String skippedLogString = getParameterString("skipped");
        liveSkippedEventsList.postValue(skippedLogString);
    }



    @NonNull
    private static String calculatePeriodString(long timeTo, long timeFrom) {
        int offset= TimeZone.getDefault().getOffset(timeTo);
        DateFormat format= DateFormat.getTimeInstance(MEDIUM);
        long days = (timeTo - timeFrom - offset)/(24*3600*1000);
        long rest = (timeTo - timeFrom - offset)%(24*3600*1000);
        return days+" d " + format.format(new Date(rest)) + " h";
    }


}


