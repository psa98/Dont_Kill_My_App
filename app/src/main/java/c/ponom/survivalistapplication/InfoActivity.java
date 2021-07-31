package c.ponom.survivalistapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.impl.WorkManagerImpl;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.concurrent.TimeUnit;

import static c.ponom.survivalistapplication.SharedPrefsRepository.DataType.STRING;
import static c.ponom.survivalistapplication.SharedPrefsRepository.getParameterString;

public class InfoActivity extends AppCompatActivity {

    EditText list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list =findViewById(R.id.logList);
    }

    public void showFullLog(View view) {
        list.setText(getParameterString("events"));
    }



    public synchronized void clearEvents (View view){
        SharedPrefsRepository.saveParameter("","events", STRING);
        list.setText("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        list.setText(getParameterString("events"));
    }
}