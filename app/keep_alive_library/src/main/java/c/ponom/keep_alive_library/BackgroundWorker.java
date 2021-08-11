package c.ponom.keep_alive_library;

@SuppressWarnings("unused")
public abstract class BackgroundWorker {


    /**
     *  Tут можно, к примеру, инициировать для последующего обзора через  observeForever лайфдаты
     *  полученные через lifeKeeper.subscribeOn..., или выполнить другие однократные действия
     *  и реализовать все другие обработчики.<BR>
     * Метод  можно вызвать  при автозапуске/перезапуске если в onCreate Application класса
     * есть следующий код:<BR>
     * LifeKeeper.getInstance().start(this);<BR>
     * MyBackgroundWork myBackGroundWork = new MyBackgroundWork();<BR>
     * myBackGroundWork.backgroundProcessorSetup(); <BR>
     * Фактически данный класс  используется для разгрузки Application от излишнего стартового  кода
     */
    public abstract void backgroundProcessorSetup();

}
