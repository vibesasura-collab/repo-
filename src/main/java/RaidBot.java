import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.List;

public class RaidBot {

    static WebDriver driver;
    static WebDriverWait wait;

    public static void main(String[] args) {

        String user = System.getenv("GAME_ID");
        String pass = System.getenv("GAME_PASSWORD");

        if (user == null || pass == null) {
            throw new RuntimeException("Missing credentials");
        }

        driver = setup();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {

            login(user, pass);

            while (true) {

                // ---------------- GUILD FLOW ----------------
                click("/guild/");
                click("/guild/graids/");
                click("/guild/graids/tweens/");
                clickJoinRaid();

                System.out.println("Joined raid");

                sleep(2000);

                // ---------------- ATTACK LOOP ----------------
                boolean raidActive = true;

                while (raidActive) {

                    boolean attacked = false;

                    // 1. First priority: Check for x1.5 multiplier link
                    attacked = clickAttackByMultiplier("x 1.5");

                    // 2. If no x1.5 found, invoke the 'Switch' target mechanism
                    if (!attacked) {
                        System.out.println("x1.5 not found. Attempting to Switch target pool...");
                        boolean switched = clickIfExists("a[href*='/chtarget/']");
                        
                        if (switched) {
                            sleep(2000);
                            // Re-evaluate immediate target choices for x1.5 after target pool shifts
                            attacked = clickAttackByMultiplier("x 1.5");
                        }
                    }

                    // 3. Second priority: Fallback to matching a regular x1 multiplier row link
                    if (!attacked) {
                        System.out.println("x1.5 unavailable. Scanning fallback for x1...");
                        attacked = clickAttackByMultiplier("x 1");
                    }

                    // 4. Absolute final resort: Accept the lower x0.5 efficiency tier
                    if (!attacked) {
                        System.out.println("No high tier targets. Opting for fallback x0.5...");
                        attacked = clickAttackByMultiplier("x 0.5");
                    }

                    sleep(2000);

                    // Handles fallback loops if all attack patterns are completely exhausted
                    if (!attacked) {

                        System.out.println("No targeted action sequences executed successfully");

                        // check start digging
                        if (clickIfExists("a[href*='/start_cave/']")) {

                            System.out.println("Start digging clicked");
                            sleep(2000);
                            continue;

                        } else {

                            System.out.println("No start digging → restarting guild flow");
                            raidActive = false;
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    // ---------------- LOGIN ----------------
    private static void login(String user, String pass) {

        driver.get("https://elem.cards/login/");

        driver.findElement(By.name("plogin")).sendKeys(user);
        driver.findElement(By.name("ppass")).sendKeys(pass);
        driver.findElement(By.cssSelector("input[type='submit']")).click();

        sleep(4000);

        try {
            driver.findElement(By.cssSelector("a.urfin")).click();
            sleep(2000);
        } catch (Exception ignored) {}
    }

    // ---------------- NAV CLICK ----------------
    private static void click(String path) {
        try {
            WebElement el = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.cssSelector("a[href='" + path + "']")
                    )
            );
            el.click();
            sleep(2000);
        } catch (Exception e) {
            System.out.println("Failed click: " + path);
        }
    }

    private static void clickJoinRaid() {
        try {
            WebElement el = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.cssSelector("a[href^='/guild/graids/tweens/join/']")
                    )
            );
            el.click();
            sleep(2000);
        } catch (Exception e) {
            System.out.println("Join raid not found");
        }
    }

    // ---------------- ATTACK / START CHECK ----------------
    private static boolean clickIfExists(String css) {

        try {
            List<WebElement> el = driver.findElements(By.cssSelector(css));

            if (el.isEmpty()) return false;

            WebElement e = el.get(0);

            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].scrollIntoView({block:'center'});", e);

            sleep(300);

            try {
                e.click();
            } catch (Exception ex) {
                ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", e);
            }

            sleep(2000);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    // ---------------- HELPER TO MATCH MULTIPLIER TO LINK ----------------
    private static boolean clickAttackByMultiplier(String targetMultiplier) {
        try {
            // Isolates each structural battle row option box on the interface
            List<WebElement> paths = driver.findElements(By.cssSelector("div.fb_path"));
            
            for (WebElement path : paths) {
                String pathText = path.getText();
                
                // Matches the contextual plain text signature of the target multiplier string inside the block
                if (pathText.contains(targetMultiplier)) {
                    // Extract the targeted functional link safely bounded inside the matching structural row elements
                    List<WebElement> links = path.findElements(By.cssSelector("a[href*='/attack']"));
                    if (!links.isEmpty()) {
                        WebElement linkToClick = links.get(0);
                        
                        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", linkToClick);
                        sleep(300);
                        try {
                            linkToClick.click();
                        } catch (Exception ex) {
                            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", linkToClick);
                        }
                        
                        System.out.println("Successfully attacked targeting: " + targetMultiplier);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error processing multiplier matching sequence: " + e.getMessage());
        }
        return false;
    }

    // ---------------- SETUP ----------------
    private static WebDriver setup() {

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");

        return new ChromeDriver(options);
    }

    // ---------------- SLEEP ----------------
    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ignored) {}
    }
}
