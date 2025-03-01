package com.etc;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.time.Duration;
import java.util.Set;

public class agentes {
    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().browserVersion("125.0.6422.141").setup();
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().window().maximize(); // Asegurar que cada prueba abre una nueva ventana maximizada
    }

    @Test
    public void testGoogleBusqueda() throws InterruptedException {
        driver.get("https://www.google.com");
        WebElement searchBox = driver.findElement(By.name("q"));
        searchBox.sendKeys("Barcelona");
        searchBox.submit();

        // Verificar si aparece el CAPTCHA
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement captchaCheckbox = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//iframe[contains(@title, 'reCAPTCHA')]")));
            driver.switchTo().frame(captchaCheckbox);
            WebElement recaptchaBox = driver.findElement(By.cssSelector(".recaptcha-checkbox-border"));
            recaptchaBox.click();
            driver.switchTo().defaultContent();

            // Esperar a que el CAPTCHA se resuelva antes de continuar
            Thread.sleep(4000);
        } catch (Exception e) {
            System.out.println("No se encontró CAPTCHA, continuando con la búsqueda.");
        }

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.titleContains("Barcelona"));
        Thread.sleep(4000); // Mantener la ventana abierta por más tiempo
    }

    @Test
    public void testYoutubeBusqueda() throws InterruptedException {
        driver.get("https://www.youtube.com");
        WebElement searchBox = driver.findElement(By.name("search_query"));
        searchBox.sendKeys("Las mejores jugadas de Lionel Messi");
        searchBox.submit();
        Thread.sleep(4000);
    }

    @Test
    public void testGmailHome() throws InterruptedException {
        driver.get("https://mail.google.com");
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        // Escribir "Hola" en el campo de correo si está presente
        try {
            WebElement emailInput = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@type='email']")));
            emailInput.sendKeys("Hola");
        } catch (Exception e) {
            System.out.println("No se encontró el campo de correo.");
        }
        Thread.sleep(4000);
    }

    @Test
    public void testNuestroDiario() throws InterruptedException {
        driver.get("https://www.nuestrodiario.com");
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        Thread.sleep(4000);
    }

    @Test
    public void testManejoDeMultiplesVentanas() throws InterruptedException {
        driver.get("https://www.twitch.tv/");

        // Guardar el identificador de la ventana original
        String ventanaOriginal = driver.getWindowHandle();

        // Abrir una nueva pestaña con JavaScript
        ((JavascriptExecutor) driver).executeScript("window.open('https://www.youtube.com', '_blank');");

        // Esperar a que haya 2 ventanas abiertas
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.numberOfWindowsToBe(2));

        // Obtener todos los manejadores de ventana
        Set<String> ventanas = driver.getWindowHandles();

        // Cambiar a la nueva ventana
        for (String ventana : ventanas) {
            if (!ventana.equals(ventanaOriginal)) {
                driver.switchTo().window(ventana);
                break;
            }
        }

        // Esperar a que el título de la nueva ventana contenga "YouTube"
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.titleContains("YouTube"));

        // Mantener la ventana abierta por más tiempo
        Thread.sleep(4000);

        // Cerrar la nueva ventana y volver a la original
        driver.close();
        driver.switchTo().window(ventanaOriginal);
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
