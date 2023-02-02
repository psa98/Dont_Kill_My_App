package c.ponom.keep_alive_library;

@SuppressWarnings("unused")
public abstract class BackgroundProcessor {





    /**
     *  Tут можно, к примеру, инициировать для последующего обзора через observeForever лайфдаты
     *  полученные через lifeKeeper.subscribeOn..., или выполнить другие однократные действия
     *  и реализовать все другие обработчики.<BR>
     * Метод можно вызвать при автозапуске/перезапуске если в onCreate Application класса
     * к примеру используя следующий код:<BR>
     * LifeKeeper.getInstance().start(this);<BR>
     * MyBackgroundWork myBackGroundWork = new MyBackgroundWork();<BR>
     * myBackGroundWork.backgroundProcessorInit(); <BR>
     * Фактически данный класс используется для разгрузки Application от излишнего стартового кода
     */
    public abstract void backgroundProcessorInit();

    public  void backgroundProcessorStart(){}

    public  void backgroundProcessorStop(){}

    public  void backgroundProcessorRelease(){}




}


