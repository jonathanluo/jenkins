package org.jenkinsci.plugins.mwjpi.rest;

import java.net.URI;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.client.ClientConfig;

public class RestClient {

    private static String ACCESS_TOKEN = "836...";

    public static String query() {
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        WebTarget target = client.target(getBaseURI());

        // token authentication
        String result = target.path("rest/test_type/5").request().header("Authorization", "Token " + ACCESS_TOKEN)
                .accept(MediaType.APPLICATION_JSON).get(String.class);
        return result;
    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://192.168.57.67:9005").build();
    }
}