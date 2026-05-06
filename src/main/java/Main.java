import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.List;

public class Main {

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

        try {
            login(driver, user, pass);
            claimDailyReward(driver);
            playAllDuels(driver);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    // ---------------- PLAY ALL DUELS ----------------
    private static void playAllDuels(WebDriver driver) {

        int duelCount = 0;

        while (true) {

            driver.get("https://elem.cards/duel/");
            sleep(2000);

            List<WebElement> attackBtn = driver.findElements(
                    By.xpath("//a[contains(@href,'/duel/tobattle/')]")
            );

            if (attackBtn.isEmpty()) {
                System.out.println("No duels left.");
                break;
            }

            System.out.println("Starting duel #" + (++duelCount));

            click(driver, attackBtn.get(0));
            sleep(2000);

            fight(driver);
            clickAnotherDuel(driver);
        }

        System.out.println("All duels completed ✔");
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
        } catch (Exception ignored) {}

        sleep(3000);
    }

    // ---------------- DAILY REWARD ----------------
    private static void claimDailyReward(WebDriver driver) {

        try {
            List<WebElement> list = driver.findElements(
                    By.xpath("//a[contains(@href,'/dailyreward/')]")
            );

            if (list.isEmpty()) {
                System.out.println("No daily reward.");
                return;
            }

            click(driver, list.get(0));
            System.out.println("Daily reward claimed ✔");

        } catch (Exception e) {
            System.out.println("Daily reward failed: " + e.getMessage());
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
            click(driver, btn.get(0));
            sleep(2000);
        }
    }

    private static boolean isEnemyDead(WebDriver driver) {
        return !driver.findElements(
                By.xpath("//span[contains(text(),'Another duel')]")
        ).isEmpty();
    }

    private static void clickIfPresent(WebDriver driver, String css) {
        List<WebElement> el = driver.findElements(By.cssSelector(css));
        if (!el.isEmpty()) {
            click(driver, el.get(0));
        }
    }

    private static void click(WebDriver driver, WebElement el) {
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
