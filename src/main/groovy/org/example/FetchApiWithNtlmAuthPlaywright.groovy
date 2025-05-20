package org.example

// Playwright does not natively support NTLM authentication.
// This example demonstrates launching a browser and navigating to an NTLM-protected API endpoint.
// For real NTLM support, you would need to use a proxy or helper like cntlm, or inject credentials via browser extension or external tool.
// This script shows the Playwright browser automation part in Groovy using Java interop.

import com.microsoft.playwright.*

class FetchApiWithNtlmAuthPlaywright {
    static void main(String[] args) {
        // Set up Playwright
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))
            BrowserContext context = browser.newContext()
            Page page = context.newPage()

            // NTLM-protected API endpoint
            String url = "https://ntlm-protected-endpoint.example.com/api"

            // Attempt to fetch the API (will likely fail without NTLM helper)
            page.navigate(url)
            // Wait for the response or error
            try {
                page.waitForLoadState()
                String content = page.content()
                println "Page content: $content"
            } catch (Exception e) {
                println "Failed to load NTLM-protected resource: ${e.getMessage()}"
            }

            browser.close()
        }
    }
}