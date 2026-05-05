import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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
        Instant startTime = Instant.now();

        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

            // ---------------- LOGIN ----------------
            driver.get("https://elem.cards/login/");
            sleep(2000);

            driver.findElement(By.name("plogin")).sendKeys(user);
            driver.findElement(By.name("ppass")).sendKeys(pass);
            driver.findElement(By.cssSelector("input[type='submit']")).click();

            sleep(4000);

            driver.findElement(By.cssSelector("a.urfin")).click();
            sleep(3000);

            // ---------------- DAILY REWARD ----------------
            claimDailyReward(driver);

            // ---------------- MAIN LOOP ----------------
            while (!shouldStop(startTime)) {

                // GO TO DUELS PAGE
                driver.get("https://elem.cards/duel/");
                sleep(2000);

                // Check if duels exist
                List<WebElement> attackBtn = driver.findElements(
                        By.xpath("//a[contains(@href,'/duel/tobattle/')]")
                );

                if (attackBtn.isEmpty()) {
                    System.out.println("No duels available.");
                    break;
                }

                attackBtn.get(0).click();
                sleep(2000);

                // ================= FIGHT AREA =================
                // 👉 INSERT YOUR EXISTING ATTACK LOGIC HERE
                runFightLoop(driver);

                // After fight
                clickAnotherDuel(driver);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    // ---------------- DAILY REWARD ----------------
    private static void claimDailyReward(WebDriver driver) {
        try {
            List<WebElement> rewardBtn = driver.findElements(
                    By.xpath("//a[contains(@href,'/dailyreward') and .//span[text()='Receive']]")
            );

            if (!rewardBtn.isEmpty()) {
                System.out.println("Claiming daily reward...");
                rewardBtn.get(0).click();
                sleep(2000);
            } else {
                System.out.println("No daily reward.");
            }

        } catch (Exception e) {
            System.out.println("Reward check failed.");
        }
    }

    // ---------------- FIGHT LOOP (placeholder) ----------------
    private static void runFightLoop(WebDriver driver) {
        int rounds = 0;

        while (rounds < 50) {

            if (isEnemyDead(driver)) {
                break;
            }

            clickIfPresent(driver, "a[href*='attack0']");
            sleep(1000);

            clickIfPresent(driver, "a[href*='attack1']");
            sleep(1000);

            clickIfPresent(driver, "a[href*='attack2']");
            sleep(1000);

            rounds++;
        }
    }

    // ---------------- AFTER FIGHT ----------------
    private static void clickAnotherDuel(WebDriver driver) {
        try {
            List<WebElement> btn = driver.findElements(
                    By.xpath("//span[text()='Another duel']/ancestor::a")
            );

            if (!btn.isEmpty()) {
                btn.get(0).click();
                sleep(2000);
            }

        } catch (Exception ignored) {}
    }

    // ---------------- HELPERS ----------------
    private static boolean isEnemyDead(WebDriver driver) {
        return !driver.findElements(By.xpath("//span[text()='Another duel']")).isEmpty();
    }

    private static void clickIfPresent(WebDriver driver, String css) {
        List<WebElement> el = driver.findElements(By.cssSelector(css));
        if (!el.isEmpty()) {
            try { el.get(0).click(); } catch (Exception ignored) {}
        }
    }

    private static boolean shouldStop(Instant start) {
        long mins = Duration.between(start, Instant.now()).toMinutes();
        return mins >= MAX_RUN_MINUTES;
    }

    private static void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
