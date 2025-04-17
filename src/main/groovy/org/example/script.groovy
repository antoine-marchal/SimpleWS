package org.example

import org.apache.http.auth.AuthScope
import org.apache.http.auth.NTCredentials
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.util.EntityUtils
import org.apache.http.impl.auth.NTLMSchemeFactory
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.HttpResponse
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.client.CookieStore
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class Script {
    static def baseurl = "https://petstore.swagger.io/v2/pet"
    static def user = "user"
    static def pass = "pass"
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

    static String doPost(String path, Map<String, String> headers, def body, String user = this.user, String pass = this.pass, String workstation = null, String domain = null) {
        String url = baseurl + path
        CloseableHttpClient httpclient = createNtlmClient(user, pass, workstation, domain)
        try {
            HttpPost httpPost = new HttpPost(url)
            headers?.each { k, v -> httpPost.setHeader(k, v) }
            String jsonBody
            if (body instanceof Map) {
                jsonBody = groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(body))
            } else if (body instanceof String) {
                try {
                    def parsed = groovy.json.JsonSlurper.newInstance().parseText(body)
                    jsonBody = groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(parsed))
                } catch (Exception e) {
                    jsonBody = body // fallback to raw string if not valid JSON
                }
            } else {
                jsonBody = body?.toString() ?: ""
            }
            httpPost.setEntity(new StringEntity(jsonBody, "UTF-8"))
            HttpResponse response = httpclient.execute(httpPost)
            return EntityUtils.toString(response.getEntity(), "UTF-8")
        } finally {
            httpclient.close()
        }
    }

    static String doGet(String path, Map<String, String> headers, String user = "user", String pass = "pass", String workstation = null, String domain = null) {
        String url = baseurl + path
        CloseableHttpClient httpclient = createNtlmClient(user, pass, workstation, domain)
        try {
            def httpGet = new org.apache.http.client.methods.HttpGet(url)
            headers?.each { k, v -> httpGet.setHeader(k, v) }
            HttpResponse response = httpclient.execute(httpGet)
            return EntityUtils.toString(response.getEntity(), "UTF-8")
        } finally {
            httpclient.close()
        }
    }

    static void main(String[] args) {
        def headers = ["Content-Type": "application/json"]
        def body = [key1: "value1", key2: "value2"]


        
        String postResponse = doPost("", headers, body)
        Map obj = groovy.json.JsonSlurper.newInstance().parseText(postResponse)
        println "POST response:\n$obj"
        String getResponse = doGet("", headers)
        println "GET response:\n$getResponse"

        try {
            Map getObj = groovy.json.JsonSlurper.newInstance().parseText(getResponse)
            println "Parsed GET response as JSON:\n$getObj"
        } catch(Exception e) {
            try {
                Document doc = Jsoup.parse(getResponse)
                def apiResponse = doc.selectFirst("apiResponse")?.text()
                println "Extracted <apiResponse> tag value: $apiResponse"
            } catch(Exception ex) {
                println "Failed to parse GET response as HTML: ${ex.message}"
            }
        }
    }
}
