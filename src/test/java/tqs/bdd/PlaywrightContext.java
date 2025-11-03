package tqs.bdd;

import com.microsoft.playwright.Page;

public class PlaywrightContext {
    private static final ThreadLocal<Page> PAGE = new ThreadLocal<>();

    public static void setPage(Page page) {
        PAGE.set(page);
    }

    public static Page getPage() {
        return PAGE.get();
    }

    public static void clear() {
        PAGE.remove();
    }
}
