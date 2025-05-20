package org.example

import org.openqa.selenium.WebDriver
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.By
import org.openqa.selenium.Cookie
/**
 * Example: Using Edge WebDriver in headless mode to access an NTLM-protected page.
 * Note: Selenium does not natively support NTLM authentication prompts. For true NTLM automation,
 *       run Edge as the Windows user with access, or use a proxy like cntlm, or pre-authenticate via Windows session.
 *       This script demonstrates headless navigation and highlights the NTLM challenge.
 */
class FetchNtlmEdgeSelenium {
    static void main(String[] args) {
        // Set the path to the Edge WebDriver
        System.setProperty("webdriver.edge.driver", "drivers/msedgedriver.exe")

        // Configure Edge options for headless mode
        EdgeOptions options = new EdgeOptions()
        options.addArguments("--headless=new")
        options.addArguments("--disable-gpu")
        options.addArguments("--window-size=1920,1080")

        WebDriver driver = new EdgeDriver(options)
        try {
            String url = "https://ntlm-protected-endpoint.example.com/"
            driver.get(url)
            // If NTLM is required, the browser will show a login prompt (not handled by Selenium directly)
            // Workarounds: Use a proxy, or run as a user with access, or use a custom extension
            println "Page title: ${driver.title}"
            println "Page source (truncated): ${driver.pageSource.take(500)}"
            // Print all cookies
            def cookies = driver.manage().getCookies()
            println "Cookies:"
            cookies.each { cookie ->
                println "  Name: ${cookie.getName()}, Value: ${cookie.getValue()}, Domain: ${cookie.getDomain()}, Path: ${cookie.getPath()}, Expiry: ${cookie.getExpiry()}, Secure: ${cookie.isSecure()}"
            }
        } catch (Exception e) {
            println "Error accessing NTLM-protected page: ${e.getMessage()}"
        } finally {
            driver.quit()
        }
    }
}