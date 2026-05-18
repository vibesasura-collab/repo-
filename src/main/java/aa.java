import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.List;

public class aa {

    private static WebDriver driver;

    public static void main(String[] args) {

        String user = System.getenv("GAME_ID");
        String pass = System.getenv("GAME_PASSWORD");

        if (user == null || pass == null) {
            throw new RuntimeException("Missing credentials");
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

            login(user, pass);

            playDungeon();   // 🔥 ONLY FEATURE

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) driver.quit();
            System.exit(0);
        }
    }

    // ---------------- LOGIN ONLY ----------------

    private static void login(String user, String pass) {

        driver.get("https://elem.cards/login/");
        sleep(3000);

        driver.findElement(By.name("plogin")).sendKeys(user);
        driver.findElement(By.name("ppass")).sendKeys(pass);
        driver.findElement(By.cssSelector("input[type='submit']")).click();

        sleep(5000);

        System.out.println("Login successful ✔");
    }

    // ---------------- DUNGEON MODE ----------------

    private static void playDungeon() {

        int stage = 1;

        while (true) {

            System.out.println("==================================");
            System.out.println("Dungeon Stage: " + stage);

            driver.get("https://elem.cards/dungeon/" + stage + "/start/");
            sleep(3000);

            while (true) {

                List<WebElement> attacks = driver.findElements(
                        By.cssSelector("a[href*='/dungeon/attack']")
                );

                if (attacks.isEmpty()) {
                    break;
                }

                click(attacks.get(0));
                sleep(2000);
            }

            System.out.println("Stage " + stage + " cleared ✔");

            driver.get("https://elem.cards/dungeon/");
            sleep(2000);

            stage++;

            // safety limit (remove if you want infinite farming)
            if (stage > 20) {
                System.out.println("Dungeon run finished ✔");
                break;
            }
        }
    }

    // ---------------- HELPERS ----------------

    private static void click(WebElement el) {

        try {
            el.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].click();", el);
        }
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ignored) {}
    }
}
