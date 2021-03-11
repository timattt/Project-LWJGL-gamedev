# WIP
Тут описаны механики, которые реализованы, но пока что плохо проработаны.

## Поселения
Еще одна глобальная механика - поселения.
Теперь у нас на карте есть города, которые могут расширяться и производить всякие ресурсы.   
**Вот так выглядит центральный район города**
  ![](https://github.com/timattt/LWJGL-Programming-timattt/blob/master/imgs/Settlement_example1.png)
Давайте расширим город - построим район.   
**Открываем GUI города**
  ![](https://github.com/timattt/LWJGL-Programming-timattt/blob/master/imgs/Settlement_example2.png)
**Нажимаем кнопку - попадаем в вид сверху - выбираем клетку для нового района**
  ![](https://github.com/timattt/LWJGL-Programming-timattt/blob/master/imgs/Settlement_example3.png)
Отлично, а теперь мы хотим построить в новом районе домик. Чтобы город производил больше полезных ресурсов.   
**Открываем панель строительства зданий - выбираем**
  ![](https://github.com/timattt/LWJGL-Programming-timattt/blob/master/imgs/Settlement_example4.png)
**Начинаем строить - слева показывается скорость строительства**
  ![](https://github.com/timattt/LWJGL-Programming-timattt/blob/master/imgs/Settlement_example5.png)
Осталось только добавить побольше структур внутри города - фабрики, стены и т.д.
  
## Карточки
Еще одна механика - система карточек.
У каждого игрока есть некоторый набор игровых карточек, которые можно "кинуть" на какую-нибудь клетку и карточка сделает что-нибудь.
Идея экспериментальная, но весь необходимый функционал уже есть.   
**Вот карточка уже лежит**
  ![](https://github.com/timattt/LWJGL-Programming-timattt/blob/master/imgs/Card_example.png)
  
