    - модуль server - пока оставил как и было на уроке 3

    - модуль client - начал реализовывать UI на javaFX,
    он запускается из метода Main в модуле client
javaFX зависимости в мавен добавить не смог(не запускается), скачал sdk javaFX и подключил вручную его библтотеки,
и прописать VM options:
"--module-path /home/dima/Загрузки/openjfx-19_linux-x64_bin-sdk/javafx-sdk-19/lib --add-modules=javafx.controls,javafx.fxml"
    - При запуске нажимаем кнопку "update file list" и видим список файлов в директории клиента(в т.ч. во вложенных папках)
выбираем файл для отправки на сервер, нажав кнопку "send to server" отправляем файл на сервер
    - Удаляем файл в директории клиента нажав "delete in client"

    - Пока работает только отправка с клиента на сервер
    - Пока без аутентификации


    так же пытался делать клиента на nio но
    при сериализации в nio на сервере с netty получаем ошибку
    "io.netty.handler.codec.TooLongFrameException: Adjusted frame length exceeds 104857600"

добавил в модель Message поле list<String> files чтобы отправлять список файлов имеющихся на сервере