package c.ponom.survivalistapplication;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import c.ponom.survivalistapplication.model.SharedPrefsRepository;

import static c.ponom.survivalistapplication.Application.TAG;
import static c.ponom.survivalistapplication.model.SharedPrefsRepository.DataType.LONG;
import static c.ponom.survivalistapplication.model.SharedPrefsRepository.DataType.STRING;
import static c.ponom.survivalistapplication.model.SharedPrefsRepository.getParameterLong;
import static c.ponom.survivalistapplication.model.SharedPrefsRepository.getParameterString;
import static c.ponom.survivalistapplication.model.SharedPrefsRepository.saveParameter;

public class Logger {


    private static final long EVENT_WAS_SKIPPED_TIME = 180;
    public static MutableLiveData<String> liveEventsList = new MutableLiveData<>();
    public static MutableLiveData<String> liveSkippedEventsList = new MutableLiveData<>();


    public static void registerBroadcastEvent(String intentTypeMessage) {
        String eventString = "\n" + formattedTimeStamp() + intentTypeMessage;
        appendEvent(eventString);
    }

    public static synchronized void appendEvent(String eventString) {
        String oldEventsList = getParameterString("events");
        if (oldEventsList.length() > 200000) oldEventsList =
                // режем лог - размер SP ограничен
                oldEventsList.substring(oldEventsList.length() - 10000);
        String logString = oldEventsList + eventString;
        saveParameter(logString, "events", STRING);
        liveEventsList.postValue(logString);
        testForSkippedEvents();
        Log.i(TAG, "LiveKeeper event:");
    }

    private static void testForSkippedEvents() {
        Date currentTimeDate = new Date();
        Date lastEventDate = new Date();
        if (SharedPrefsRepository.hasParameterSet("lastEvent")) {
            lastEventDate.setTime(getParameterLong("lastEvent"));
            SharedPrefsRepository.saveParameter(currentTimeDate.getTime(), "lastEvent", LONG);
        }
        long secondsBetween = currentTimeDate.getTime() / 1000 - lastEventDate.getTime() / 1000;
        if (secondsBetween > EVENT_WAS_SKIPPED_TIME) {
            String oldSkippedEventsList = getParameterString("skipped");
            String skippedEventDescription =
                    "No timer events registered between " +
                            formatDate(lastEventDate) +
                            " and  " +
                            formatDate(currentTimeDate) +
                            " for " + secondsBetween + "s \n";
            String skippedLogString = oldSkippedEventsList + skippedEventDescription;
            saveParameter(skippedLogString, "skipped", STRING);
            liveSkippedEventsList.postValue(skippedLogString);
        }
    }

    public static void registerWorkerEvent(String type) {
        String eventString = "\n" + formattedTimeStamp() + ", worker event " + type;
        appendEvent(eventString);

    }

    public static String formattedTimeStamp() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(new Date());
    }

    public static synchronized void registerInSkippedLogEvent(String event) {
        String oldSkippedEventsList = getParameterString("skipped");
        saveParameter(oldSkippedEventsList + event, "skipped", STRING);
    }


    private static String formatDate(Date date) {
        return new SimpleDateFormat("dd/MM  HH:mm:ss",
                Locale.getDefault()).format(date);
    }
}


