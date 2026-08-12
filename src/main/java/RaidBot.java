import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RaidBot {

    static WebDriver driver;
    static WebDriverWait wait;
    private static final String BASE_URL = "https://elem.cards";

    public static void main(String[] args) {

        String user = System.getenv("GAME_ID");
        String pass = System.getenv("GAME_PASSWORD");

        if (user == null || pass == null) {
            throw new RuntimeException("Missing credentials");
        }

        driver = setup();
        wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        try {
            login(user, pass);

            while (true) {
                driver.get(BASE_URL + "/guild/graids/tweens/");
                sleep(1000, 1800); // Increased page load wait

                clickJoinRaid();
                System.out.println("Joined raid");
                sleep(1200, 2000); // Increased post-join delay

                // ---------------- ATTACK LOOP ----------------
                boolean raidActive = true;

                while (raidActive) {
                    boolean attacked = false;

                    // 1. Priority: x1.5 multiplier
                    attacked = clickAttackByMultiplier("x 1.5");

                    // 2. Switch target pool if x1.5 not found
                    if (!attacked) {
                        if (clickIfExists("a[href*='/chtarget/']")) {
                            sleep(1000, 1800); // Increased target switch delay
                            attacked = clickAttackByMultiplier("x 1.5");
                        }
                    }

                    // 3. Fallback: x1 multiplier
                    if (!attacked) {
                        attacked = clickAttackByMultiplier("x 1");
                    }

                    // 4. Final resort: x0.5 multiplier
                    if (!attacked) {
                        attacked = clickAttackByMultiplier("x 0.5");
                    }

                    // Pacing delay between attacks (1.2 - 2.2 seconds)
                    if (attacked) {
                        sleep(1200, 2200);
                    } else {
                        System.out.println("No targeted action sequences executed successfully");

                        if (clickIfExists("a[href*='/start_cave/']")) {
                            System.out.println("Start digging clicked");
                            sleep(1500, 2500); // Increased post-dig delay
                        } else {
                            System.out.println("No start digging -> restarting guild flow");
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
        driver.get(BASE_URL + "/login/");

        WebElement userField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("plogin")));
        userField.sendKeys(user);
        
        WebElement passField = driver.findElement(By.name("ppass"));
        passField.sendKeys(pass);

        WebElement submit = driver.findElement(By.cssSelector("input[type='submit']"));
        quickClick(submit);

        sleep(1500, 2500); // Increased post-login navigation delay

        try {
            List<WebElement> urfinList = driver.findElements(By.cssSelector("a.urfin"));
            if (!urfinList.isEmpty()) {
                quickClick(urfinList.get(0));
                sleep(1000, 1800);
            }
        } catch (Exception ignored) {}
    }

    private static void clickJoinRaid() {
        try {
            WebElement el = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href^='/guild/graids/tweens/join/']"))
            );
            quickClick(el);
        } catch (Exception e) {
            System.out.println("Join raid not found");
        }
    }

    // ---------------- ATTACK / START CHECK ----------------
    private static boolean clickIfExists(String css) {
        try {
            List<WebElement> list = driver.findElements(By.cssSelector(css));
            if (list.isEmpty()) return false;
            
            quickClick(list.get(0));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ---------------- MULTIPLIER MATCHING ----------------
    private static boolean clickAttackByMultiplier(String targetMultiplier) {
        try {
            String xpath = String.format("//div[contains(@class,'fb_path') and contains(., '%s')]//a[contains(@href, '/attack')]", targetMultiplier);
            List<WebElement> links = driver.findElements(By.xpath(xpath));

            if (!links.isEmpty()) {
                quickClick(links.get(0));
                System.out.println("Successfully attacked targeting: " + targetMultiplier);
                return true;
            }
        } catch (Exception e) {
            System.out.println("Error processing multiplier matching: " + e.getMessage());
        }
        return false;
    }

    // ---------------- PACED CLICK / SLEEP ----------------

    // Pre-click micro-pause (200-450ms)
    private static void quickClick(WebElement element) {
        try {
            sleep(200, 450);
            element.click();
        } catch (Exception ex) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    private static void sleep(int minMs, int maxMs) {
        try {
            int delay = ThreadLocalRandom.current().nextInt(minMs, maxMs + 1);
            Thread.sleep(delay);
        } catch (Exception ignored) {}
    }

    // ---------------- DRIVER SETUP ----------------
    private static WebDriver setup() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");

        return new ChromeDriver(options);
    }
}
