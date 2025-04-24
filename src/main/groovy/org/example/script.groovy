package org.example

import org.htmlunit.WebClient
import org.htmlunit.BrowserVersion
import org.htmlunit.html.HtmlPage
import org.htmlunit.ScriptResult
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class HtmlUnitHttpClient {
    static WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED)

    static String doXmlHttpRequest(String method, String url, Map<String, String> headers = [:], def body = null) {
        webClient.options.setThrowExceptionOnScriptError(false)
        webClient.options.setCssEnabled(false)
        String js = '''
            var done = false;
            var result = null;
            var xhr = new XMLHttpRequest();
            xhr.open("''' + method + '''", "''' + url + '''", false);
        '''
        if (headers) {
            headers.each { k, v ->
                js += 'xhr.setRequestHeader("' + k + '", "' + v + '");\n'
            }
        }
        js += '''
            xhr.onreadystatechange = function() {
                if (xhr.readyState == 4) {
                    done = true;
                    result = xhr.responseText;
                }
            };
        '''
        if (body != null) {
            js += 'xhr.send(JSON.stringify(' + JsonOutput.toJson(body) + '));\n'
        } else {
            js += 'xhr.send();\n'
        }
        js += '''
            result;
        '''
        // Use a blank page as the context for JS execution
        def page = webClient.getPage("about:blank")
        def responseContent
        if (page instanceof org.htmlunit.html.HtmlPage) {
            ScriptResult scriptResult = page.executeJavaScript(js)
            webClient.waitForBackgroundJavaScript(10000)
            responseContent = scriptResult.getJavaScriptResult()?.toString()
        } else {
            responseContent = null
        }
        return responseContent
    }

    static String doGet(String url, Map<String, String> headers = [:]) {
        return doXmlHttpRequest("GET", url, headers)
    }

    static String doPost(String url, Map<String, String> headers = [:], def body = null) {
        return doXmlHttpRequest("POST", url, headers, body)
    }



    static void main(String[] args) {
        String getUrl = "https://httpbin.org/get"
        String postUrl = "https://httpbin.org/post"
        Map<String, String> headers = ["Content-Type": "application/json"]
        def postBody = [foo: "bar", test: 123]

        println "GET Test:"
        String getResp = HtmlUnitHttpClient.doGet(getUrl, headers)
        println getResp
        println "\nPOST Test:"
        String postResp = HtmlUnitHttpClient.doPost(postUrl, headers, postBody)
        println postResp
    }
}