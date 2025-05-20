package org.example

import com.microsoft.playwright.*
import com.microsoft.playwright.options.Cookie
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class FetchPostApiWithPromise {
    static void main(String[] args) {
        // Initialize Playwright
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            Page page = browser.newPage();
            String url = "https://httpbin.org/post";
            Map<String, Object> payload = [name: "Groovy", type: "Playwright"];
            String jsonBody = JsonOutput.toJson(payload);
            Map<String, String> headers = ["Content-Type": "application/json"];

            // Execute JS directly in the browser context with async/await and sleep
            Map<String, Object> evalParams = [url: url, jsonBody: jsonBody, headers: headers]
            Object result = page.evaluate("""
                async ({url, jsonBody, headers}) => {
                    function sleep(ms) { return new Promise(resolve => setTimeout(resolve, ms)); }
                    await sleep(1000);
                    const response = await fetch(url, {
                        method: 'POST',
                        headers,
                        body: jsonBody
                    });
                    const data = await response.json();
                    await sleep(1000);
                    return data;
                }
            """, evalParams);
            println "Response JSON: ${JsonOutput.prettyPrint(JsonOutput.toJson(result))}";
            // Print cookies after the POST request
            List<Cookie> cookies = page.context().cookies();
            println "Cookies:";
            cookies.each { cookie ->
                println "${cookie.name()} = ${cookie.value()} (domain: ${cookie.domain()}, path: ${cookie.path()})";
            }
            browser.close();
        }
    }
}