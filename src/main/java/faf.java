import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class faf {

    private static final int MAX_RUN_MINUTES = 345;
    private static final int WAIT_AFTER_FULL_CYCLE_MINUTES = 55;

    public static void main(String[] args) {
        String user = System.getenv("USER_KEY");
        String pass = System.getenv("ACCESS_KEY");

        if (user == null || pass == null || user.isEmpty() || pass.isEmpty()) {
            return;
        }

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        WebDriver driver = new ChromeDriver(options);

        try {
            login(driver, user, pass);

            collectDailyRewardIfAvailable(driver);
            collectFreeGemsIfAvailable(driver);

            runOneFullCycle(driver);

            // 🔥 STEP 1: ALWAYS GO TO AUTOTUNE PAGE
            driver.get("https://elem.cards/funnyfights/?autotune=on");
            sleep(3000);

            // 🔥 STEP 2: FIND NEXT PACK BUTTON (FIXED XPATH)
            List<WebElement> changePackBtn = driver.findElements(
                By.xpath("//a[contains(@href,'/funnyfights/nextpack/')]")
            );

            System.out.println("Pack buttons found: " + changePackBtn.size());

            // 🔥 STEP 3: CLICK BUTTON SAFELY
            if (!changePackBtn.isEmpty()) {
                WebElement btn = changePackBtn.get(0);

                try {
                    btn.click();
                } catch (Exception e) {
                    ((org.openqa.selenium.JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", btn);
                }

                sleep(4000);
            }

            // 🔥 STEP 4: SECOND CYCLE AFTER PACK SWITCH
            runOneFullCycle(driver);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    // ✅ LOGIN FIXED (SAFE WAIT)
    private static void login(WebDriver driver, String user, String pass) {

        driver.get("https://elem.cards");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.name("plogin")));
        } catch (Exception e) {
            System.out.println("Login page not loaded properly!");
            System.out.println("Current URL: " + driver.getCurrentUrl());
            return;
        }

        driver.findElement(By.name("plogin")).sendKeys(user);
        driver.findElement(By.name("ppass")).sendKeys(pass);
        driver.findElement(By.cssSelector("input[type='submit']")).click();

        sleep(5000);
    }

    // 🔥 CORE LOOP (5 ATTACKS)
    private static void runOneFullCycle(WebDriver driver) {

        driver.get("https://elem.cards/funnyfights/?autotune=on");
        sleep(3000);

        for (int i = 1; i <= 5; i++) {

            driver.get("https://elem.cards/funnyfights/enemy/" + i + "/");
            sleep(2500);

            List<WebElement> attackBtns = driver.findElements(
                By.xpath("//a[contains(@href,'/funnyfights/attack/') and .//span[text()='Attack!']]")
            );

            if (attackBtns.isEmpty()) continue;

            String attackLink = attackBtns.get(0).getAttribute("href");

            if (attackLink == null || attackLink.isEmpty()) continue;

            driver.get(attackLink);
            sleep(5000);

            driver.get("https://elem.cards/funnyfights/manage/");
            sleep(2500);

            clickUpgradeIfAvailable(driver);

            driver.get("https://elem.cards/funnyfights/?autotune=on");
            sleep(2500);
        }
    }

    private static void clickUpgradeIfAvailable(WebDriver driver) {
        try {
            List<WebElement> upgradeBtns = driver.findElements(
                By.xpath("//a[contains(@href,'/funnyfights/manage/upgrade/0/')]")
            );

            if (!upgradeBtns.isEmpty()) {
                driver.get(upgradeBtns.get(0).getAttribute("href"));
                sleep(2500);
            }
        } catch (Exception ignored) {}
    }

    private static void collectFreeGemsIfAvailable(WebDriver driver) {
        try {
            driver.get("https://elem.cards/funnyfights/");
            sleep(2500);

            List<WebElement> btns = driver.findElements(
                By.xpath("//a[contains(@href,'/funnyfights/freegems/') and .//span[text()='Take']]")
            );

            if (!btns.isEmpty()) {
                driver.get(btns.get(0).getAttribute("href"));
                sleep(2500);
            }
        } catch (Exception ignored) {}
    }

    private static void collectDailyRewardIfAvailable(WebDriver driver) {
        try {
            driver.get("https://elem.cards/");
            sleep(2500);

            List<WebElement> btns = driver.findElements(
                By.xpath("//a[contains(@href,'/dailyreward/tnx/') and .//span[text()='Receive']]")
            );

            if (!btns.isEmpty()) {
                driver.get(btns.get(0).getAttribute("href"));
                sleep(2500);
            }
        } catch (Exception ignored) {}
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static boolean shouldStopNow(Instant startTime) {
        return Duration.between(startTime, Instant.now()).toMinutes() >= MAX_RUN_MINUTES;
    }
}
