## Что нужно сделать
Реализовать web-приложение для работы со счетами клиента. Клиент должен иметь возможность:

1. Пополнить счет.
2. Снять со счета.
3. Получить инфомацию по конкрентому счету.
4. Получить инфомацию по счетам на основе каких-то параметров.
5. Открыть счет.
6. Закрыть счет.

_Ввел сущность статуса. Закрытие счета != удаление счета._

_В маппингах для пополнения и снятия пользователь не используется, счет известен, счет без пользователя не бывает._
_Плюс накинул некоторую бизнес логику_

Внешний интерфейс приложения представлен в виде HTTP API.

## Обязательные требования:
1. Реализовано на Java+SpringBoot.
2. Без UI.
3. СУБД Postgre. Поднимается рядом с приложением в докер-контейнере.

Полностью докеризировал приложение по аналогии с https://www.baeldung.com/spring-boot-postgresql-docker
Смотреть ниже раздел "Запуск приложения в Docker".

4. Основные компоненты приложения покрыты unit-тестами.

Добавлены интеграционные тесты:
src/test/java/com/bankname/banking/controller/ApplicationControllerIntegrationTest.java

Отключены миграции flyway по наполнению БД (V20, V21 переименованы), чтобы тесты не зависели от скриптов наполнения.

5. Flyway для версионирования схемы базы данных.

_Да. Если нужно, чтобы скрипты flyway вернули БД в исходное состояние, предварительно запустить resources/sql/drop_all.sql_

6. Применение REST практик.
7. Maven

## Сборка приложения

### Развернуть Postgres в контейнере Docker
```
docker run -d --name postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres
```
### Развернуть Kafka в контейнере Docker
```
docker run -p 9092:9092 -d --name kafka-server --hostname kafka-server \
--network app-tier \
-e KAFKA_CFG_NODE_ID=0 \
-e KAFKA_CFG_PROCESS_ROLES=controller,broker \
-e KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 \
-e KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT \
-e KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=0@kafka-server:9093 \
-e KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER \
bitnami/kafka:latest
```
Дополнительно на локальной машине в файл /etc/hosts добавить запись (будет исправлено)
```
127.0.0.1 kafka-server
```
### Сборка приложения
Теперь можно запустить сборку приложения. В корне проекта выполнить
```
./mvnw clean package
```

## Докеризация приложения
В корне проекта выполнить
```
docker-compose up -d
```
При повторной попытке поднять в докере, нужно удалить контейнер "banking".

## Тестирование
При повторной попытке пройти тесты нужно:
- дропнуть БД, т. к. прокрутились ID после интеграционных тестов com/bankname/banking/controller/ApplicationControllerIntegrationTest.java.
- дропнуть информацию о миграциях, чтобы они снова отработали.
```
src/main/resources/sql/drop_all.sql
```
Для проверки можно отправить API запрос для получения всех счетов пользователей, в логине которых содержится строка "a" (пользоват):
```
localhost:8080/users?login=a
```
Выдаст пустой массив, т. к. БД зачищается в tearDown() методе интеграционных тестов com/bankname/banking/controller/ApplicationControllerIntegrationTest.java.

Наполнение базы тестовыми данными можно сделать при помощи отключенных скриптов миграции Flyway

```
src/main/resources/db/migration/fill/_V20__fill_table_banking.user.sql
src/main/resources/db/migration/fill/_V21__fill_table_banking.account.sql
```

Swagger URL:
```
http://localhost:8080/swagger-ui/index.html#
```