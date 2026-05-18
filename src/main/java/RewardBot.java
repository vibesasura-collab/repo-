import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.List;

public class RewardBot {

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

            openMainPage();

            openDailyPage();

            runRewardMode();

            System.out.println("Reward Mode Completed ✔");

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

    // ---------------- MAIN PAGE ----------------

    private static void openMainPage() {

        driver.findElement(By.cssSelector("a[href='/']")).click();
        sleep(3000);

        System.out.println("Main Page opened ✔");
    }

    // ---------------- DAILY PAGE ----------------

    private static void openDailyPage() {

        driver.findElement(By.cssSelector("a[href='/daily/']")).click();
        sleep(3000);

        System.out.println("Daily Page opened ✔");
    }

    // ---------------- REWARD MODE ----------------

    private static void runRewardMode() {

        System.out.println("Checking Rewards...");

        clickOnce("/daily/reward/win_duels/");
        clickOnce("/daily/reward/duels/");
        clickOnce("/daily/reward/arenas/");
        clickOnce("/daily/reward/get_dungeon_cards/");
        clickOnce("/daily/reward/win_arenas/");
        clickOnce("/daily/reward/win_arenas/");

        System.out.println("All Rewards Checked ✔");
    }

    // ---------------- SAFE CLICK ----------------

    private static void clickOnce(String path) {

        List<WebElement> el = driver.findElements(
                By.cssSelector("a[href='" + path + "']")
        );

        if (!el.isEmpty()) {

            try {
                el.get(0).click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", el.get(0));
            }

            sleep(2000);
            System.out.println("Clicked: " + path);

        } else {
            System.out.println("Not found: " + path);
        }
    }

    // ---------------- SLEEP ----------------

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ignored) {}
    }
}
