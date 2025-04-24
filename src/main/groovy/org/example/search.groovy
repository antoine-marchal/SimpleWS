package org.example

import org.htmlunit.WebClient
import org.htmlunit.BrowserVersion
import org.htmlunit.html.HtmlPage
import org.htmlunit.ScriptResult
import org.htmlunit.util.Cookie
import groovy.json.JsonSlurper
import groovy.json.JsonOutput


class Search3DSpace {
    static def baseurl = "https://3dx.fr/"
    static def user = "user"
    static def pass = "pass"
    static def securityContext = "ctx::xxx"
    static WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED)

    static String doXmlHttpRequest(String method, String url, Map<String, String> headers, def body = null) {
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
            String jsonBody = (body instanceof String) ? body : JsonOutput.toJson(body)
            js += 'xhr.send(JSON.stringify(' + JsonOutput.toJson(body) + '));\n'
        } else {
            js += 'xhr.send();\n'
        }
        js += '''
            result;
        '''
        HtmlPage page = webClient.getPage(baseurl)
        ScriptResult scriptResult = page.executeJavaScript(js)
        return scriptResult.getJavaScriptResult()?.toString()
    }

    static String doPost(String path, Map<String, String> headers, def body) {
        String url = baseurl + path
        return doXmlHttpRequest("POST", url, headers, body)
    }

    static String doGet(String path, Map<String, String> headers) {
        String url = baseurl + path
        return doXmlHttpRequest("GET", url, headers)
    }

    static List fetchResourceIds(String queryString) {
        def headers = [
            "Content-Type"   : "application/json; charset=UTF-8",
            "SecurityContext": securityContext
        ]

        def body = [
            label: 'service',
            query: queryString,
            nresults: 1000,
            refine: [
                "ds6w:what/ds6w:type": [
                    [disptext: "Physical Product", object: "VPMReference", type: "string", field: ["implicit"]],
                    [disptext: "Electrical Geometry", object: "ElectricalGeometry", type: "string", field: ["implicit"]],
                    [disptext: "Electrical Branch Geometry", object: "ElectricalBranchGeometry", type: "string", field: ["implicit"]]
                ]
            ],
            select_snippets: [],
            tenant: 'OnPremise',
            source: ['swym', '3dspace'],
            with_synthesis: false,
            with_nls: false,
            with_indexing_date: false
        ]

        def responseText = doPost("federated/search?", headers, body)
        def json = new JsonSlurper().parseText(responseText)

        if (json.results && json.results.size() > 0) {
            return json.results.collect { result ->
                def attrs = result.attributes
                def idAttr = attrs.find { it.name == 'resourceid' }
                def nameAttr = attrs.find { it.name == 'ds6w:identifier' }
                def descAttr = attrs.find { it.name == 'ds6w:description' }
                [idAttr?.value, nameAttr?.value, descAttr?.value]
            }
        } else {
            println "No results found or invalid response structure."
            return null
        }
    }

    static void main(String[] args) {
        // Simulate login (GET request to /3dspace/login)
        def loginResp = doGet("3dspace/login", [:])
        println "Login response: $loginResp"

        // Perform federated search
        def results = fetchResourceIds("((project:F46_COMPLETION) OR (project:COMPLETION_REUSE)) AND [ds6w:label]:\"test\" AND [ds6wg:PLMReference.V_isLastVersion]:\"TRUE\"")
        println "Fetched Resource Info: $results"
    }
}
