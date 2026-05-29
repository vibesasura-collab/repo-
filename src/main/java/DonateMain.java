import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

public class DonateMain {

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

            donateGold();

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            if (driver != null) {
                driver.quit();
            }

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

    // ---------------- DONATE ----------------

    private static void donateGold() {

        try {

            // Open Guild
            driver.get("https://elem.cards/guild/");

            sleep(3000);

            // Open Treasury
            driver.get("https://elem.cards/guild/treasury/");

            sleep(3000);

            // Enter 10 gold
            WebElement goldInput = driver.findElement(
                    By.name("donate_gold")
            );

            goldInput.clear();

            goldInput.sendKeys("10");

            sleep(1000);

            // Click contribute
            WebElement contributeBtn = driver.findElement(
                    By.xpath("//input[@type='submit' and @value='Contribute']")
            );

            contributeBtn.click();

            sleep(3000);

            System.out.println("10 gold donated ✔");

        } catch (Exception e) {

            System.out.println("Donation failed.");

            e.printStackTrace();
        }
    }

    // ---------------- HELPERS ----------------

    private static void sleep(int ms) {

        try {

            Thread.sleep(ms);

        } catch (Exception ignored) {}
    }
}
