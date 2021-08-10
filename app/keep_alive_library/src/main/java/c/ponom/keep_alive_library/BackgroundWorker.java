package c.ponom.keep_alive_library;

@SuppressWarnings("unused")
public class BackgroundWorker {


    /**
     *  тут можно, к примеру, инициировать для последующего обзора через  observeForever лайфдаты
     *  полученные через lifeKeeper.subscribeOn..., или выполнить другие однократные действия
     *  и реализовать все другие обработчики
     * метод к примеру можно вызвать  при автозапуске/перезапуске если в onCreate Application класса
     * есть следующий код:
     * LifeKeeper.getInstance().start(this);
     * MyBackgroundWork myBackGroundWork = new MyBackgroundWork();
     * myBackGroundWork.backgroundProcessorSetup();
     * Фактически данный класс  используется для разгрузки Application от излишнего стартового  кода
     */
    public void backgroundProcessorSetup() {


    }

}
