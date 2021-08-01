package c.ponom.survivalistapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import static c.ponom.survivalistapplication.SharedPrefsRepository.DataType.STRING;

public class InfoActivity extends AppCompatActivity {

    EditText list, skippedList;
    boolean refreshingLogs = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = findViewById(R.id.logList);
        skippedList = findViewById(R.id.skppedList);
        LiveData<String> eventList = App.liveEventsList;
        LiveData<String> skippedEventList = App.liveSkippedEventsList;
        eventList.observe(this, newList -> {
            if (!refreshingLogs) return;
            showAndScrollToEnd(newList);
        });

        skippedEventList.observe(this, newList -> showSkippedAndScrollToEnd(newList));
    }

    private void showAndScrollToEnd(String newList) {
        list.setText(newList);
        if (!newList.isEmpty()) list.setSelection(newList.length() - 2, newList.length() - 1);
        list.clearFocus();
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