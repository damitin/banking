## ТЗ
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


## Используемые технологии:
1. Spring Boot 3
2. PostgreSQL
3. Flyway. Скрипты инициализации (DDL) и наполнения БД (DML)
4. Docker. Полностью докеризировал приложение (3 контейнера: Java + PostgreSQL + Kafka) по аналогии с https://www.baeldung.com/spring-boot-postgresql-docker
Смотреть раздел "Запуск приложения в Docker"
5. Maven
6. Юнит-тесты JUnit Jupiter API + Mockito. com/bankname/banking/controller/ApplicationControllerUnitTest.java
7. Интеграционные тесты com/bankname/banking/controller/ApplicationControllerIntegrationTest.java
8. AOP (логирование - явно, глобальная обработка исключений и транзакции не явно)
9. Логирование slf4j, datasource-proxy (для перехвата запросов к БД с подстановкой значений параметров)
10. Spring Profiles. Смотреть раздел "Запуск приложения"
11. Kafka в качестве брокера сообщений между данным сервисом и сервисом нотификации https://github.com/damitin/EmailNotificationMicroservice
12. Swagger. Смотреть раздел "Swagger URL"
13. Lombok местами
14. Ehcache. Кэш второго уровня для словаря AccountStatus для сокращения числа обращений к БД


## Сборка и запуск приложения

### Клонировать репозиторий

### Вариант 1. Приложение запускается из среды разработки, Postgres и Kafka запускаются в Docker.
Выполнить файл docker-compose-without-java-app.yml из среды разработки или в корне проекта выполнить
```
docker-compose -f docker-compose-without-java-app.yml up -d
```
### Вариант 2. Приложение, Postgres и Kafka запускаются в Docker.
Собрать приложение с тестами (если ранее был выполнен Вариант 1). В корне проекта выполнить
```
mvn clean package
```
Или сразу собрать без тестов
```
mvn clean package -DskipTests
```
Выполнить файл docker-compose.yml из среды разработки или в корне проекта выполнить
```
docker-compose up -d
```
После внесения изменений в приложение, при повторной попытке поднять в докере, нужно удалить контейнер (Containers) "app" в стеке "banking_all_in_one_stack" (можно удалить весь стек), а затем образ (Images) "banking". Далее выполнить package.
## Запуск приложения
Профиль по умолчанию
```
java -jar banking-0.0.1-SNAPSHOT.jar
```
Или с выбором профиля dev/prod
```
java -jar banking-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

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