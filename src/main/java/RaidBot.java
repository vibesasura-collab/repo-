import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        wait = new WebDriverWait(driver, Duration.ofSeconds(2));

        try {
            login(user, pass);

            while (true) {
                // Direct jump to destination eliminates 3 menu click round-trips
                driver.get(BASE_URL + "/guild/graids/tweens/");
                clickJoinRaid();

                System.out.println("Joined raid");

                // ---------------- ATTACK LOOP ----------------
                boolean raidActive = true;

                while (raidActive) {
                    boolean attacked = false;

                    // 1. Priority: x1.5 multiplier
                    attacked = clickAttackByMultiplier("x 1.5");

                    // 2. Switch target pool if x1.5 not found
                    if (!attacked) {
                        if (clickIfExists("a[href*='/chtarget/']")) {
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

                    // Handle scenario where attack patterns are exhausted
                    if (!attacked) {
                        System.out.println("No targeted action sequences executed successfully");

                        if (clickIfExists("a[href*='/start_cave/']")) {
                            System.out.println("Start digging clicked");
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

        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("plogin"))).sendKeys(user);
        driver.findElement(By.name("ppass")).sendKeys(pass);
        
        WebElement submit = driver.findElement(By.cssSelector("input[type='submit']"));
        jsClick(submit);

        // Fast optional check for popup without blocking flow
        try {
            WebElement urfin = new WebDriverWait(driver, Duration.ofMillis(800))
                    .until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.urfin")));
            jsClick(urfin);
        } catch (Exception ignored) {}
    }

    private static void clickJoinRaid() {
        try {
            WebElement el = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href^='/guild/graids/tweens/join/']"))
            );
            jsClick(el);
        } catch (Exception e) {
            System.out.println("Join raid not found");
        }
    }

    // ---------------- ATTACK / START CHECK ----------------
    private static boolean clickIfExists(String css) {
        try {
            List<WebElement> list = driver.findElements(By.cssSelector(css));
            if (list.isEmpty()) return false;
            jsClick(list.get(0));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ---------------- MULTIPLIER MATCHING (SINGLE XPATH CALL) ----------------
    private static boolean clickAttackByMultiplier(String targetMultiplier) {
        try {
            // Evaluates text condition natively inside browser DOM in 1 network call
            String xpath = String.format("//div[contains(@class,'fb_path') and contains(., '%s')]//a[contains(@href, '/attack')]", targetMultiplier);
            List<WebElement> links = driver.findElements(By.xpath(xpath));

            if (!links.isEmpty()) {
                jsClick(links.get(0));
                System.out.println("Successfully attacked targeting: " + targetMultiplier);
                return true;
            }
        } catch (Exception e) {
            System.out.println("Error processing multiplier matching: " + e.getMessage());
        }
        return false;
    }

    // ---------------- HELPER FOR INSTANT JS CLICKS ----------------
    private static void jsClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    // ---------------- DRIVER SETUP ----------------
    private static WebDriver setup() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        
        // Don't wait for images, ads, or external scripts to complete full load
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);

        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--blink-settings=imagesEnabled=false");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-notifications");

        // Block images and stylesheets at browser level to minimize network payload
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.managed_default_content_settings.images", 2);
        prefs.put("profile.managed_default_content_settings.stylesheets", 2);
        options.setExperimentalOption("prefs", prefs);

        return new ChromeDriver(options);
    }
}
