package org.jenkinsci.plugins.mwjpi.rest;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.client.ClientConfig;
import org.json.JSONObject;

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

    /**
     * Sample query:
     *        full url: http://localhost:8000/employees?ordering=first_name,last_name&limit=999"
     *        query("employees/", "ordering=first_name,last_name&limit=999")
     *
     * @param path relative rest url path 
     * @param queryParams query parameters separated by &amp;
     *        e.g. ordering=first_name,last_name&limit=999
     *             name=book&amp;location=fremont
     * @return
     */
    public static String query(String path, String queryParams) {

        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        WebTarget target = client.target(getBaseURI());

        target = target.path(path);
        Map<String, String> map = toQueryMap(queryParams);
        Iterator<Entry<String, String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, String> entry = it.next();
            target = target.queryParam(entry.getKey(), entry.getValue());
        }
        // token authentication
        String result = target.request().header("Authorization", "Token " + prop.getProperty(ACCESS_TOKEN))
                .accept(MediaType.APPLICATION_JSON).get(String.class);
        return result;
    }

    public static String post4AccessToken(String username, String password) {
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        WebTarget target = client.target(getBaseURI());

        Form form = new Form();
        form.param("username", username);
        form.param("password", password);
        String data = target.path("access-token").request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED), String.class);
        JSONObject obj = new JSONObject(data);
        String token = obj.getString("token");
        return token;
    }
    
    private static Map<String, String> toQueryMap(String query) {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params) {
            String name  = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri(prop.getProperty(BASE_URL)).build();
    }
}