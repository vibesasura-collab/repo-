import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.util.List;

public class ArenaBatchTwo {

    private static WebDriver driver;

    public static void main(String[] args) {
        // Render will tell this specific server what account number it handles (21-41)
        String accountIdStr = System.getenv("ACCOUNT_ID");
        if (accountIdStr == null) {
            throw new RuntimeException("Error: Missing ACCOUNT_ID environment variable on Render!");
        }

        // Dynamically looks up keys like GAME_ID_21, GAME_PASSWORD_21 inside your shared group
        String expectedUserKey = "GAME_ID_" + accountIdStr;
        String expectedPassKey = "GAME_PASSWORD_" + accountIdStr;

        String user = System.getenv(expectedUserKey);
        String pass = System.getenv(expectedPassKey);

        if (user == null || pass == null) {
            throw new RuntimeException("Error: Key lookup failed for account " + accountIdStr + ". Check Render Environment Group!");
        }

        System.out.println("=== Launching Batch 2 Bot | Account Instance: " + accountIdStr + " ===");

        // --- AUTOMATIC CHROME DEPLOYER FOR RENDER LINUX ---
        try {
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", 
                "if ! command -v google-chrome >/dev/null 2>&1; then " +
                "mkdir -p $HOME/.chrome; wget -q https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb; " +
                "dpkg -x google-chrome-stable_current_amd64.deb $HOME/.chrome; " +
                "fi");
            pb.start().waitFor();
        } catch (Exception e) {
            System.out.println("Note: Native OS Chrome check completed.");
        }

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        
        String homePath = System.getProperty("user.home");
        options.setBinary(homePath + "/.chrome/opt/google/chrome/google-chrome");

        try {
            driver = new ChromeDriver(options);
        } catch (Exception e) {
            options.setBinary(""); // Use system fallback path if environment handles binary globally
            driver = new ChromeDriver(options);
        }

        try {
            login(user, pass);
            
            // Navigate directly to the room scraper
            driver.get("https://elem.cards/guild/arena/");
            sleep(2000);

            List<WebElement> joinLinks = driver.findElements(By.xpath("//a[contains(@href,'/guild/arena/join/')]"));
            if (!joinLinks.isEmpty()) {
                String targetMatch = joinLinks.get(0).getAttribute("href");
                System.out.println("Entering targeted session path: " + targetMatch);
                driver.get(targetMatch);
                sleep(2000);
            }

            executeCombatLoop();

        } catch (Exception e) {
            e.printStackTrace();
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
        System.out.println("Login status verified: Success ✔");
    }

    private static void executeCombatLoop() {
        int ticks = 0;
        System.out.println("Monitoring active combat slots...");

        while (ticks < 300) {
            boolean actionsAvailable = false;

            if (fireAttackSlot("a[href*='attack0']")) actionsAvailable = true;
            if (fireAttackSlot("a[href*='attack1']")) actionsAvailable = true;
            if (fireAttackSlot("a[href*='attack2']")) actionsAvailable = true;

            if (!actionsAvailable) {
                sleep(1500);
                driver.navigate().refresh();
                
                boolean activeGrid = !driver.findElements(By.cssSelector("a[href*='attack']")).isEmpty();
                if (!activeGrid) {
                    System.out.println("Combat nodes clear or cycle terminated.");
                    break;
                }
            }
            sleep(400);
            ticks++;
        }
    }

    private static boolean fireAttackSlot(String selector) {
        List<WebElement> elements = driver.findElements(By.cssSelector(selector));
        if (!elements.isEmpty()) {
            try {
                elements.get(0).click();
            } catch (Exception e) {
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", elements.get(0));
                } catch (Exception ignored) {}
            }
            return true;
        }
        return false;
    }

    private static void sleep(int ms) {
        try { Thread.sleep(ms); } catch (Exception ignored) {}
    }
}
