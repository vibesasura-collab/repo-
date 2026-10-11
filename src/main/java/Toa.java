import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.List;

public class Toa {

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
            joinTournament();
        } catch (Exception ignored) {
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
    }

    private static void joinTournament() {
        // Click Tournament Link
        driver.get("https://elem.cards/tournament/");
        sleep(2000);

        List<WebElement> tournamentLinks = driver.findElements(By.cssSelector("a.wfb.bttn.tournament"));
        if (!tournamentLinks.isEmpty()) {
            click(tournamentLinks.get(0));
            sleep(2000);
        } else {
            driver.get("https://elem.cards/tournament/");
            sleep(2000);
        }

        // Click Tournament Join / Sign up Link
        List<WebElement> joinLinks = driver.findElements(By.xpath("//a[contains(@href, '/tournament/join/')]"));
        if (!joinLinks.isEmpty()) {
            click(joinLinks.get(0));
            sleep(1000);
            System.out.println("yes joined");
        }
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
