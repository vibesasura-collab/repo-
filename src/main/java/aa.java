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

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);

        try {

            login(user, pass);
            playDungeon();

        } finally {
            driver.quit();
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
            sleep(5000);

            int noActionCount = 0;

            while (true) {

                List<WebElement> attacks = driver.findElements(
                        By.cssSelector("a[href*='/dungeon/attack'], a[href*='attack0'], a[href*='attack1'], a[href*='attack2']")
                );

                if (attacks.isEmpty()) {

                    noActionCount++;
                    sleep(1200);

                    // if nothing appears for a while → stage finished
                    if (noActionCount >= 5) {
                        break;
                    }

                    continue;
                }

                noActionCount = 0;

                try {
                    attacks.get(0).click();
                } catch (Exception e) {
                    ((JavascriptExecutor) driver)
                            .executeScript("arguments[0].click();", attacks.get(0));
                }

                sleep(1200);
            }

            System.out.println("Stage " + stage + " cleared ✔");

            driver.get("https://elem.cards/dungeon/");
            sleep(2000);
        }

        System.out.println("Dungeon completed ✔");
    }

    // ---------------- SLEEP ----------------

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ignored) {}
    }
}
