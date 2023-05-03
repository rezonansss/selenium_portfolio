import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;

import org.junit.Test;
import static org.junit.Assert.fail;
import org.junit.Ignore;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.security.SecureRandom;
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
    public void Registration() throws InterruptedException {
        System.setProperty("webdriver.chrome.driver", "C://Selenium/ChromeDriver/chromedriver.exe");
        WebDriver driver = new ChromeDriver();

        System.out.printf("======ТЕСТ РЕГИСТРАЦИИ=======\n");
        //Генерация данных для регистрации
        Generator Generate = new Generator();
        String login = Generate.letter(6);
        String password = Generate.letter(12);
        String email = Generate.letter(6) + "@mail.ru";
        System.out.printf("Логин - %s\nПароль - %s\nПочта - %s\n", login, password, email);
        //Открытие страницы с формой регистрации
        driver.get("https://online-kassa.store/lichnyj_kabinet/");

        //Заполнение формы
        driver.findElement(By.id("reg_username")).sendKeys(login);
        driver.findElement(By.id("reg_password")).sendKeys(password);
        driver.findElement(By.id("reg_email")).sendKeys(email);

        //Отправка запроса на регистрацию
        driver.findElement(By.name("register")).click();

        //После регистрации сайт автоматически перенаправляет в авторизованный личный кабинет
        //Получаем приветственное сообщение
        String welcome = driver.findElement(By.xpath("//*[@id=\"post-3854\"]/div/div/div[2]/p[1]")).getText();

        //Сравнение фактического результата с ожидаемым
        if (!welcome.equals("Добро пожаловать, " + login + " (не " + login + "? Выйти)")){
            fail("Ошибка, регистрация не прошла");
        }

        //Закрытие браузера и сообщение об успешном тесте
        pass_end();
        driver.quit();
    }
    @Ignore
    @Test//Тест оформления заказа
    public void Cart() throws InterruptedException {
        System.setProperty("webdriver.chrome.driver", "C://Selenium/ChromeDriver/chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, 20);

        System.out.printf("======ТЕСТ КОРЗИНЫ=======\n");

        //Открываем страницу с товаром
        driver.get("https://online-kassa.store/catalog/online-kassa/proizvoditel/evotor/evotor-7-2/");

        //Проверяем расчёт предварительной стоимости
        //Изменяем количество на 7
        driver.findElement(By.name("quantity")).clear();
        driver.findElement(By.name("quantity")).sendKeys("7");

        //Убираем товар, который автоматический включается в комплект
        driver.findElement(By.className("tm-epo-reset-radio")).click();

        //Получаем предварительную стоимость и убираем всё лишнее кроме цифр
        WebElement price = driver.findElement(By.xpath("//*[@id=\"tm-epo-totals\"]/dl/dd/span/ins/span/bdi"));
        String price_text = price.getText().replaceAll("[^\\d.]", "");

        //Сравнение фактического результата с ожидаемым
        if (price_text.equals("122500")){
            System.out.printf("Стоимость расчитана верно\n");
        }
        else {
            System.out.printf("Стоимость \"%s\" на странице товара расчитана не верно", price_text);
            fail("Не правильно расчитана стоимость");
        }

        //Добавляем товар в корзину и переходим на стрницу корзины
        driver.findElement(By.name("add-to-cart")).click();

        //Проверка текущего URL
        //Получаем адрес страницы
        String url = driver.getCurrentUrl();

        //Сравнение фактического результата с ожидаемым
        if (url.equals("https://online-kassa.store/korzina/")){
            System.out.printf("Адрес страницы верный\n");
        }
        else {
            System.out.printf("%s", url);//Вывод текущей страницы
            fail("Не верный адрес страницы");
        }

        //Поиск сообщения об успешном добавлении товара в корзину
        WebElement massage = driver.findElement(By.xpath("//*[@id=\"post-3134\"]/div/div/div[1]/div"));
        String massage_text = massage.getText();

        //Сравнение фактического результата с ожидаемым
        if (massage_text.equals("Продолжить покупки\nВы отложили 7 × “Эвотор 7.2” в свою корзину.")){
            System.out.printf("Товар отложен в корзину коррекно\n");
        }
        else {
            System.out.printf("%s", massage_text);//Вывод сообщения
            fail("Текст сообщения не совпал с ожидаемым результатом");
        }

        //Проверяем расчёт итоговой стоимости в корзине
        //Изменяем количество на 10
        WebElement kolichestvo_in_cart = driver.findElement(By.name("cart[9518adefea75517037f2fcea1905ff54][qty]"));
        kolichestvo_in_cart.clear();
        kolichestvo_in_cart.sendKeys("10");

        //Обновляем корзину
        driver.findElement(By.name("update_cart")).click();

        //Ждём обновления
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div.blockUI.blockOverlay")));

        //Получаем итоговую стоимость и убираем всё лишнее кроме цифр
        WebElement price_in_cart = driver.findElement(By.xpath("//*[@id=\"post-3134\"]/div/div/form/table/tbody/tr[1]/td[6]/span/bdi"));
        String price_in_cart_text = price_in_cart.getText().replaceAll("[^\\d.]", "");

        //Сравнение фактического результата с ожидаемым
        if (price_in_cart_text.equals("175000")){
            System.out.printf("Стоимость расчитана верно\n");
        }
        else {
            System.out.printf("Стоимость \"%s\" на странице корзины расчитана не верно, ожидаемый результат = 175000", price_in_cart_text);
            fail("Не правильно расчитана стоимость");
        }

        //Оформляем заказ
        driver.findElement(By.className("b24-widget-button-popup-btn-hide")).click();//Закрытие сообщения поддержки, т.к. оно мешает нажатию кнопки для оформления корзины
        driver.findElement(By.linkText("Оформить заказ")).click();

        //Заполнение деталей заказа
        driver.findElement(By.xpath("//*[@id=\"billing_first_name\"]")).sendKeys("Тестовый заказ");
        driver.findElement(By.xpath("//*[@id=\"billing_last_name\"]")).sendKeys("Тестовый заказ");
        driver.findElement(By.xpath("//*[@id=\"billing_phone\"]")).sendKeys("88005553535");
        driver.findElement(By.xpath("//*[@id=\"billing_email\"]")).sendKeys("test@test.test");

        //Соглашаемся с правилами сайта
        //Прокручиваем страницу до элемента
        WebElement terms = driver.findElement(By.id("terms"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", terms);

        //Ожидаем загрузку кнопки для отправки заказа и нажимаем на неё
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div.blockUI.blockOverlay")));
        driver.findElement(By.id("terms")).click();

        //Отправка заказа
        driver.findElement(By.id("place_order")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div.blockUI.blockOverlay")));
        //Получаем сообщение о принятом заказе
        String order_accepted = driver.findElement(By.className("entry-title")).getText();

        //Сравнение фактического результата с ожидаемым
        if (order_accepted.equals("Заказ принят")){
            System.out.printf("Заказ принят\n");
            WebElement orderDetailsList = driver.findElement(By.cssSelector("ul.woocommerce-order-overview"));
            //Вывод деталей заказа
            String order_info = "Детали заказа:\n";
            for (WebElement orderDetail : orderDetailsList.findElements(By.cssSelector("li"))) {
                order_info = order_info + orderDetail.getText() + "\n";
            }
            System.out.printf(order_info);
        }
        else {
            System.out.printf("Заказ не принят: %s", order_accepted);
            fail("Не правильно расчитана стоимость");
        }

        //Закрытие браузера и сообщение об успешном тесте
        pass_end();
        driver.quit();

    }
}
