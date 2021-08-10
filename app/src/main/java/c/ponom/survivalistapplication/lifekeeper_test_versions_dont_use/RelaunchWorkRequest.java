package c.ponom.survivalistapplication.lifekeeper_test_versions_dont_use;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Locale;
import java.util.Set;

import c.ponom.survivalistapplication.Logger;

import static java.lang.String.format;


public final class RelaunchWorkRequest extends Worker {


    public RelaunchWorkRequest(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }


    @NonNull
    @Override
    public Result doWork() {
        LifeKeeper lifeKeeper = LifeKeeper.getInstance();
        Set<String> tags = getTags();
        String tagString = "";
        for (String tagItem : tags) {
            if (!tagItem.startsWith("seconds=")) continue;
            tagString = tagItem.substring(8);
            break;
        }

        int period = Integer.parseInt(tagString);
        if (tagString.isEmpty()) return Result.success();
        // что-то пошло не так, в теге нет правильного времени, перезапуска не будет

        Logger.registerWorkerEvent(format(Locale.getDefault(),"Worker event %d s", period));
        lifeKeeper.launchRepeatingWorkRequest(period);
        lifeKeeper.launchTimerTask();
        lifeKeeper.emitEvents();
        return Result.success();

    }
}
