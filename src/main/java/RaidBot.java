import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
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

            // ---------------- GUILD FLOW ----------------
            driver.get("https://elem.cards/guild/");
            sleep(2000);

            driver.get("https://elem.cards/guild/graids/");
            sleep(2000);

            driver.get("https://elem.cards/guild/raids/");
            sleep(2000);

            driver.get("https://elem.cards/guild/raids/dragon_fire/join/");
            sleep(3000);

            System.out.println("Joined raid");

            // ---------------- RAID WAIT LOOP ----------------
            driver.get("https://elem.cards/guild/raids/dragon_fire/");

            int tries = 0;
            boolean started = false;

            while (tries < 60 && !started) {

                sleep(3000);

                if (isRaidStarted(driver)) {
                    System.out.println("Raid started!");
                    started = true;
                    break;
                }

                System.out.println("Raid not started → refreshing...");
                sleep(10000);
                driver.navigate().refresh();

                tries++;
            }

            if (!started) {
                System.out.println("Raid not started in time.");
                return;
            }

            // ---------------- ATTACK SEQUENCE ----------------
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

    // ---------------- RAID DETECTION ----------------
    private static boolean isRaidStarted(WebDriver driver) {

        List<WebElement> buttons = driver.findElements(By.xpath(
                "//a[contains(@href,'attack0') or contains(@href,'attack1') or contains(@href,'attack2')]"
        ));

        return !buttons.isEmpty();
    }

    // ---------------- ATTACK ----------------
    private static void attack(WebDriver driver, int id) {

        try {

            List<WebElement> btn = driver.findElements(
                    By.cssSelector("a[href*='attack" + id + "']")
            );

            if (btn.isEmpty()) return;

            WebElement el = btn.get(0);

            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].scrollIntoView({block:'center'});", el);

            sleep(500);

            try {
                el.click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", el);
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

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        return driver;
    }

    // ---------------- SLEEP ----------------
    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ignored) {}
    }
}
