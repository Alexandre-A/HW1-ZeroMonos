package tqs.bdd;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import com.microsoft.playwright.*;

public class PlaywrightHooks {
    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    @Before
    public void setup() {
        if (playwright == null) {
            playwright = Playwright.create();
        }
        if (browser == null) {
            browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setSlowMo(50) // Slow down for stability
            );
        }
        context = browser.newContext(new Browser.NewContextOptions()
            .setViewportSize(1280, 720));
        page = context.newPage();
        
        // Store page in shared context
        PlaywrightContext.setPage(page);
    }

    @After
    public void teardown() {
        if (page != null) {
            page.close();
        }
        if (context != null) {
            context.close();
        }
    }

    public static void closeAll() {
        if (browser != null) {
            browser.close();
            browser = null;
        }
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
    }
}
