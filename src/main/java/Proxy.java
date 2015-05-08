import io.netty.handler.codec.http.HttpRequest;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.*;
import io.vertx.core.streams.Pump;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by allen on 4/29/15.
 */
public class Proxy extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        HttpClient client = vertx.createHttpClient(new HttpClientOptions());

        vertx.createHttpServer(new HttpServerOptions().setPort(8080)).requestHandler(req -> {

            String host = req.headers().get("host");
            String hostAddress = host;
            int hostPort = 80;
            if (host.indexOf(":") > 0) {
                hostAddress = host.substring(0, host.indexOf(":"));
                hostPort = Integer.valueOf(host.substring(host.indexOf(":") + 1));
            }

            HttpClientRequest c_req = client.request(req.method(), hostPort, hostAddress, req.uri(), c_res -> {

                req.response().setStatusCode(c_res.statusCode());
                req.response().setStatusMessage(c_res.statusMessage());
                req.response().headers().setAll(c_res.headers());
                if (c_res.headers().get("Content-Length") == null) {
                    req.response().setChunked(true);
                }

                Pump target2Proxy = Pump.pump(c_res, req.response());
                target2Proxy.start();

                c_res.endHandler((v) -> req.response().end());
            });
            if (req.method().equals(HttpMethod.POST) || req.method().equals(HttpMethod.PUT)) {
                if (req.headers().get("Content-Length") == null) {
                    c_req.setChunked(true);
                }
            }

            c_req.headers().setAll(req.headers());
            Pump proxy2Target = Pump.pump(req, c_req);
            proxy2Target.start();

            req.endHandler((v) -> c_req.end());
        }).listen(httpServerAsyncResult -> {
            if (httpServerAsyncResult.succeeded()) {
                System.out.println("Binding succeed!");
            } else {
                System.out.println("Binding failed");
                try {
                    throw httpServerAsyncResult.cause();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });
    }

    public static void main(String args[]) {
        Proxy proxy = new Proxy();
        Vertx.vertx().deployVerticle(proxy);
    }
}   //Proxy
