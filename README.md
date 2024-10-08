## Встановлення
1. Створіть базу данних та підєднайте до application.properties (потребує build.gradle зміни)
2. Запустіть команду у створеній базі данних
3. Запустіть ./run
4. Пароль від root: 1234
5. Якщо пише що немає доступу коли ви маєте його то перезайдіть

Команда:
```sql
-- creating books table
CREATE TABLE IF NOT EXISTS books (
	id SERIAL PRIMARY KEY,
	title VARCHAR(255) UNIQUE NOT NULL,
	author VARCHAR(255) NOT NULL
);

-- creating users table
-- token always will be unique
CREATE TABLE IF NOT EXISTS users (
	id SERIAL PRIMARY KEY,
	username VARCHAR(255) UNIQUE NOT NULL,
	password VARCHAR(255) NOT NULL,
	token VARCHAR(255) UNIQUE NOT NULL
);

INSERT INTO users (username, password, token)
VALUES ('root', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'TOKEN:5287fc10739f99871d410951045885a6bfc668435fdc2de409b64344ffca7ba9')
```

Домашнє завдання TODO list: завдання: зробити бібліотеку для мінапулявання книгами та
вбудованим у сервер RESTful API з перевіркою на дозвіл війського рівня (спойлер: це SHA256 усе підряд)
та зберіганням у базу данних. маніпулявання має бути у консолі

✅ Додавати нову книгу.<br>
✅ Видаляти книгу за ID.<br>
✅ Шукати книги за автором та за назвою.<br>
✅ Виводити список усіх книг, відсортований за назвою.<br>
✅ Зберігати та завантажувати список книг з/в файл.<br>

✅ Використовувати об'єктно-орієнтоване програмування.<br>
✅ Взаємодія з користувачем через консоль (введення/виведення).<br>
✅ Реалізувати винятки обробки помилок. [DUPLICATE]<br>
✅ Застосувати колекції для зберігання даних.<br>
✅ Реалізувати серіалізацію для збереження та завантаження даних.<br>

✅ Створення нового користувача.<br>
✅ Отримання інформації про користувача ID.<br>
✅ Оновлення інформації про користувача.<br>
✅ Видалення користувача за ID.<br>
✅ Отримати список усіх користувачів.<br>

✅ Використовувати Spring Boot для створення програми.<br>
✅ Використовуйте JPA для взаємодії з базою даних.<br>
✅ Створити відповідні DTO передачі даних.<br>
✅ Реалізувати обробку помилок.<br>