package c.ponom.survivalistapplication;

import static c.ponom.survivalistapplication.Application.TAG;
import static c.ponom.survivalistapplication.Application.debugMode;
import static c.ponom.survivalistapplication.model.SharedPrefsDAO.DataType.LONG;
import static c.ponom.survivalistapplication.model.SharedPrefsDAO.DataType.STRING;
import static c.ponom.survivalistapplication.model.SharedPrefsDAO.getParameterLong;
import static c.ponom.survivalistapplication.model.SharedPrefsDAO.getParameterString;
import static c.ponom.survivalistapplication.model.SharedPrefsDAO.saveParameter;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import c.ponom.survivalistapplication.model.SharedPrefsDAO;

public class Logger {


    private static final long EVENT_WAS_SKIPPED_TIME = 240;
    static final MutableLiveData<String> liveEventsList = new MutableLiveData<>();
    static final MutableLiveData<String> liveSkippedEventsList = new MutableLiveData<>();


    public static synchronized void appendEvent(String eventString) {
        String oldEventsList = getParameterString("events");
        if (oldEventsList.length() > 200000) oldEventsList =
                // режем лог - размер SP ограничен примерно парой мегабайт оказывается
                oldEventsList.substring(oldEventsList.length() - 30000);
        String logString = oldEventsList + eventString;
        saveParameter(logString, "events", STRING);
        liveEventsList.postValue(logString);
        testForSkippedEvents();
        if (debugMode)Log.i(TAG, "LiveKeeper event:");
    }

    private static void testForSkippedEvents() {
        Date currentTimeDate = new Date();
        Date lastEventDate = new Date();
        if (SharedPrefsDAO.hasParameterSet("lastEvent")) {
            lastEventDate.setTime(getParameterLong("lastEvent"));
            if (debugMode) Log.e(TAG, "testForSkippedEvents: "+ lastEventDate +" / \n "+currentTimeDate );
         }
        SharedPrefsDAO.saveParameter(currentTimeDate.getTime(), "lastEvent", LONG);
        long secondsBetween = currentTimeDate.getTime() / 1000 - lastEventDate.getTime() / 1000;
        if (debugMode) Log.i(TAG, "testForSkippedEvents: seconds "+ secondsBetween);
        if (secondsBetween > EVENT_WAS_SKIPPED_TIME) {
            String oldSkippedEventsList = getParameterString("skipped");
            String skippedEventDescription =
                    "\nNo timer events registered between " +
                            formatDate(lastEventDate) +
                            " and  " +
                            formatDate(currentTimeDate) +
                            " for " + secondsBetween + "s";
            String skippedLogString = oldSkippedEventsList + skippedEventDescription;
            if (skippedLogString.length() > 200000) skippedLogString =
                    // режем лог - размер SP ограничен примерно парой мегабайт оказывается
                    skippedLogString.substring(skippedLogString.length() - 30000);
            saveParameter(skippedLogString, "skipped", STRING);
            liveSkippedEventsList.postValue(skippedLogString);
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

}


