import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.List;

public class RaidBot {

    public static void main(String[] args) {

        String user = System.getenv("GAME_ID");
        String pass = System.getenv("GAME_PASSWORD");

        if (user == null || pass == null) {
            throw new RuntimeException("Missing credentials");
        }

        WebDriver driver = setup();

        try {

            login(driver, user, pass);

            // ---------------- GUILD NAVIGATION ----------------
            driver.get("https://elem.cards/guild/");
            sleep(2000);

            driver.get("https://elem.cards/guild/graids/");
            sleep(2000);

            driver.get("https://elem.cards/guild/raids/");
            sleep(2000);

            driver.get("https://elem.cards/guild/raids/dragon_fire/join/");
            sleep(3000);

            System.out.println("Joined raid");

            // ---------------- WAIT FOR RAID START ----------------
            driver.get("https://elem.cards/guild/raids/dragon_fire/");

            int tries = 0;

            while (tries < 60) {

                sleep(3000);

                List<WebElement> a0 = driver.findElements(By.xpath("//a[contains(@href,'attack0')]"));
                List<WebElement> a1 = driver.findElements(By.xpath("//a[contains(@href,'attack1')]"));
                List<WebElement> a2 = driver.findElements(By.xpath("//a[contains(@href,'attack2')]"));

                if (!a0.isEmpty() || !a1.isEmpty() || !a2.isEmpty()) {
                    System.out.println("Raid started!");
                    break;
                }

                System.out.println("Raid not started → refreshing...");
                sleep(10000);
                driver.navigate().refresh();

                tries++;
            }

            // ---------------- ATTACK ----------------
            attack(driver, 0);
            attack(driver, 1);
            attack(driver, 2);

            System.out.println("Raid completed");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    // ---------------- LOGIN ----------------
    private static void login(WebDriver driver, String user, String pass) {

        driver.get("https://elem.cards/login/");

        driver.findElement(By.name("plogin")).sendKeys(user);
        driver.findElement(By.name("ppass")).sendKeys(pass);
        driver.findElement(By.cssSelector("input[type='submit']")).click();

        sleep(4000);

        try {
            driver.findElement(By.cssSelector("a.urfin")).click();
            sleep(2000);
        } catch (Exception ignored) {}
    }

    // ---------------- ATTACK ----------------
    private static void attack(WebDriver driver, int id) {

        try {

            List<WebElement> btn = driver.findElements(
                    By.cssSelector("a[href*='attack" + id + "']")
            );

            if (!btn.isEmpty()) {

                WebElement el = btn.get(0);

                ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].scrollIntoView(true);", el);

                sleep(500);

                try {
                    el.click();
                } catch (Exception e) {
                    ((JavascriptExecutor) driver)
                            .executeScript("arguments[0].click();", el);
                }
            }

        } catch (Exception e) {
            System.out.println("Attack error: " + e.getMessage());
        }
    }

    // ---------------- SETUP ----------------
    private static WebDriver setup() {

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");

        return new ChromeDriver(options);
    }

    // ---------------- SLEEP ----------------
    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ignored) {}
    }
}
