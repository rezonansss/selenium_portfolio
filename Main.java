import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;

import org.junit.Test;
import static org.junit.Assert.fail;
import org.junit.Ignore;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

//Генератор слов
class Generator {
    private static final String SYMBOLS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final Random RANDOM = new SecureRandom();

    public String letter(int length) {

        char[] symbols = SYMBOLS.toCharArray();
        char[] buf = new char[length];

        for (int i = 0; i < buf.length; ++i) {
            buf[i] = symbols[RANDOM.nextInt(symbols.length)];
        }

        return new String(buf);
    }
}

public class Main {
    //Сообщение об успешном тесте
    public void pass_end() {
        char checkmark = '\u2713';
        System.out.printf("%c%c%c Тест успешный", checkmark, checkmark, checkmark);
    }

    @Test//Тест регистрации пользователя
    public void A_Site_test() throws InterruptedException {
        System.setProperty("webdriver.chrome.driver", "C://Selenium/ChromeDriver/chromedriver.exe");
        WebDriver driver = new ChromeDriver();

        System.out.printf("======ТЕСТ РЕГИСТРАЦИИ=======\n");
        //Генерация данных для регистрации
        Generator Generate = new Generator();
        String login = Generate.letter(6);
        String password = Generate.letter(12);
        String email = Generate.letter(6) + "@mail.ru";
        System.out.printf("Логин - %s\nПароль - %s\nПочта - %s\n", login, password, email);
        //Открытие страницы формы регистрации
        driver.get("https://online-kassa.store/lichnyj_kabinet/");
        //Заполнение формы
        driver.findElement(By.id("reg_username")).sendKeys(login);
        driver.findElement(By.id("reg_password")).sendKeys(password);
        driver.findElement(By.id("reg_email")).sendKeys(email);
        //Отправка запроса
        driver.findElement(By.name("register")).click();
        //Получаем приветственное сообщение
        String welcome = driver.findElement(By.xpath("//*[@id=\"post-3854\"]/div/div/div[2]/p[1]")).getText();
        //Сравнение ФР с ОР
        if (!welcome.equals("Добро пожаловать, " + login + " (не " + login + "? Выйти)")){
            fail("Ошибка, регистрация не прошла");
        }
        //Закрытие браузера и сообщение об успешном тесте
        pass_end();
        driver.quit();
    }

    @Test//Тест оформления заказа
    public void Cart_test() throws InterruptedException {
        System.setProperty("webdriver.chrome.driver", "C://Selenium/ChromeDriver/chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        System.out.printf("======ТЕСТ КОРЗИНЫ=======\n");
        //Открываем страницу товара
        driver.get("https://online-kassa.store/catalog/online-kassa/proizvoditel/evotor/evotor-7-2/");
        //Проверяем расчёт стоимости
        driver.findElement(By.name("quantity")).clear();//Изменяем количество на 7
        driver.findElement(By.name("quantity")).sendKeys("7");
        driver.findElement(By.className("tm-epo-reset-radio")).click();//Убираем комплект, т.к. он влияет на стоимость
        WebElement price = driver.findElement(By.xpath("//*[@id=\"tm-epo-totals\"]/dl/dd/span/ins/span/bdi"));
        String price_text = price.getText().replaceAll("[^\\d.]", "");//Получаем текст и убираем всё лишнее
        //Сравнение ФР с ОР
        if (price_text.equals("122500")){
            System.out.printf("Стоимость расчитана верно\n");
        }
        else {
            System.out.printf("Стоимость \"%s\" на странице товара расчитана не верно", price_text);//Вывод текущей страницы
            fail("Не правильно расчитана стоимость");
        }
        //Добавляем товар и переходим на сраницу корзины
        driver.findElement(By.name("add-to-cart")).click();

        //Проверка URL
        //Получаем адрес текущей страницы
        String url = driver.getCurrentUrl();

        //Сравнение ФР с ОР
        if (url.equals("https://online-kassa.store/korzina/")){
            System.out.printf("Адрес страницы верный\n");
        }
        else {
            System.out.printf("%s", url);//Вывод текущей страницы
            fail("Не верный адрес страницы");
        }

        //Поиск сообщения об успешном добавлении
        WebElement massage = driver.findElement(By.xpath("//*[@id=\"post-3134\"]/div/div/div[1]/div"));
        String massage_text = massage.getText();

        if (massage_text.equals("Продолжить покупки\nВы отложили 7 × “Эвотор 7.2” в свою корзину.")){
            System.out.printf("Товар отложен в корзину коррекно\n");
        }
        else {
            System.out.printf("%s", massage_text);//Вывод сообщения
            fail("Текст сообщения не совпал с ожидаемым результатом");
        }
        //Проверяем расчёт стоимости
        WebElement kolichestvo_in_cart = driver.findElement(By.name("cart[9518adefea75517037f2fcea1905ff54][qty]"));
        kolichestvo_in_cart.clear();//Изменяем количество на 10
        kolichestvo_in_cart.sendKeys("10");
        driver.findElement(By.name("update_cart")).click();//Обновляем корзину
        Thread.sleep(10000);
        WebElement price_in_cart = driver.findElement(By.xpath("//*[@id=\"post-3134\"]/div/div/form/table/tbody/tr[1]/td[6]/span/bdi"));
        String price_in_cart_text = price_in_cart.getText().replaceAll("[^\\d.]", "");//Получаем текст и убираем всё лишнее
        //Сравнение ФР с ОР
        if (price_in_cart_text.equals("175000")){
            System.out.printf("Стоимость расчитана верно\n");
        }
        else {
            System.out.printf("Стоимость \"%s\" на странице корзины расчитана не верно, ожидаемый результат = 175000", price_in_cart_text);
            fail("Не правильно расчитана стоимость");
        }

        //Нажатие кнопки для оформления заказа
        driver.findElement(By.className("b24-widget-button-popup-btn-hide")).click();//Закрытие сообщения поддержки, т.к. оно мешает нажатию кнопки для оформления корзины
        driver.findElement(By.linkText("Оформить заказ")).click();;
        //Заполнение деталей заказа
        String order_data = "Тест";
        WebElement order_Fname = driver.findElement(By.xpath("//*[@id=\"billing_first_name\"]"));
        order_Fname.sendKeys(order_data);
        WebElement order_lname = driver.findElement(By.xpath("//*[@id=\"billing_last_name\"]"));
        order_lname.sendKeys(order_data);
        WebElement order_number = driver.findElement(By.xpath("//*[@id=\"billing_phone\"]"));
        order_number.sendKeys(order_data);
        WebElement order_email = driver.findElement(By.xpath("//*[@id=\"billing_email\"]"));
        order_email.sendKeys(order_data);
        //Отправка заказа
        driver.findElement(By.linkText("Подтвердить заказ"));
        //Закрытие браузера и сообщение об успешном тесте
        pass_end();
        driver.quit();

    }

}
