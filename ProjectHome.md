Multifunctional android GPS/GLONASS tracker-videorecorder (DVR) and map browser for [GeoScope service](http://translate.google.ru/translate?hl=ru&sl=ru&tl=en&u=http%3A%2F%2Fgisar.sourceforge.net%2FGeoScope%2FIndex.html).

The project consists of two parts:

1. Map browser.

![http://gisar.sourceforge.net/GeoScope/GeoLog/GeoEyeEn.png](http://gisar.sourceforge.net/GeoScope/GeoLog/GeoEyeEn.png)

**_Features_: easy map navigation (moving/scaling/rotating), two different viewing modes (tile mode, map shot mode), list of frequently observed places, list of user tracker-objects, search map objects by name context. Support of other tracker types, for example Enfora (Enfora MT3000, support of vehicle diagnostic protocol ODB-II that allows to monitor many automobile parameters such as ignition, stop/idle/motion, acceleration, tow alert etc).**<font color='Red'>New: map user editing/drawing ("WIKI" style).</font>

![http://gisar.sourceforge.net/GeoScope/GeoLog/MayDay.jpg](http://gisar.sourceforge.net/GeoScope/GeoLog/MayDay.jpg)
![http://gisar.sourceforge.net/GeoScope/GeoLog/GeoEyeWikiPainting.png](http://gisar.sourceforge.net/GeoScope/GeoLog/GeoEyeWikiPainting.png)

2. Multifunctional tracker-videorecorder. That is Android service for transmitting GPS/GLONASS info, video-camera streams and onboard sensor's data from device to the server.

![http://gisar.sourceforge.net/GeoScope/GeoLog/GeoLogEn.png](http://gisar.sourceforge.net/GeoScope/GeoLog/GeoLogEn.png)

**_Features:_
  * sending GPS fixes and other events by time interval, distance interval, value thresholds.
  * getting current location by user request.
  * notifying subscribed user about "tracker is out of area", "trackers is inside of area" and other triggered events.
  * signalling on pressing/releasing "Alarm Button".
  * sending device status information: signal strength value, battery change value.
  * creating a POI (Point Of Interest) on map and adding related data such as: Text, Images, Video clips.
  * working without server connection in "Black Box" mode. In this case all outgoing events are stored into event queue on flash disk. Data will be transmitted to the server side as soon as server connection be established.
  * optimizing and encrypting outgoing traffic. All data packets are packed and encrypted before sending. If queue contains a sequence of similar events so these events are joined into single zipped and encrypted packet for transmission.
  * working as Video/Audio streamer (H.263,H.264,AAC). Remoted user can watch video/audio using GeoScope client program ([download](https://geo-log.googlecode.com/files/GeoScope.090912.zip)) with installed VLC player ([download VLC](http://www.videolan.org/vlc)).
  * working as Video-registrator (DVR). Recorded video files are stored on flash card as MPEG4 or 3GPP. Remoted user can download and play those files using GeoScope client program (with VLC player).**


_!!! Android tracker application (Geo.log) that can be installed from this page works without registration on the server side so that only map browsing will be available. To get full functionality of the tracker you should make following steps:
  * [Download GeoScope client program](https://geo-log.googlecode.com/files/GeoScope.090912.zip).
  * Unpack zip archive.
  * Launch executive file SOAPClient.exe in folder GeoScope.
  * Register a new user.
  * Login into the server with new user account.
  * Create new object that will represent a tracker. "Start panel" -> "New objects creating" -> press "Geo.Log Person Android Model2" button.
  * Initialize android device. Connect device to computer via USB -> Select device drive letter -> press OK. Flash disk will contain a "Geo.Log" folder supplied with configuration files.
  * Install tracker application or restart it._


Android tracker controlling will be available now via "Geo monitor object" panel in the GeoScope client program like it shown below:

![http://gisar.sourceforge.net/GeoScope/GeoLog/ObjectPropsPanelEn.png](http://gisar.sourceforge.net/GeoScope/GeoLog/ObjectPropsPanelEn.png)

Server "GeoScope" is free for nonprofit purposes and it is free for private using (<= 3 users). Android version for tracker >= 2.2.

Any questions? Feel free to [contact us](mailto:alxponom@mail.ru).

**GeoScope-project (copyleft) 2001-2012.**




.Русская версия

Многофункциональный андроид трекер-видеорегистратор и обозреватель карт для сервера [GeoScope](http://gisar.sourceforge.net/GeoScope/Index.html)

состоит из двух частей:

1. Обозреватель карты и объектов на ней.

![http://gisar.sourceforge.net/GeoScope/GeoLog/GeoEye.png](http://gisar.sourceforge.net/GeoScope/GeoLog/GeoEye.png)

**_Возможности_: навигация по карте (перемещение/масштабирование/поворот), два режима отображения (плитки, снимок карты), хранение закладок часто посещаемых мест, хранение списка наблюдаемых объектов-трекеров, поиск объектов карты по имени. Поддержка трекеров различных типов, в частности Enfora (Enfora MT3000, обработка данных по протоколу диагностики автомобиля ODB-II, позволяющего контролировать основные параметры движения транспортного средства).**

2. многофункциональный трекер-служба, передающая информацию от приемника местоположения, камеры, микрофона и различных датчиков.

![http://gisar.sourceforge.net/GeoScope/GeoLog/GeoLog.png](http://gisar.sourceforge.net/GeoScope/GeoLog/GeoLog.png)

**_На данный момент трекер умеет:_
  * передавать координаты о текущем местоположении через определенный интервал расстояния или времени.
  * получать мгновенную позицию устройства по запросу.
  * извещать других пользователей о входе в определенный регион пространства и/или выходе из него. А также, извещать о сближении/удалении устройства с каким-либо другим объектом, при срабатывании порога расстояния.
  * сигнализировать пользователей проекта о нажатии "Тревожной кнопки".
  * передавать информацию о состоянии устройства: силу сигнала сети, заряд аккумулятора устройства.
  * создавать на карте точки интереса (POI) согласно текущим координатам устройства и прикреплять к ним сопутствующие данные: Текст, Изображение, Видео-клип.
  * работать вне сети, в режиме "Черного ящика", все поступившие события сохраняются в очереди на флэш-карте устройства. При появлении сигнала сети, данные передаются на сервер.
  * оптимально передавать исходящий трафик. Все передаваемые данные сжимаются и шифруются. Также, если, в течение некоторого времени, в очереди скопилось несколько однотипных событий(например координат), то они передаются в сжатом и шифрованном виде одним пакетом, что значительно сокращает трафик.
  * работать в режиме Видео/Аудио трансляции (H.264 поток). Другие пользователи через [Удаленный клиент](http://gisar.sourceforge.net/GeoScope/Server/Clients/SOAPClient/DOC/Description.htm) ([скачать](http://code.google.com/p/geo-log/downloads/detail?name=GeoScopeClient.250812.zip#makechanges)) имеют возможность смотреть трансляцию ([VLC плейер должен быть установлен](http://www.videolan.org/vlc)).
  * работать в режиме Видео-регистратора. Видео-файлы сохраняются на флэш-карте в формате MPEG4 или 3GP. Удаленный клиент имеет возможность скачать эти файлы за определенный промежуток времени и посмотреть их (VLC плейер должен быть установлен).**


_! [Андроид-приложение-трекер](http://code.google.com/p/geo-log/downloads/detail?name=GeoLog.250812.apk#makechanges), скачиваемое с этой страницы и с сайта Play.Google.com работает без регистрации на сервере, поэтому, будет доступен только режим просмотра карты и других объектов. Для того, чтобы обеспечить трекеру полный функционал необходимо зарегистрироваться в проекте, и создать новый объект на карте привязанный к данному устройству. Для этого надо скачать [клиентское приложение](http://code.google.com/p/geo-log/downloads/detail?name=GeoScopeClient.250812.zip#makechanges), распаковать архив, запустить файл SOAPClient.exe.
Далее в нем:
  * зарегистрировать нового пользователя.
  * войти в проект.
  * создать андроид-объект: на стартовой панели выбрать закладку "Создание новых объектов" и нажать кнопку "Geo.Log Персона Андроид Model2". По окончании создания объекта, программа предложит инициализировать устройство, для этого надо подключить "Андроид" к компьютеру, и выбрать букву соответствующую флэш-диску устройства. Там в корне создастся каталог Geo.Log  с настроечными файлами._

В итоге, в клиентской программе, управление устройством будет доступно через панель свойств объекта карты:

![http://gisar.sourceforge.net/GeoScope/GeoLog/ObjectPropsPanel.png](http://gisar.sourceforge.net/GeoScope/GeoLog/ObjectPropsPanel.png)

Если будут вопросы, предложения или непонятки, а также, по вопросу приобретения [GeoScope](http://gisar.sourceforge.net/GeoScope/Index.html)-сервера,  [пишите](mailto:alxponom@mail.ru).
Сервер для некоммерческого или частного (на 3-х пользователей) использования бесплатен.
Дополнительная информация по трекеру (но частично устаревшая) доступна [тут](http://geoscope.su/index.php/klientyi/android/opisaie-klienta-android). Версия Android для трекера не ниже 2.2