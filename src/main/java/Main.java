import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class Main {

    private static final int MAX_RUN_MINUTES = 345;

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

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        Instant start = Instant.now();

        try {

            // LOGIN
            driver.get("https://elem.cards/login/");

            driver.findElement(By.name("plogin")).sendKeys(user);
            driver.findElement(By.name("ppass")).sendKeys(pass);
            driver.findElement(By.cssSelector("input[type='submit']")).click();

            sleep(4000);

            driver.findElement(By.cssSelector("a.urfin")).click();
            sleep(3000);

            // DAILY REWARD
            claimDailyReward(driver, wait);

            // DUEL LOOP
            while (!shouldStop(start)) {

                driver.get("https://elem.cards/duel/");
                sleep(2000);

                List<WebElement> attackBtn = driver.findElements(
                        By.xpath("//a[contains(@href,'/duel/tobattle/')]")
                );

                if (attackBtn.isEmpty()) {
                    System.out.println("No duels left.");
                    break;
                }

                attackBtn.get(0).click();
                sleep(2000);

                fight(driver);

                clickAnotherDuel(driver);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    // ---------------- DAILY REWARD FIXED ----------------
    private static void claimDailyReward(WebDriver driver, WebDriverWait wait) {

        try {

            By locator = By.xpath(
                    "//a[contains(@href,'dailyreward') and .//span[contains(.,'Receive')]]"
            );

            WebElement btn = wait.until(
                    ExpectedConditions.elementToBeClickable(locator)
            );

            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].scrollIntoView({block:'center'});", btn);

            sleep(700);

            try {
                btn.click();
                System.out.println("Daily reward clicked (normal)");
            } catch (Exception e) {
                ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", btn);
                System.out.println("Daily reward clicked (JS)");
            }

            sleep(2000);

        } catch (Exception e) {
            System.out.println("Daily reward not available / already claimed");
        }
    }

    // ---------------- FIGHT ----------------
    private static void fight(WebDriver driver) {

        int rounds = 0;

        while (rounds < 50) {

            if (isEnemyDead(driver)) break;

            clickIfPresent(driver, "a[href*='attack0']");
            sleep(800);

            clickIfPresent(driver, "a[href*='attack1']");
            sleep(800);

            clickIfPresent(driver, "a[href*='attack2']");
            sleep(800);

            rounds++;
        }
    }

    // ---------------- NEXT DUEL ----------------
    private static void clickAnotherDuel(WebDriver driver) {

        List<WebElement> btn = driver.findElements(
                By.xpath("//span[contains(text(),'Another duel')]/ancestor::a")
        );

        if (!btn.isEmpty()) {
            try {
                btn.get(0).click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", btn.get(0));
            }
            sleep(2000);
        }
    }

    // ---------------- HELPERS ----------------
    private static boolean isEnemyDead(WebDriver driver) {
        return !driver.findElements(By.xpath("//span[contains(text(),'Another duel')]")).isEmpty();
    }

    private static void clickIfPresent(WebDriver driver, String css) {
        List<WebElement> el = driver.findElements(By.cssSelector(css));
        if (!el.isEmpty()) {
            try {
                el.get(0).click();
            } catch (Exception ignored) {}
        }
    }

    private static boolean shouldStop(Instant start) {
        return Duration.between(start, Instant.now()).toMinutes() >= MAX_RUN_MINUTES;
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ignored) {}
    }
}
