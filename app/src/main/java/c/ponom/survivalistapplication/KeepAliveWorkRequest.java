package c.ponom.survivalistapplication;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;



public class KeepAliveWorkRequest extends Worker {


    public KeepAliveWorkRequest(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    //Data sampleResultData = Data.fromByteArray(new byte[]{1});
    // тут можно вернуть данные из воркера



    @NonNull
    @Override
    public Result doWork() {

        // единственная задача воркера - разбудить приложение если его вдруг прибила система
        App.registerWorkerEvent("Type=" + tagString + "seconds");

        return Result.success();

    }
}
