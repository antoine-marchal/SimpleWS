package org.example

import org.openqa.selenium.WebDriver
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.By

class FetchPokemonApiSelenium {
    static void main(String[] args) {
        // Set the path to the Edge WebDriver
        System.setProperty("webdriver.edge.driver", "drivers/msedgedriver.exe")

        // Optional: configure Edge options
        EdgeOptions options = new EdgeOptions()
        //options.addArguments("--headless=new") // Run headless (no UI)
        options.addArguments("--headless=new")
        options.addArguments("--disable-gpu")
        options.addArguments("--window-size=1920,1080")
        WebDriver driver = new EdgeDriver(options)
        try {
            // Navigate to a blank page to execute JS fetch
            driver.get("about:blank")

            // JavaScript code to fetch Pokémon API and return JSON as string
            String script = """
                return fetch('https://pokeapi.co/api/v2/pokemon/pikachu')
                  .then(response => response.json())
                  .then(data => JSON.stringify(data));
            """
            // Execute the JS asynchronously and get the JSON string
            String json = driver.executeAsyncScript("""
                var callback = arguments[arguments.length - 1];
                fetch('https://pokeapi.co/api/v2/pokemon/pikachu')
                .then(response => response.json())
                .then(data => callback(JSON.stringify(data)))
                .catch(err => callback(JSON.stringify({error: err.message})));
                """
            )

            //println "Raw JSON: $json"

            // Optionally, parse the JSON (requires groovy.json.JsonSlurper)
            def parsed = new groovy.json.JsonSlurper().parseText(json)
            println "Name: ${parsed.name}"
            println "Height: ${parsed.height}"
            println "Weight: ${parsed.weight}"
            println "Base experience: ${parsed.base_experience}"
        } finally {
            driver.quit()
        }
    }
}