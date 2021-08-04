package c.ponom.survivalistapplication.lifekeeper;

import android.content.Context;


@SuppressWarnings({"unused", "RedundantSuppression"})
public abstract class BackgroundWork {

    /**
     * если переопределить = тут можно, к примеру, инициировать для последедующего обзора
     * observe forever лайфдаты через LifeKeeper.subscribe..., выполнить другие однкратные действия
     * метод так же вызывается при
     */
    public abstract void backgroundProcessorSetup();


    public void launchLifeKeeper(Context context) {
        LifeKeeper.getInstance().start(context);

    }


    public void stopLifeKeeper(Context context) {
        LifeKeeper.getInstance().pause(context);

    }


}
