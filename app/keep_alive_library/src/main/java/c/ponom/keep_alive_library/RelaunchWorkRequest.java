package c.ponom.keep_alive_library;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Set;


public final class RelaunchWorkRequest extends Worker {

    public RelaunchWorkRequest(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        //перезапускаем воркеры
        LifeKeeper lifeKeeper = LifeKeeper.getInstance();
        Set<String> tags = getTags();
        String tagString = "";
        for (String tagItem : tags) {
            if (!tagItem.startsWith("seconds=")) continue;
            //начиная с 8 символа в тэге у нас идет число секунд периодичности данного воркера.
            // система добавляет и свои тэги в другом формате, надо их игнорировать
            tagString = tagItem.substring(8);
            break;
        }
        // что-то пошло не так, в тэге нет правильного времени, перезапуска не будет
        if (tagString.isEmpty()) return Result.failure();
        int period = 0;
        try {
            period = Integer.parseInt(tagString);
        }catch (NumberFormatException exception){
            exception.printStackTrace();
            return Result.failure();
            // что-то пошло не так, в тэге нет правильного времени, перезапуска не будет
        }
        // что-то пошло не так, в теге нет правильного времени, перезапуска не будет
        if (period==0) return Result.failure();
        lifeKeeper.launchRepeatingWorkRequest(period);
        lifeKeeper.launchTimerTask();
        lifeKeeper.emitEvents();
        return Result.success();
    }
}
