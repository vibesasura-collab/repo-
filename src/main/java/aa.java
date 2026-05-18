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
            playDungeon();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) driver.quit();
            System.exit(0);
        }
    }

    // ---------------- LOGIN ----------------

    private static void login(String user, String pass) {

        driver.get("https://elem.cards/login/");
        sleep(3000);

        driver.findElement(By.name("plogin")).sendKeys(user);
        driver.findElement(By.name("ppass")).sendKeys(pass);
        driver.findElement(By.cssSelector("input[type='submit']")).click();

        sleep(5000);

        System.out.println("Login successful ✔");
    }

    // ---------------- DUNGEON BOT ----------------

    private static void playDungeon() {

        for (int stage = 1; stage <= 3; stage++) {

            System.out.println("==================================");
            System.out.println("Starting Stage: " + stage);

            driver.get("https://elem.cards/dungeon/" + stage + "/start/");

            sleep(4000);

            int idleChecks = 0;

            while (true) {

                List<WebElement> attacks = driver.findElements(
                        By.cssSelector("a[href*='/dungeon/attack']")
                );

                if (attacks.isEmpty()) {

                    idleChecks++;
                    sleep(1000);

                    if (idleChecks >= 5) {
                        break;
                    }

                    continue;
                }

                idleChecks = 0;

                System.out.println("Attacking... remaining: " + attacks.size());

                try {
                    safeClick(attacks.get(0));
                } catch (Exception e) {
                    System.out.println("Click retry...");
                }

                sleep(2000);
            }

            System.out.println("Stage " + stage + " cleared ✔");

            driver.get("https://elem.cards/dungeon/");
            sleep(2000);
        }

        System.out.println("Dungeon completed ✔");
    }

    // ---------------- SAFE CLICK (ONLY CHANGE) ----------------

    private static void safeClick(WebElement el) {

        try {
            el.click();
        } catch (Exception e) {
            try {
                ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", el);
            } catch (Exception ignored) {}
        }
    }

    // ---------------- SLEEP ----------------

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ignored) {}
    }
}
