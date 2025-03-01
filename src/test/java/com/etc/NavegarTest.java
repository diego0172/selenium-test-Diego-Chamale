package com.etc;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class NavegarTest {
    WebDriver driver;

    @BeforeTest
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
    }

    @Test
    public void abrirGoogle() {
        driver.get("https://www.google.com");
        System.out.println("Página abierta: " + driver.getTitle());
    }

    @AfterTest
    public void tearDown() {
        driver.quit();
    }
}
