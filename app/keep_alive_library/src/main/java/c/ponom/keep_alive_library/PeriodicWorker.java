package c.ponom.keep_alive_library;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


public final class PeriodicWorker extends Worker {


    public PeriodicWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        LifeKeeper.getInstance().emitEvents();
        return Result.success();
    }
}
