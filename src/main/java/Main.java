import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.List;

public class Main {

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

            claimDailyReward();

            playAllDuels();

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            if (driver != null) {
                driver.quit();
            }

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

    // ---------------- DAILY REWARD ----------------

    private static void claimDailyReward() {

        try {

            // Open main page
            driver.get("https://elem.cards/");

            sleep(3000);

            // Open reward page
            driver.get("https://elem.cards/dailyreward/");

            sleep(2000);

            List<WebElement> rewards = driver.findElements(
                    By.cssSelector("a[href*='/dailyreward/tnx/']")
            );

            if (rewards.isEmpty()) {

                System.out.println("No daily reward available.");

                return;
            }

            String rewardUrl =
                    rewards.get(0).getAttribute("href");

            System.out.println("Opening reward: " + rewardUrl);

            driver.get(rewardUrl);

            sleep(3000);

            System.out.println("Daily reward claimed ✔");

        } catch (Exception e) {

            System.out.println("Daily reward failed.");

            e.printStackTrace();
        }
    }

    // ---------------- PLAY ALL DUELS ----------------

    private static void playAllDuels() {

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

            duelCount++;

            System.out.println("Starting duel #" + duelCount);

            click(attackBtn.get(0));

            sleep(2000);

            fight();

            clickAnotherDuel();
        }

        System.out.println("All duels completed ✔");
    }

    // ---------------- FIGHT ----------------

    private static void fight() {

        int rounds = 0;

        while (rounds < 50) {

            if (isEnemyDead()) {
                break;
            }

            clickIfPresent("a[href*='attack0']");
            sleep(700);

            clickIfPresent("a[href*='attack1']");
            sleep(700);

            clickIfPresent("a[href*='attack2']");
            sleep(700);

            rounds++;
        }

        System.out.println("Fight completed ✔");
    }

    // ---------------- NEXT DUEL ----------------

    private static void clickAnotherDuel() {

        List<WebElement> btn = driver.findElements(
                By.xpath("//span[contains(text(),'Another duel')]/ancestor::a")
        );

        if (!btn.isEmpty()) {

            click(btn.get(0));

            sleep(2000);
        }
    }

    // ---------------- HELPERS ----------------

    private static boolean isEnemyDead() {

        return !driver.findElements(
                By.xpath("//span[contains(text(),'Another duel')]")
        ).isEmpty();
    }

    private static void clickIfPresent(String css) {

        List<WebElement> el = driver.findElements(
                By.cssSelector(css)
        );

        if (!el.isEmpty()) {

            click(el.get(0));
        }
    }

    private static void click(WebElement el) {

        try {

            el.click();

        } catch (Exception e) {

            try {

                ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", el);

            } catch (Exception ignored) {}
        }
    }

    private static void sleep(int ms) {

        try {

            Thread.sleep(ms);

        } catch (Exception ignored) {}
    }
}
