package org.example

import com.microsoft.playwright.*
import groovy.json.JsonSlurper

class FetchPokemonApi {
    static void main(String[] args) {
        // Initialize Playwright
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch()
            Page page = browser.newPage()
            // Use APIRequestContext for direct API calls
            APIRequestContext request = playwright.request().newContext()
            APIResponse response = request.get("https://pokeapi.co/api/v2/pokemon/pikachu")
            if (response.ok()) {
                String json = response.text()
                def parsed = new JsonSlurper().parseText(json)
                println "Name: ${parsed.name}"
                println "Height: ${parsed.height}"
                println "Weight: ${parsed.weight}"
                println "Base experience: ${parsed.base_experience}"
            } else {
                println "Failed to fetch Pokémon data. Status: ${response.status()}"
            }
            browser.close()
        }
    }
}