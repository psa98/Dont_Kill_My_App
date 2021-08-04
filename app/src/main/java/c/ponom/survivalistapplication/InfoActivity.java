package c.ponom.survivalistapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import java.util.Date;
import java.util.Locale;

import c.ponom.survivalistapplication.lifekeeper.LifeKeeper;
import c.ponom.survivalistapplication.model.SharedPrefsRepository;

import static c.ponom.survivalistapplication.model.SharedPrefsRepository.DataType.STRING;

@SuppressLint("SetTextI18n")
public class InfoActivity extends AppCompatActivity {

    EditText list, skippedList;
    boolean refreshingLogs = true;
    LiveData<Long> eventBus;
    private TextView lastEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = findViewById(R.id.logList);
        skippedList = findViewById(R.id.skippedList);
        lastEvent = findViewById(R.id.lastEvent);

        LiveData<String> eventList = Logger.liveEventsList;
        LiveData<String> skippedEventList = Logger.liveSkippedEventsList;
        eventList.observe(this, newList -> {
            if (refreshingLogs) showAndScrollToEnd(newList);
        });
        skippedEventList.observe(this, newList -> showSkippedAndScrollToEnd(newList));
        eventBus = LifeKeeper.getInstance().subscribeOnEvents();
        eventBus.observe(this, value -> {
            if (value == null) return;
            Date date = new Date();
            date.setTime(value);
            lastEvent.setText(String.format(Locale.getDefault(), "Last event%tT", date));
        });

    }

    private void showAndScrollToEnd(String newList) {
        list.setText(newList);
        if (!newList.isEmpty()) list.setSelection(newList.length() - 1, newList.length());
        list.clearFocus(); // костыль для скролла - выделить посл.символ, убрать выделение
    }

    private void showSkippedAndScrollToEnd(String newList) {
        skippedList.setText(newList);
        if (!newList.isEmpty())
            skippedList.setSelection(newList.length() - 2, newList.length() - 1);
        skippedList.clearFocus();
    }


    public synchronized void clearEvents(View view) {
        SharedPrefsRepository.saveParameter("", "events", STRING);
        SharedPrefsRepository.saveParameter("", "skipped", STRING);
        list.setText("");
        skippedList.setText("");
    }


    public void checkBoxClicked(View view) {
        CheckBox checkBox = (CheckBox) view;
        refreshingLogs = checkBox.isChecked();
    }
}