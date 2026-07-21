import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.List;

public class ArenBot {

    private static WebDriver driver;

    public static void main(String[] args) {
        String user = System.getenv("GAME_ID");
        String pass = System.getenv("GAME_PASSWORD");

        if (user == null || pass == null) {
            throw new RuntimeException("Missing credentials in environment variables.");
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
            // Step 1: Login
            login(user, pass);

            // Step 2: Handle Daily Reward (if present)
            claimDailyReward();

            // Step 3: Run target sequence loop 3 times
            for (int cycle = 1; cycle <= 3; cycle++) {
                System.out.println("Starting Guild War Cycle " + cycle + " of 3...");
                runGuildWarSequence();
            }

            System.out.println("All 3 cycles completed successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
            System.exit(0);
        }
    }

    private static void login(String user, String pass) {
        driver.get("https://elem.cards/login/");
        sleep(2000);

        driver.findElement(By.name("plogin")).sendKeys(user);
        driver.findElement(By.name("ppass")).sendKeys(pass);
        driver.findElement(By.cssSelector("input[type='submit']")).click();
        sleep(3000);

        System.out.println("Login successful ✔");
    }

    private static void claimDailyReward() {
        // Matches relative dynamic links containing /dailyreward/tnx/
        List<WebElement> rewards = driver.findElements(By.xpath("//a[contains(@href, '/dailyreward/tnx/')]"));
        if (!rewards.isEmpty()) {
            System.out.println("Claiming daily reward...");
            click(rewards.get(0));
            sleep(2000);
        }
    }

    private static void runGuildWarSequence() {
        // 1. Click Guild menu icon
        List<WebElement> guildBtn = driver.findElements(By.xpath("//a[@href='/guild/']"));
        if (!guildBtn.isEmpty()) {
            click(guildBtn.get(0));
            sleep(2000);
        } else {
            driver.get("https://elem.cards/guild/");
            sleep(2000);
        }

        // 2. Click War / Keys section
        List<WebElement> warBtn = driver.findElements(By.xpath("//a[contains(@href, '/guild/war/')]"));
        if (!warBtn.isEmpty()) {
            click(warBtn.get(0));
            sleep(2000);
        }

        // 3. Find target list links and click the BOTTOM-MOST target available
        List<WebElement> attackTargets = driver.findElements(By.xpath("//a[contains(@href, '/guild/war/attack/')]"));
        if (!attackTargets.isEmpty()) {
            WebElement bottomTarget = attackTargets.get(attackTargets.size() - 1);
            click(bottomTarget);
            sleep(2000);
        } else {
            System.out.println("No attack targets found in list.");
            return;
        }

        // 4. Click Confirmation "Attack" button
        List<WebElement> confirmBtn = driver.findElements(By.xpath("//a[contains(@href, '/confirmed/')]"));
        if (!confirmBtn.isEmpty()) {
            click(confirmBtn.get(0));
            sleep(2000);
        }

        // 5. Loop clicking card duel targets until no cards are left
        System.out.println("Starting duel card attacks...");
        int attackCount = 0;
        while (true) {
            List<WebElement> cardAttacks = driver.findElements(By.xpath("//a[contains(@href, '/guild/war/duel/') and contains(@href, '/attack')]"));
            if (cardAttacks.isEmpty()) {
                break;
            }

            click(cardAttacks.get(0));
            attackCount++;
            sleep(1000); // Short delay to prevent server rate limiting
        }

        System.out.println("Completed " + attackCount + " card attacks in this cycle.");
    }

    private static void click(WebElement el) {
        try {
            el.click();
        } catch (Exception e) {
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
            } catch (Exception ignored) {}
        }
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ignored) {}
    }
}
