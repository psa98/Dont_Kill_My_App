package c.ponom.keep_alive_library;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import org.jetbrains.annotations.NotNull;

import c.ponom.keep_alive_library.LifeKeeper.LifeKeeperEventsListener;


@SuppressWarnings({"unused", "RedundantSuppression"})
public final class LifeKeeperAPI {


    private final static LifeKeeper lifeKeeper = LifeKeeper.getInstance();

    /*
    Класс-обертка предназначен  для скрытия непубличных (package-private) методов класса,
    их имплементирующих, и реализации вызова API через статические методы
    */



    /** При значении параметра  extremeDozeAvoidance =true, будет использоваться дополнительный
     * метод поддержания активности приложения в режиме экономии энергии.  Его использование может
     * привести к увеличению расхода батареи. Параметр игнорируется для SDK < 21
     */
    public static synchronized boolean isUsingExtremeDozeAvoidance() {
        return lifeKeeper.isUsingExtremeDozeAvoidance();
    }

    /** При значении параметра  extremeDozeAvoidance =true, будет использоваться дополнительный
     * метод поддержания активности приложения в режиме экономии энергии.  Его использование может
     * привести к увеличению расхода батареи. Параметр игнорируется для SDK < 21
    */
    public static void setExtremeDozeAvoidanceMode(boolean extremeDozeAvoidance) {
        lifeKeeper.setExtremeDozeAvoidanceMode(extremeDozeAvoidance);
    }

    /**
     * Вызовите этот метод из application класса для обеспечения выживания  приложения в фоне.
     * Oно так же будет автоматически перезапускаться при перезагрузке. При этом (и при любом перезапуске)
     * гарантируется вызов метода onCreate(..) Application класса, где можно возобновить необходимые
     * подписки на события.<BR>
     * При значении параметра  extremeDozeAvoidance =true, будет использоваться дополнительный
     * метод поддержания активности приложения в режиме экономии энергии, до достижения заданного
     * уровня разрядки батареи (15% по умолчанию).  Его использование может
     * привести к увеличению расхода батареи в DozeMode. Параметр игнорируется для SDK < 21
     * При необходимости выполнять периодические фоновые действия  возможна установка слушателей
     * на периодические события через setEventListener(), или подписка на обновление лайфдат через
     * subscribeOnPeriodicEvents (...) subscribeOnAllEvents(), или на получение конкретных интентов
     * в классе EventReceiver через установку слушателей типа setBatteryEventListener(...)
     */
    public static  synchronized void start(Context context, boolean extremeDozeAvoidance) {
        lifeKeeper.start(context, extremeDozeAvoidance);
    }


    /**
     * Вызовите этот метод из application класса для обеспечения выживания  приложения в фоне,
     * без примерения  агрессивного метода  сохранения жизни приложения(аудиохака)
     * При любом перезапуске  гарантируется вызов метода onCreate(..) Application класса,
     * где можно возобновить необходимые подписки на события.<BR>
     * При необходимости выполнять периодические фоновые действия  возможна установка слушателей
     * на периодические события через setEventListener(), или подписка на обновление лайфдат через
     * subscribeOnPeriodicEvents (...) subscribeOnAllEvents(), или на получение конкретных интентов
     * в классе EventReceiver через установку слушателей типа setBatteryEventListener(...)
     */

    public static synchronized void start(Context context){
        start(context,false);
    }

    /**
     * Временная приостановка работы ресиверов и эмиттеров событий.
     * Не отключает автоматический запуск  приложения при перезагрузке телефона в будущем
     */
    public static synchronized void pause(Context context) {
        lifeKeeper.pause(context);
    }


    /**
     * Данные в возвращаемой лайфдате (unix time время в мс) обновляются при первой возможности,
     * с максимально возможной частотой, позволяемой системой, но не чаще заданного периода
     * (минимум - 60 секунд).<BR>
     * Гарантий вызова события с заданной частотой дать невозможно, система может убить или
     * остановить приложение в любой момент. Типичные задержки вызова  (заданное время+задержка)
     *  в doze mode по итогам тестов могут достигать 3 минут, при длительном нахождение в режиме-
     * 20 минут, в ночное время когда юзер не пользуется телефоном - несколько часов
     * При нахождении приложения во фронте - не более 5-15 секунд, при включенной экстремальной
     * оптимизации - вплоть до нескольких  раз в минуту, игнорируя doze mode.  Для китайских телефонов
     * можно ожидать очень проблемной работы в фоне без отключения режима экономии для приложения
     *
     * */

    @NotNull
    public static synchronized LiveData<Long> subscribeOnAllEvents() {
        return lifeKeeper.subscribeOnAllEvents();
    }




    /**
    * Данные в возвращаемой лайфдате (unix time время в мс) обновляются при первой возможности,
    * с максимально возможной частотой, позволяемой системой но не чаще заданного периода
    * (минимум - 60 секунд).<BR>
    * Гарантий вызова события с заданной частотой дать невозможно, система может убить или
    * остановить приложение в любой момент. Типичные задержки вызова  (заданное время+задержка)
    *  в doze mode по итогам тестов могут достигать 3 минут, при длительном нахождение в режиме-
    * 20 минут. При нахождении приложения во фронте - не более 5-15 секунд. Для китайских телефонов
    * можно ожидать очень проблемной работы в фоне без отключения режима экономии для приложения
    *
    * */
    @NotNull
    public static synchronized LiveData<Long> subscribeOnPeriodicEvents(long seconds) {

        return lifeKeeper.subscribeOnPeriodicEvents(seconds);
    }

    /**
     * отписка от конкретной лайфдаты, обновляемой с максимальной частотой
     *
     * */

    public static synchronized void unsubscribeEvents(LiveData<Long> liveData) {
        lifeKeeper.unsubscribeEvents(liveData);
    }

    /**
     * отписка от конкретной лайфдаты, обновляемой с заданной частотой (не чаще)
     *
     * */
    public static synchronized void unsubscribePeriodicEvents(LiveData<Long> liveData) {
        lifeKeeper.unsubscribePeriodicEvents(liveData);
    }


    /**
     * Вызов слушателя осуществляется  при первой возможности, с максимально возможной частотой,
     * позволяемой системой.<BR>
     *  Гарантий вызова события с заданной частотой дать невозможно, система может убить или
     * остановить приложение в любой момент. Ожидаемая частота вызова при телефоне на зарядке
     * и приложении во фронте - раз в 5-10 секунд.  Типичный период вызова  в doze mode по
     * итогам тестов может достигать одного раза в  3 - 10 - 20 минут
     * для отписки установите слушатель в null
     * */

    public static  synchronized  void setEventListener(@Nullable LifeKeeperEventsListener eventListener) {
        lifeKeeper.setAllEventsListener(eventListener);
    }



    /**
     *  Уровень заряда батареи в %, ниже которого прекращается использование агрессивного метода
     * сохранения жизни приложения, по умолчанию 15%
     */
    public int getMinBatteryOptimizationLevel() {
        return lifeKeeper.getMinBatteryOptimizationLevel();
    }

    /**
     * @param minBatteryOptimizationLevel
     * Уровень заряда батареи в %, ниже которого прекращается использование агрессивного метода
     * сохранения жизни приложения, по умолчанию 15%
     */
    public void setMinBatteryOptimizationLevel(int minBatteryOptimizationLevel) {
        lifeKeeper.setMinBatteryOptimizationLevel(minBatteryOptimizationLevel);
    }


}
