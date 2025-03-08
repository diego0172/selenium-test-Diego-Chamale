import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SeleniumTest {
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeTest
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();


        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));


        ((JavascriptExecutor) driver).executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
    }

    @Test
    public void testSeleniumDocumentationNavigation() throws IOException, InterruptedException {
        driver.get("https://www.google.com");
        randomPause();

        WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(By.name("q")));
        searchBox.sendKeys("Documentación de Selenium");
        randomPause();
        searchBox.sendKeys(Keys.ENTER);


        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("search")));


        ((JavascriptExecutor) driver).executeScript("document.activeElement.blur();");

        takeScreenshot("1_busqueda_google.png", By.name("q"));

        WebElement seleniumDocLink = findSeleniumDocumentationLink();
        if (seleniumDocLink != null) {
            clickWithJs(seleniumDocLink);
        } else {
            System.out.println("No se encontró el enlace de la documentación.");
            return;
        }

        takeScreenshot("2_documentacion_selenium.png", By.tagName("h1"));

        String[] menuItems = {"Overview", "WebDriver", "Selenium Manager", "Grid", "IE Driver Server", "IDE", "Test Practices", "Legacy", "About"};

        for (String menuItem : menuItems) {
            try {
                WebElement menuElement = clickElementWithRetry(By.linkText(menuItem));
                randomPause();
                takeScreenshot("menu_" + menuItem + ".png", By.linkText(menuItem));
            } catch (TimeoutException e) {
                System.out.println(" No se pudo encontrar el menú lateral: " + menuItem);
            }
        }
    }

    @AfterTest
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }


    private void takeScreenshot(String fileName, By locator) throws IOException {
        try {
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            BufferedImage img = ImageIO.read(srcFile);


            WebElement highlightElement = null;
            try {
                highlightElement = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            } catch (StaleElementReferenceException e) {
                System.out.println("⚠️ Elemento obsoleto antes de la captura, no se resaltará.");
            }


            if (highlightElement != null && highlightElement.isDisplayed()) {
                org.openqa.selenium.Point location = highlightElement.getLocation();
                Dimension size = highlightElement.getSize();
                Graphics2D g2d = img.createGraphics();
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRect(location.getX(), location.getY(), size.getWidth(), size.getHeight());
                g2d.dispose();
            }

            File outputDir = new File("screenshots");
            if (!outputDir.exists()) outputDir.mkdirs();
            File outputFile = new File(outputDir, fileName);
            ImageIO.write(img, "png", outputFile);
        } catch (IOException e) {
            System.out.println("⚠️ Error al guardar la captura de pantalla: " + fileName);
        }
    }


    private void clickWithJs(WebElement element) {
        try {
            element.click();
        } catch (ElementClickInterceptedException e) {
            System.out.println("Elemento bloqueado, intentando hacer clic con JavaScript...");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }


    private WebElement clickElementWithRetry(By locator) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                WebElement element = wait.until(ExpectedConditions.refreshed(ExpectedConditions.elementToBeClickable(locator)));
                element.click();
                return element;
            } catch (StaleElementReferenceException e) {
                System.out.println(" Elemento obsoleto, reintentando... (" + (attempts + 1) + "/3)");
            }
            attempts++;
        }
        throw new RuntimeException("No se pudo hacer clic en el elemento después de 3 intentos.");
    }


    private WebElement findSeleniumDocumentationLink() {
        List<By> strategies = List.of(
                By.xpath("//a[contains(@href, 'selenium.dev/documentation')]"),
                By.linkText("Selenium Documentation"),
                By.partialLinkText("Selenium")
        );

        for (By strategy : strategies) {
            try {
                WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(strategy));

                // 🔹 Hacer scroll hasta el enlace
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
                //randomPause();

                return wait.until(ExpectedConditions.elementToBeClickable(element));
            } catch (TimeoutException | StaleElementReferenceException ignored) {}
        }
        return null;
    }


    private void randomPause() throws InterruptedException {
        int randomSleep = ThreadLocalRandom.current().nextInt(2000, 5000);
        Thread.sleep(randomSleep);
    }
}
