import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;

/**
 * Created by allen on 4/29/15.
 */
public class Client extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        HttpClientRequest request = vertx.createHttpClient(new HttpClientOptions()).request(HttpMethod.PUT, 8080, "localhost", "/", resp -> {
            System.out.println("Got response " + resp.statusCode());
            resp.bodyHandler(body -> System.out.println("Got data " + body.toString("ISO-8859-1")));
        });

        request.setChunked(true);

        for (int i = 0; i < 10; i++) {
            request.write("client-chunk-" + i);
        }

        request.end();
    }

}   //Client
