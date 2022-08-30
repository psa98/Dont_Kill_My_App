# Dont_Kill_My_App
Библиотека классов и тестовое приложение к ней.
Задача:  
Создание кода, способного поддерживать активность (препятствовать остановке его операционной системой) 
пок приложение находится в фоне, по возможости игнорируя doze mode и другие оптимизации экономии 
питания Android систем с автоматическим перезапуском приложения при его закрытии системой или
перезагрузкой устройства.

Собственно идея написать эту библиотеку возникла по итогам моего участия в чате https://t.me/android_ru
где жалобы на то что "система убивает мое крутое приложение и не дает мне в фоне слать с телефона какую-то
телеметрию круглые сутки" попадались несколько раз в день. Для решения пришлось использовать приемы ТРИЗ 
и подумать с полчасика - после чего оно стало вполне очевидным.

Для активации режима "выживания приложения" достаточно добавить в onCreate() Application класса
следующий код: 
LifeKeeperAPI lifeKeeper = LifeKeeperAPI.getInstance();

lifeKeeper.start(this);

Дополнительно можно, к примеру,  инициировать для последующего обзора через  observeForever лайфдаты
полученные методом lifeKeeper.subscribeOnPeriodicEvents(), или установить слушатели на вырабатываемые
регулярные события.


Протестировано на Samsung SDK level 30 - обеспечивается "незасыпание" фоновых процессов в среднем 
более чем на 3 минуты при обычной работе, и эпизодические перерывы не более 5-10-20 минут в Doze mode.

Поддерживается уровень SDK 19-31. Работа на аппаратах с агрессивной оптимизацией питания китайских
производителей не тестировалась и может быть проблемной

Как это работает:

1. Приложение, которое не находится на переднем плане, но активно выполняет какую-то работу в фоне,
 находится в приорететном положении с точки зрения его выгрузки системой и выделения процессорного 
 времени
2. Приложение остановленное или убитое, но имеющее активные бродкаст ресиверы и воркеры  перезапускается 
и при их сработке (получении события). Таким образом, следует подписаться на возможно большее
количество вызываемых перодически  системных бродкастов  и  создать несколько регулярных воркеров 
с разной периодичностью.
3. Timer из класса java.util для остановленного приложения никак не гарантирует сработки TimerTask 
в заданное время и вообще. Однако можно ожидать достаточной периодичности в вызове бродкастов и сработке 
воркеров WorkerManager-a, данные периодические события можно использовать для вызова периодических 
фоновых операций в основном приложении с негарантированнойно достаточной частотой
4. В "агрессивном" режиме используется то обстоятельство что система обычно не будет останавливать
 приложение играющее звук в фоне, для того что бы не прерывать проигрывание музыки. Для экономии
 энергии проигрывается "звук тишины" - пренебрежимо малый сигнал записанный в формате wav, не 
 требующем ресурсов процессора для дешифровки, через один канал и на минимально возможной громкости.
     
  
Имеется API для управления основными параметрами "агрессивности" сохранения жизнеспособности приложения в фоне,
а так же, как побочный результат, для  получения данных о текущем заряде батареи, переходе в спящий режим.
