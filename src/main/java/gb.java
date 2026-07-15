import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.List;

public class gb {

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

            // 1. Go to Guild War page initially
            navigateToGuildWar();

            // 2. Check for the Join link the first time
            String joinUrl = locateWarJoinUrl();

            if (joinUrl != null) {
                System.out.println("Found Join URL on first try: " + joinUrl);
                driver.get(joinUrl);
                sleep(2000);
            } else {
                System.out.println("Join link not found. Going to Main Page as fallback...");
                clickMainPageIcon();
                sleep(3000);
                
                // Go back to the Guild War page
                navigateToGuildWar();

                // 🔥 FIX: Now check for the Join link AGAIN after coming back!
                System.out.println("Checking for Join link again after returning from Main page...");
                joinUrl = locateWarJoinUrl();
                if (joinUrl != null) {
                    System.out.println("Found Join URL on second try: " + joinUrl);
                    driver.get(joinUrl);
                    sleep(2000);
                } else {
                    System.out.println("Join link still not found. Moving straight to combat wait stage.");
                }
            }

            // 3. Wait for combat start and fight
            waitForWarStartAndFight();

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

    private static void navigateToGuildWar() {
        System.out.println("Navigating to Guild War page...");
        driver.get("https://elem.cards/");
        sleep(2000);

        // Locates and clicks the "War: battle starts..." link
        List<WebElement> warLinks = driver.findElements(By.xpath("//a[contains(@href, '/guild/war/')]"));
        if (!warLinks.isEmpty()) {
            click(warLinks.get(0));
            sleep(2000);
        } else {
            // Backup direct navigation if link isn't directly clickable
            driver.get("https://elem.cards/guild/war/");
            sleep(2000);
        }
    }

    private static String locateWarJoinUrl() {
        // Scans for "/guild/war/arena/join/" links
        List<WebElement> joinLinks = driver.findElements(
                By.xpath("//a[contains(@href,'/guild/war/arena/join/')]")
        );

        if (!joinLinks.isEmpty()) {
            return joinLinks.get(0).getAttribute("href");
        }
        return null;
    }

    private static void clickMainPageIcon() {
        List<WebElement> mainPageLinks = driver.findElements(
                By.xpath("//a[@href='/' and .//img[contains(@src, 'ico-bmenu-main.png')]]")
        );
        if (!mainPageLinks.isEmpty()) {
            click(mainPageLinks.get(0));
            System.out.println("Clicked Main page icon.");
        } else {
            driver.get("https://elem.cards/");
        }
    }

    private static void waitForWarStartAndFight() {
        boolean battleStarted = false;
        int attempts = 0;

        // Quick check to see if battle is active right away
        boolean attack0Exists = !driver.findElements(By.cssSelector("a[href*='attack0']")).isEmpty();
        boolean attack1Exists = !driver.findElements(By.cssSelector("a[href*='attack1']")).isEmpty();
        boolean attack2Exists = !driver.findElements(By.cssSelector("a[href*='attack2']")).isEmpty();

        if (attack0Exists || attack1Exists || attack2Exists) {
            System.out.println("Battle is already active! Starting combat.");
            executeWarCombat();
            return;
        }

        System.out.println("No active attacks found. Waiting for battle to start... Polling every 2 seconds.");

        // Wait up to ~10 minutes (300 attempts of 2-second waits)
        while (attempts < 300) {
            attack0Exists = !driver.findElements(By.cssSelector("a[href*='attack0']")).isEmpty();
            attack1Exists = !driver.findElements(By.cssSelector("a[href*='attack1']")).isEmpty();
            attack2Exists = !driver.findElements(By.cssSelector("a[href*='attack2']")).isEmpty();

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
            executeWarCombat();
        } else {
            System.out.println("Timed out waiting for the war battle to initiate.");
        }
    }

    private static void executeWarCombat() {
        System.out.println("Commencing attack spam sequence...");

        long startTime = System.currentTimeMillis();
        long maxDuration = 10 * 60 * 1000; // 10 minutes max fight time
        int consecutiveNoActionCount = 0;

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
                    if (clickMultiplierLink("x 1.6")) actionTaken = true;
                    else if (clickMultiplierLink("x 1.5")) actionTaken = true;
                }
            }

            // 3. Middle-priority check: Fallback to regular x1 paths
            if (!actionTaken) {
                if (clickMultiplierLink("x 1")) {
                    actionTaken = true;
                }
            }

            // 4. Absolute final fallback: Grab lower damage tier x0.5
            if (!actionTaken) {
                if (clickMultiplierLink("x 0.5")) {
                    actionTaken = true;
                }
            }

            // --- SMART EXIT LOGIC ---
            if (!actionTaken) {
                consecutiveNoActionCount++;
                
                // If we found nothing to attack for 10 consecutive refreshes (~15-20 seconds),
                // we assume the war is finished or you ran out of energy.
                if (consecutiveNoActionCount >= 10) {
                    System.out.println("No targets or attacks available for 10 consecutive checks. Ending combat.");
                    break;
                }

                driver.navigate().refresh();
                sleep(1500); // Slightly longer pause when page is idle
            } else {
                consecutiveNoActionCount = 0; // Reset counter since we successfully attacked
                sleep(300); // Fast break between successful hits
            }
        }

        System.out.println("War sequence complete ✔");
    }

    private static boolean clickMultiplierLink(String multiplierText) {
        try {
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
