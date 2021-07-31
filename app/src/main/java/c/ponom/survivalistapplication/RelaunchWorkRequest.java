package c.ponom.survivalistapplication;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Set;


public class RelaunchWorkRequest extends Worker {


    public RelaunchWorkRequest(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    //Data sampleResultData = Data.fromByteArray(new byte[]{1});
    // тут можно вернуть данные из воркера



    @NonNull
    @Override
    public Result doWork() {

        // задача воркера  - рестарт нового одноразового
        App.registerWorkerEvent();
        Set tags = getTags();
        String tagtring = (String) getTags().toArray()[1];

        int period = Integer.getInteger(tagtring);
        // извлекаем данные о заданном времени из единственного тэга.
        App.launchFrequentWorkRequest(period);
        return Result.success();

    }
}
