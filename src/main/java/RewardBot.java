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

            openMain();
            openDaily();

            runRewards();

            System.out.println("All Rewards Completed ✔");

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

    // ---------------- NAVIGATION ----------------

    private static void openMain() {

        List<WebElement> el = driver.findElements(By.cssSelector("a[href='/']"));
        if (!el.isEmpty()) {
            el.get(0).click();
        }

        sleep(2000);
    }

    private static void openDaily() {

        List<WebElement> el = driver.findElements(By.cssSelector("a[href='/daily/']"));
        if (!el.isEmpty()) {
            el.get(0).click();
        }

        sleep(3000);
    }

    // ---------------- REWARDS ----------------

    private static void runRewards() {

        clickReward("win_duels");
        clickReward("duels");
        clickReward("arenas");
        clickReward("get_dungeon_cards");
        clickReward("win_arenas");
        clickReward("improve_cards"); // ✅ FIXED ONE (your issue)

    }

    // ---------------- FIXED SAFE CLICK ----------------

    private static void clickReward(String path) {

        for (int i = 0; i < 8; i++) {

            List<WebElement> el = driver.findElements(
                    By.cssSelector("a[href*='" + path + "']")
            );

            if (!el.isEmpty()) {

                try {
                    el.get(0).click();
                } catch (Exception e) {
                    ((JavascriptExecutor) driver)
                            .executeScript("arguments[0].click();", el.get(0));
                }

                System.out.println("Clicked reward ✔: " + path);
                sleep(2000);
                return;
            }

            sleep(1000);
        }

        System.out.println("Skipped (not found): " + path);
    }

    // ---------------- SLEEP ----------------

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ignored) {}
    }
}
