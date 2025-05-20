package org.example

import org.apache.http.auth.AuthScope
import org.apache.http.auth.NTCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.client.methods.HttpGet
import org.apache.http.HttpResponse
import org.apache.http.util.EntityUtils

class FetchApiWithNtlmAuth {
    static void main(String[] args) {
        // NTLM credentials
        String username = "your_username"
        String password = "your_password"
        String workstation = "your_workstation" // Can be blank if not needed
        String domain = "your_domain"
        String url = "https://ntlm-protected-endpoint.example.com/api"

        CredentialsProvider credsProvider = new BasicCredentialsProvider()
        credsProvider.setCredentials(
            AuthScope.ANY,
            new NTCredentials(username, password, workstation, domain)
        )

        CloseableHttpClient httpclient = HttpClients.custom()
            .setDefaultCredentialsProvider(credsProvider)
            .build()
        try {
            HttpGet httpget = new HttpGet(url)
            HttpResponse response = httpclient.execute(httpget)
            int statusCode = response.getStatusLine().getStatusCode()
            String responseBody = EntityUtils.toString(response.getEntity())
            println "Status: $statusCode"
            println "Response: $responseBody"
        } finally {
            httpclient.close()
        }
    }
}