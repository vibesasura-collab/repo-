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

                    attacked |= clickIfExists("a[href*='/attack0/']");
                    attacked |= clickIfExists("a[href*='/attack1/']");
                    attacked |= clickIfExists("a[href*='/attack2/']");

                    sleep(2000);

                    if (!attacked) {

                        System.out.println("No attacks found");

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
