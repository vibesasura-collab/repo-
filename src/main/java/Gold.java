import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.List;

public class Gold {

    private static WebDriver driver;

    public static void main(String[] args) {
        String user = System.getenv("GAME_ID");
        String pass = System.getenv("GAME_PASSWORD");

        if (user == null || pass == null) {
            throw new RuntimeException("Missing credentials in environment variables.");
        }

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);

        try {
            // Step 1: Login
            login(user, pass);

            // Step 2: Extract and print Gold count
            checkAndPrintGold();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
            System.exit(0);
        }
    }

    private static void login(String user, String pass) {
        driver.get("https://elem.cards/login/");
        sleep(2000);

        driver.findElement(By.name("plogin")).sendKeys(user);
        driver.findElement(By.name("ppass")).sendKeys(pass);
        driver.findElement(By.cssSelector("input[type='submit']")).click();
        sleep(3000);

        System.out.println("Login successful ✔");
    }

    private static void checkAndPrintGold() {
        try {
            List<WebElement> goldElements = driver.findElements(By.className("c_gold"));
            if (!goldElements.isEmpty()) {
                String goldAmount = goldElements.get(0).getText().trim();
                System.out.println("Gold Count: " + goldAmount);
            } else {
                System.out.println("Gold element (span.c_gold) not found after login.");
            }
        } catch (Exception e) {
            System.out.println("Failed to read gold count: " + e.getMessage());
        }
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ignored) {}
    }
}
