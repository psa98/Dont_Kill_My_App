package c.ponom.survivalistapplication;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


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
        App.launchWorkRequest();
        return Result.success();

    }
}
