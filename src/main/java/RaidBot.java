import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.List;

public class RaidBot {

    public static void main(String[] args) {

        WebDriver driver = setup();

        try {

            login(driver);

            driver.get("https://elem.cards/guild/");
            sleep(1500);

            driver.get("https://elem.cards/guild/graids/");
            sleep(1500);

            driver.get("https://elem.cards/guild/raids/");
            sleep(1500);

            driver.get("https://elem.cards/guild/raids/dragon_fire/join/");
            sleep(3000);

            System.out.println("Joined raid");

            driver.get("https://elem.cards/guild/raids/dragon_fire/");

            while (true) {

                sleep(2000);

                List<WebElement> a0 = driver.findElements(By.cssSelector("a[href*='attack0']"));
                List<WebElement> a1 = driver.findElements(By.cssSelector("a[href*='attack1']"));
                List<WebElement> a2 = driver.findElements(By.cssSelector("a[href*='attack2']"));

                if (!a0.isEmpty() || !a1.isEmpty() || !a2.isEmpty()) {
                    System.out.println("Raid started");
                    break;
                }

                System.out.println("Waiting for raid...");
                sleep(10000);
                driver.navigate().refresh();
            }

            attack(driver, 0);
            attack(driver, 1);
            attack(driver, 2);

            System.out.println("Raid complete");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    private static void login(WebDriver driver) {

        driver.get("https://elem.cards/login/");

        driver.findElement(By.name("plogin"))
                .sendKeys(System.getenv("GAME_ID"));

        driver.findElement(By.name("ppass"))
                .sendKeys(System.getenv("GAME_PASSWORD"));

        driver.findElement(By.cssSelector("input[type='submit']")).click();

        sleep(4000);
    }

    private static void attack(WebDriver driver, int id) {

        try {

            List<WebElement> btn = driver.findElements(
                    By.cssSelector("a[href*='attack" + id + "']")
            );

            if (!btn.isEmpty()) {
                ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", btn.get(0));
            }

            sleep(800);

        } catch (Exception ignored) {}
    }

    private static WebDriver setup() {

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        return new ChromeDriver(options);
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ignored) {}
    }
}
