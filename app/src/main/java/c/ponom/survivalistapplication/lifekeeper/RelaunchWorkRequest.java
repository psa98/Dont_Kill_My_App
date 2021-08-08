package c.ponom.survivalistapplication.lifekeeper;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Set;

import c.ponom.survivalistapplication.Logger;

import static java.lang.String.format;


public final class RelaunchWorkRequest extends Worker {


    public RelaunchWorkRequest(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public Result doWork() {
        LifeKeeper lifeKeeper = LifeKeeper.getInstance();
        Set<String> tags = getTags();
        String tagString = "Unknown!";
        for (String tagItem : tags) {


            //todo  грубый хак для передачи числа в тэгах,
            // заменить тэг на что то вроде seconds=xxx,брать оттуда
            if (!tagItem.startsWith("seconds=")) continue;
            tagString = tagItem.substring(8);
            break;
        }

        int period = Integer.parseInt(tagString);
        Logger.registerWorkerEvent(format("Worker event %d s", period));
        lifeKeeper.launchRepeatingWorkRequest(period);
        lifeKeeper.launchTimerTask();
        lifeKeeper.emitEvents();
        return Result.success();

    }
}
