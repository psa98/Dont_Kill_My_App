package c.ponom.survivalistapplication;

import static java.util.Locale.getDefault;
import static c.ponom.survivalistapplication.Logger.refreshLists;
import static c.ponom.survivalistapplication.model.SharedPrefsDAO.DataType.STRING;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import java.util.Date;

import c.ponom.keep_alive_library.LifeKeeperAPI;
import c.ponom.survivalistapplication.model.SharedPrefsDAO;

@SuppressLint("SetTextI18n")
public class InfoActivity extends AppCompatActivity {

    private EditText list, skippedList;
    private boolean refreshingLogs = true;
    private TextView lastEvent;
    private LiveData<Long> allEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        list = findViewById(R.id.logList);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            list.setShowSoftInputOnFocus(false);
        skippedList = findViewById(R.id.skippedList);
        lastEvent = findViewById(R.id.lastEvent);
        LiveData<String> eventList = Logger.liveEventsList;
        LiveData<String> skippedEventList = Logger.liveSkippedEventsList;
        eventList.observe(this, newList -> {
            if (refreshingLogs) showAndScrollToEnd(newList);
        });
        skippedEventList.observe(this, newList -> showSkippedAndScrollToEnd(newList));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLists();
        allEvents = LifeKeeperAPI.subscribeOnAllEvents();
        allEvents.observe(this, value -> {
            if (value != null) {
                Date date = new Date();
                date.setTime(value);
                lastEvent.setText(String.format(getDefault(), "Last event %tT", date));
            }
        });
    }

    private void showAndScrollToEnd(String newList) {
        list.setText(newList);
        if (!newList.isEmpty()) list.setSelection(newList.length() - 1, newList.length());
        list.clearFocus();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LifeKeeperAPI.unsubscribeEvents(allEvents);
    }

    private void showSkippedAndScrollToEnd(String newList) {
        skippedList.setText(newList);
        if (!newList.isEmpty())
            skippedList.setSelection(newList.length() - 1, newList.length());
        skippedList.clearFocus();
    }


    public synchronized void clearEvents(View view) {
        SharedPrefsDAO.saveParameter("", "events", STRING);
        SharedPrefsDAO.saveParameter("", "skipped", STRING);
        list.setText("");
        skippedList.setText("");
    }


    public void checkBoxClicked(View view) {
        refreshingLogs = ((CheckBox) view).isChecked();
    }
}