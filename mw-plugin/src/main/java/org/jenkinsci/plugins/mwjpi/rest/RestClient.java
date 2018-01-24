package org.jenkinsci.plugins.mwjpi.rest;

import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.client.ClientConfig;

public class RestClient {

    private static String PROPERTY_FILE = "/restapi.properties";
    private static String BASE_URL = "base_url";
    private static String ACCESS_TOKEN = "access_token";

    static Properties prop = new Properties();
    static {
        try {
            InputStream in = RestClient.class.getResourceAsStream(PROPERTY_FILE);
            prop.load(in);
            in.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Query data from remote server using rest api
     *
     * @param path relative rest path
     * @return
     */
    public static String query(String path) {

        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        WebTarget target = client.target(getBaseURI());

        // token authentication
        String result = target.path(path).request().header("Authorization", "Token " + prop.getProperty(ACCESS_TOKEN))
                .accept(MediaType.APPLICATION_JSON).get(String.class);
        return result;
    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri(prop.getProperty(BASE_URL)).build();
    }
}