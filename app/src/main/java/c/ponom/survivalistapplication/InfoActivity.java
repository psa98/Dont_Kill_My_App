package c.ponom.survivalistapplication;

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
import java.util.Locale;

import c.ponom.keep_alive_library.LifeKeeper;
import c.ponom.survivalistapplication.model.SharedPrefsRepository;

import static c.ponom.survivalistapplication.model.SharedPrefsRepository.DataType.STRING;

@SuppressLint("SetTextI18n")
public class InfoActivity extends AppCompatActivity {

    private EditText list, skippedList;
    private boolean refreshingLogs = true;
    private TextView lastEvent;
    private LiveData<Long> eventBus;
    private final LifeKeeper lifeKeeper=LifeKeeper.getInstance();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = findViewById(R.id.logList);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            list.setShowSoftInputOnFocus(false);
        }
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
        Logger.refreshLists();
        eventBus = lifeKeeper.subscribeOnAllEvents();
        eventBus.observe(this, value -> {
            if (value == null) return;
            Date date = new Date();
            date.setTime(value);
            lastEvent.setText(String.format(Locale.getDefault(), "Last event %tT", date));
        });
    }

    private void showAndScrollToEnd(String newList) {
        list.setText(newList);
        if (!newList.isEmpty()) list.setSelection(newList.length() - 1, newList.length());
        list.clearFocus(); // костыль для скролла - выделить посл.символ, убрать выделение
    }

    @Override
    protected void onStop() {
        super.onStop();
        lifeKeeper.unsubscribeEvents(eventBus);
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