import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

public class Main {

    private static final int MAX_RUN_MINUTES = 345;
    private static final LocalTime DAILY_STOP_START = LocalTime.of(23, 30);
    private static final LocalTime DAILY_STOP_END = LocalTime.of(1, 0);

    public static void main(String[] args) {

        String user = System.getenv("GAME_ID");
        String pass = System.getenv("GAME_PASSWORD");

        if (user == null || pass == null) {
            throw new RuntimeException("Secrets not set");
        }

        if (isInShutdownWindow()) {
            System.out.println("Shutdown window. Exiting.");
            return;
        }

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");

        WebDriver driver = new ChromeDriver(options);
        Instant startTime = Instant.now();

        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

            // LOGIN
            driver.get("https://elem.cards/login/");
            sleep(2000);

            driver.findElement(By.name("plogin")).sendKeys(user);
            driver.findElement(By.name("ppass")).sendKeys(pass);
            driver.findElement(By.cssSelector("input[type='submit']")).click();

            sleep(4000);

            driver.findElement(By.cssSelector("a.urfin")).click();
            sleep(3000);

            // DUEL LOOP
            while (true) {

                if (shouldStopNow(startTime)) break;

                driver.get("https://elem.cards/duel/");
                sleep(2000);

                List<WebElement> attackBtn = driver.findElements(
                        By.xpath("//a[contains(@href,'/duel/tobattle/')]"));

                if (attackBtn.isEmpty()) {
                    System.out.println("No duels left.");
                    break;
                }

                attackBtn.get(0).click();
                sleep(2000);

                // FIGHT LOOP
                int rounds = 0;

                while (rounds < 50) {

                    if (isEnemyDead(driver)) break;

                    clickIfPresent(driver, "a[href*='attack0']");
                    sleep(1000);

                    clickIfPresent(driver, "a[href*='attack1']");
                    sleep(1000);

                    clickIfPresent(driver, "a[href*='attack2']");
                    sleep(1000);

                    rounds++;
                }

                // NEXT DUEL
                List<WebElement> another = driver.findElements(
                        By.xpath("//span[text()='Another duel']/ancestor::a"));

                if (!another.isEmpty()) {
                    another.get(0).click();
                    sleep(2000);
                } else {
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    public static boolean isEnemyDead(WebDriver driver) {
        return !driver.findElements(
                By.xpath("//span[text()='Another duel']")).isEmpty();
    }

    public static void clickIfPresent(WebDriver driver, String css) {
        List<WebElement> elements = driver.findElements(By.cssSelector(css));
        if (!elements.isEmpty()) {
            try { elements.get(0).click(); } catch (Exception ignored) {}
        }
    }

    public static boolean shouldStopNow(Instant startTime) {
        long minutes = Duration.between(startTime, Instant.now()).toMinutes();
        return minutes >= MAX_RUN_MINUTES || isInShutdownWindow();
    }

    public static boolean isInShutdownWindow() {
        LocalTime now = LocalTime.now(ZoneOffset.UTC);
        return !now.isBefore(DAILY_STOP_START) || now.isBefore(DAILY_STOP_END);
    }

    public static void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
