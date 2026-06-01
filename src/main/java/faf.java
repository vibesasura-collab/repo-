import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class faf {

    private static final int MAX_RUN_MINUTES = 345;
    private static final int WAIT_AFTER_FULL_CYCLE_MINUTES = 55;

    private static final String LOGIN_URL = "https://elem.cards/login/";
    private static final String AUTOTUNE_URL = "https://elem.cards/funnyfights/?autotune=on";
    private static final String ENEMY_URL = "https://elem.cards/funnyfights/enemy/";
    private static final String MANAGE_URL = "https://elem.cards/funnyfights/manage/";
    private static final String FUNNYFIGHTS_HOME_URL = "https://elem.cards/funnyfights/";
    private static final String HOME_URL = "https://elem.cards/";

    public static void main(String[] args) {
        String user = System.getenv("USER_KEY");
        String pass = System.getenv("ACCESS_KEY");

        if (user == null || pass == null || user.isEmpty() || pass.isEmpty()) {
            System.out.println("Error: USER_KEY or ACCESS_KEY environment variables are missing!");
            return;
        }

        System.out.println("Starting the Nebula Task execution...");
        
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
            
            System.out.println("Task finished successfully!");

        } catch (Exception e) {
            System.out.println("An error occurred during execution.");
        } finally {
            System.out.println("Closing browser.");
            driver.quit();
        }
    }

    private static void login(WebDriver driver, String user, String pass) {
        driver.get(LOGIN_URL);
        sleep(2000);

        driver.findElement(By.name("plogin")).sendKeys(user);
        driver.findElement(By.name("ppass")).sendKeys(pass);
        driver.findElement(By.cssSelector("input[type='submit']")).click();

        sleep(5000);
    }

    private static void runOneFullCycle(WebDriver driver) {
        driver.get(AUTOTUNE_URL);
        sleep(3000);

        for (int i = 1; i <= 5; i++) {
            driver.get(ENEMY_URL + i + "/");
            sleep(2500);

            List<WebElement> attackBtns = driver.findElements(
                By.xpath("//a[contains(@href,'/funnyfights/attack/') and .//span[text()='Attack!']]")
            );

            if (attackBtns.isEmpty()) {
                continue;
            }

            String attackLink = attackBtns.get(0).getAttribute("href");

            if (attackLink == null || attackLink.isEmpty()) {
                continue;
            }

            driver.get(attackLink);
            sleep(5000);

            driver.get(MANAGE_URL);
            sleep(2500);

            clickUpgradeIfAvailable(driver);

            driver.get(AUTOTUNE_URL);
            sleep(2500);
        }
    }

    private static void clickUpgradeIfAvailable(WebDriver driver) {
        try {
            List<WebElement> upgradeBtns = driver.findElements(
                By.xpath("//a[contains(@href,'/funnyfights/manage/upgrade/0/')]")
            );

            if (!upgradeBtns.isEmpty()) {
                String upgradeLink = upgradeBtns.get(0).getAttribute("href");

                if (upgradeLink != null && !upgradeLink.isEmpty()) {
                    driver.get(upgradeLink);
                    sleep(2500);
                }
            }
        } catch (Exception e) {
            // Suppressed
        }
    }

    private static void collectFreeGemsIfAvailable(WebDriver driver) {
        try {
            driver.get(FUNNYFIGHTS_HOME_URL);
            sleep(2500);

            List<WebElement> freeGemBtns = driver.findElements(
                By.xpath("//a[contains(@href,'/funnyfights/freegems/') and .//span[text()='Take']]")
            );

            if (!freeGemBtns.isEmpty()) {
                String freeGemLink = freeGemBtns.get(0).getAttribute("href");

                if (freeGemLink != null && !freeGemLink.isEmpty()) {
                    driver.get(freeGemLink);
                    sleep(2500);
                }
            }
        } catch (Exception e) {
            // Suppressed
        }
    }

    private static void collectDailyRewardIfAvailable(WebDriver driver) {
        try {
            driver.get(HOME_URL);
            sleep(2500);

            List<WebElement> dailyRewardBtns = driver.findElements(
                By.xpath("//a[contains(@href,'/dailyreward/tnx/') and .//span[text()='Receive']]")
            );

            if (!dailyRewardBtns.isEmpty()) {
                String dailyRewardLink = dailyRewardBtns.get(0).getAttribute("href");

                if (dailyRewardLink != null && !dailyRewardLink.isEmpty()) {
                    driver.get(dailyRewardLink);
                    sleep(2500);
                }
            }
        } catch (Exception e) {
            // Suppressed
        }
    }

    private static boolean shouldStopNow(Instant startTime) {
        long elapsedMinutes = Duration.between(startTime, Instant.now()).toMinutes();
        return elapsedMinutes >= MAX_RUN_MINUTES;
    }

    private static void sleepMinutes(int minutes) {
        sleep(minutes * 60L * 1000L);
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
