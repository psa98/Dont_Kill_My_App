package c.ponom.survivalistapplication.lifekeeper;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Set;

import c.ponom.survivalistapplication.Logger;


public final class RelaunchWorkRequest extends Worker {


    public RelaunchWorkRequest(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        LifeKeeper lifeKeeper = LifeKeeper.getInstance();
        Set<String> tags = getTags();
        String tagString = "Unknown!";
        for (String tagItem : tags) {
            //todo  грубый хак для передачи числа в тэгах,
            // заменить тэг на что то вроде seconds=xxx,брать оттуда
            if (tagItem.length() > 5) continue;
            tagString = tagItem;
            break;
        }


        Logger.registerWorkerEvent("type=" + tagString + " seconds");
        int period = Integer.parseInt(tagString);
        lifeKeeper.launchRepeatingWorkRequest(period);
        lifeKeeper.launchTimerTask();
        return Result.success();

    }
}
