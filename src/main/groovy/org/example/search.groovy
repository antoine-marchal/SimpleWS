package org.example

import org.apache.http.auth.AuthScope
import org.apache.http.auth.NTCredentials
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpGet
import org.apache.http.entity.StringEntity
import org.apache.http.util.EntityUtils
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.client.CookieStore
import org.apache.http.HttpResponse
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

class Search3DSpace {
    static def baseurl = "https://3dx.fr/"
    static def user = "user"
    static def pass = "pass"
    static def securityContext = "ctx::xxx"
    static CookieStore cookieStore = new BasicCookieStore()

    static CloseableHttpClient createNtlmClient(String user, String pass, String workstation = null, String domain = null) {
        CredentialsProvider credsProvider = new BasicCredentialsProvider()
        credsProvider.setCredentials(
            AuthScope.ANY,
            new NTCredentials(user, pass, workstation, domain)
        )
        return HttpClients.custom()
            .setDefaultCredentialsProvider(credsProvider)
            .setDefaultCookieStore(cookieStore)
            .setRedirectStrategy(new org.apache.http.impl.client.LaxRedirectStrategy())
            .build()
    }

    static String doPost(String path, Map<String, String> headers, def body) {
        String url = baseurl + path
        CloseableHttpClient httpclient = createNtlmClient(user, pass)
        try {
            HttpPost httpPost = new HttpPost(url)
            headers?.each { k, v -> httpPost.setHeader(k, v) }
            String jsonBody = (body instanceof String) ? body : JsonOutput.toJson(body)
            httpPost.setEntity(new StringEntity(jsonBody, "UTF-8"))
            HttpResponse response = httpclient.execute(httpPost)
            return EntityUtils.toString(response.getEntity(), "UTF-8")
        } finally {
            httpclient.close()
        }
    }

    static String doGet(String path, Map<String, String> headers) {
        String url = baseurl + path
        CloseableHttpClient httpclient = createNtlmClient(user, pass)
        try {
            HttpGet httpGet = new HttpGet(url)
            headers?.each { k, v -> httpGet.setHeader(k, v) }
            HttpResponse response = httpclient.execute(httpGet)
            return EntityUtils.toString(response.getEntity(), "UTF-8")
        } finally {
            httpclient.close()
        }
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
