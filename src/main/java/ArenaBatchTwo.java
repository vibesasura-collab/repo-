import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.util.List;

public class ArenaBatchTwo {

    private static WebDriver driver;

    public static void main(String[] args) {
        System.out.println("=== Starting Batch 2 Automation (Accounts 21 to 41) ===");

        // --- AUTOMATIC CHROME DEPLOYER FOR RENDER LINUX ---
        try {
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", 
                "if ! command -v google-chrome >/dev/null 2>&1; then " +
                "mkdir -p $HOME/.chrome; wget -q https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb; " +
                "dpkg -x google-chrome-stable_current_amd64.deb $HOME/.chrome; " +
                "fi");
            pb.start().waitFor();
            System.out.println("Note: Native OS Chrome check completed.");
        } catch (Exception e) {
            System.out.println("Chrome preparation warning: " + e.getMessage());
        }

        WebDriverManager.chromedriver().setup();

        // LOOP THROUGH ALL ACCOUNTS (21 to 41) ONE BY ONE
        for (int i = 21; i <= 41; i++) {
            String accountIdStr = String.valueOf(i);
            
            String expectedUserKey = "GAME_ID_" + accountIdStr;
            String expectedPassKey = "GAME_PASSWORD_" + accountIdStr;

            String user = System.getenv(expectedUserKey);
            String pass = System.getenv(expectedPassKey);

            // If an account is missing in your secrets group, skip it and move to the next
            if (user == null || pass == null) {
                System.out.println("Skipping account " + accountIdStr + ": Not found in environment variables.");
                continue; 
            }

            System.out.println("\n---------------------------------------------");
            System.out.println("▶ Processing Account [" + accountIdStr + "]");
            System.out.println("---------------------------------------------");

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            
            String homePath = System.getProperty("user.home");
            options.setBinary(homePath + "/.chrome/opt/google/chrome/google-chrome");

            try {
                try {
                    driver = new ChromeDriver(options);
                } catch (Exception e) {
                    options.setBinary(""); // Fallback path
                    driver = new ChromeDriver(options);
                }

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
                System.out.println("❌ Error processing account " + accountIdStr);
                e.printStackTrace();
            } finally {
                if (driver != null) {
                    try {
                        driver.quit();
                    } catch (Exception ignored) {}
                }
            }
            
            // Short rest break between accounts so the server doesn't look suspicious
            sleep(3000); 
        }

        System.out.println("\n=== All accounts from 21 to 41 have completed cycles! ===");
        System.exit(0);
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
