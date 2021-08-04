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

/*
    public static void startLifeKeeper(Context context) {
        LifeKeeper.getInstance().start(context);

    }
    public static void pauseLifeKeeper(Context context) {
        LifeKeeper.getInstance().pause(context);

    }

*/

    public void launchLifeKeeper(Context context) {
        LifeKeeper.getInstance().start(context);
        // todo - надо в init засунуть инициализацию и автозапуск лайфкипера если он еще не запущен

    }


    public void stopLifeKeeper(Context context) {
        LifeKeeper.getInstance().pause(context);

    }


}
