import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.List;

public class aa {

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

            playDungeon();

        } finally {
            if (driver != null) driver.quit();
            System.exit(0);
        }
    }

    // ---------------- LOGIN ----------------

    private static void login(String user, String pass) {

        driver.get("https://elem.cards/login/");
        sleep(3000);

        driver.findElement(By.name("plogin")).sendKeys(user);
        driver.findElement(By.name("ppass")).sendKeys(pass);
        driver.findElement(By.cssSelector("input[type='submit']")).click();

        sleep(5000);

        System.out.println("Login successful ✔");
    }

    // ---------------- DUNGEON ----------------

    private static void playDungeon() {

        for (int stage = 1; stage <= 3; stage++) {

            System.out.println("==================================");
            System.out.println("Dungeon Stage: " + stage);

            driver.get("https://elem.cards/dungeon/" + stage + "/start/");

            sleep(5000); // important for JS load

            int rounds = 0;

            while (rounds < 80) {

                boolean clicked = false;

                clicked |= clickIfPresent("a[href*='attack0']");
                clicked |= clickIfPresent("a[href*='attack1']");
                clicked |= clickIfPresent("a[href*='attack2']");
                clicked |= clickIfPresent("a[href*='/dungeon/attack']");

                if (!clicked) {
                    sleep(1000);
                } else {
                    sleep(600);
                }

                rounds++;
            }

            System.out.println("Stage " + stage + " cleared ✔");

            driver.get("https://elem.cards/dungeon/");
            sleep(2000);
        }

        System.out.println("Dungeon completed ✔");
    }

    // ---------------- CLICK ----------------

    private static boolean clickIfPresent(String css) {

        List<WebElement> el = driver.findElements(By.cssSelector(css));

        if (!el.isEmpty()) {

            try {
                el.get(0).click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", el.get(0));
            }

            return true;
        }

        return false;
    }

    // ---------------- SLEEP ----------------

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ignored) {}
    }
}
