import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.List;

public class ArenaBot {

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

            String joinUrl = locateArenaJoinUrl();

            if (joinUrl != null) {
                System.out.println("Found Join URL: " + joinUrl);
                driver.get(joinUrl);
                sleep(2000);
                waitForArenaStartAndFight();
            } else {
                System.out.println("Could not find an active Arena join link. Checking directly for fights...");
                driver.get("https://elem.cards/guild/arena/");
                waitForArenaStartAndFight();
            }

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
        sleep(4000);

        System.out.println("Login successful ✔");
    }

    private static String locateArenaJoinUrl() {
        driver.get("https://elem.cards/guild/arena/");
        sleep(2000);

        List<WebElement> joinLinks = driver.findElements(
                By.xpath("//a[contains(@href,'/guild/arena/join/')]")
        );

        if (!joinLinks.isEmpty()) {
            return joinLinks.get(0).getAttribute("href");
        }
        return null;
    }

    private static void waitForArenaStartAndFight() {
        boolean battleStarted = false;
        int attempts = 0;

        System.out.println("Waiting for arena to start... Polling every 2 seconds.");

        while (attempts < 1500) {
            boolean attack0Exists = !driver.findElements(By.cssSelector("a[href*='attack0']")).isEmpty();
            boolean attack1Exists = !driver.findElements(By.cssSelector("a[href*='attack1']")).isEmpty();
            boolean attack2Exists = !driver.findElements(By.cssSelector("a[href*='attack2']")).isEmpty();

            if (attack0Exists || attack1Exists || attack2Exists) {
                System.out.println("Battle Started! Attack links detected.");
                battleStarted = true;
                break;
            }

            driver.navigate().refresh();
            sleep(2000);
            attempts++;
        }

        if (battleStarted) {
            executeArenaCombat();
        } else {
            System.out.println("Timed out waiting for the arena match to initiate.");
        }
    }

    // 🔥 ONLY THIS PART CHANGED
    private static void executeArenaCombat() {

        System.out.println("Commencing attack spam sequence...");

        long startTime = System.currentTimeMillis();
        long maxDuration = 5 * 60 * 1000; // 5 minutes

        int loops = 0;

        while (System.currentTimeMillis() - startTime < maxDuration) {

            boolean actionTaken = false;

            if (clickIfPresent("a[href*='attack0']")) actionTaken = true;
            if (clickIfPresent("a[href*='attack1']")) actionTaken = true;
            if (clickIfPresent("a[href*='attack2']")) actionTaken = true;

            if (!actionTaken) {
                System.out.println("No attack found, waiting for next wave...");
            }

            sleep(2000);
            driver.navigate().refresh();

            loops++;
        }

        System.out.println("5 minutes reached — Arena sequence complete ✔");
    }

    private static boolean clickIfPresent(String css) {
        List<WebElement> elements = driver.findElements(By.cssSelector(css));
        if (!elements.isEmpty()) {
            click(elements.get(0));
            return true;
        }
        return false;
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
