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

    // 🔥 ONLY THIS PART CHANGED (Optimized for maximum speed)
    private static void executeArenaCombat() {

        System.out.println("Commencing attack spam sequence...");

        long startTime = System.currentTimeMillis();
        long maxDuration = 5 * 60 * 1000; // 5 minutes

        while (System.currentTimeMillis() - startTime < maxDuration) {

            boolean actionTaken = false;

            // 1. High-priority checks: Check for x1.6 or x1.5 matches instantly
            if (clickMultiplierLink("x 1.6")) {
                actionTaken = true;
            } else if (clickMultiplierLink("x 1.5")) {
                actionTaken = true;
            }

            // 2. If high multipliers aren't on the page, hit 'Switch' to roll for them
            if (!actionTaken) {
                boolean switched = clickIfPresent("a[href*='/chtarget/']");
                if (switched) {
                    sleep(400); // Quick brief sleep for switch state updates
                    // Instantly retry for top tiers right after switching
                    if (clickMultiplierLink("x 1.6")) actionTaken = true;
                    else if (clickMultiplierLink("x 1.5")) actionTaken = true;
                }
            }

            // 3. Middle-priority check: Fallback to regular x1 paths if top targets missing
            if (!actionTaken) {
                if (clickMultiplierLink("x 1")) {
                    actionTaken = true;
                }
            }

            // 4. Absolute final fallback: Grab lower damage tier x0.5 as last option
            if (!actionTaken) {
                if (clickMultiplierLink("x 0.5")) {
                    actionTaken = true;
                }
            }

            // If absolutely nothing was found (stuck screen or refresh delay required)
            if (!actionTaken) {
                driver.navigate().refresh();
                sleep(1000);
            } else {
                sleep(300); // Small, ultra-fast break between clicks to run smoothly
            }
        }

        System.out.println("5 minutes reached — Arena sequence complete ✔");
    }

    // 🚀 High-speed selector to instantly bridge the gap from text element straight to the link
    private static boolean clickMultiplierLink(String multiplierText) {
        try {
            // Locates the a tag inside any row that matches the targeted multiplier string contextually
            String xpath = "//div[@class='fb_path' and contains(., '" + multiplierText + "')]//a[contains(@href, '/attack')]";
            List<WebElement> targetLinks = driver.findElements(By.xpath(xpath));
            
            if (!targetLinks.isEmpty()) {
                click(targetLinks.get(0));
                return true;
            }
        } catch (Exception ignored) {}
        return false;
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
