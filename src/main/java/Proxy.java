import io.netty.handler.codec.http.HttpRequest;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by allen on 4/29/15.
 */
public class Proxy extends AbstractVerticle {
    private String fileName = "test";
    private int fileSuf = 0;

    @Override
    public void start() throws Exception {
        HttpClient client = vertx.createHttpClient(new HttpClientOptions());

        vertx.createHttpServer(new HttpServerOptions().setPort(8080)).requestHandler(req -> {
            System.out.println("Proxying request: " + req.uri());

            String host = req.headers().get("host");
            String hostAddress = host;
            int hostPort = 80;
            if (host.indexOf(":") > 0) {
                hostAddress = host.substring(0, host.indexOf(":"));
                hostPort = Integer.valueOf(host.substring(host.indexOf(":") + 1));
            }

            System.out.println("hostAddress = " + hostAddress + " | hostPort = " + hostPort);

            for (String name : req.headers().names()) {
                System.out.println("name: " + name);
            }


            HttpClientRequest c_req = client.request(req.method(), hostPort, hostAddress, req.uri(), c_res -> {
                fileSuf++;
                System.out.println("Proxying response: " + c_res.statusCode());

                for (String name : c_res.headers().names()) {
                    System.out.println("Resp name: " + name);
                }


                req.response().setStatusCode(c_res.statusCode());
                req.response().headers().setAll(c_res.headers());

                c_res.handler(data -> {
                    System.out.println("Proxying response body: " + data.toString("ISO-8859-1"));
                    req.response().write(data);
                    /*
                    File file = new File(fileName + fileSuf+".png");
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        fileOutputStream.write(data.getBytes());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    */
                });
                c_res.endHandler((v) -> req.response().end());
            });
            if (req.method().equals(HttpMethod.POST) ||
                    req.method().equals(HttpMethod.PUT)) {
                if (req.headers().get("Content-Length") == null) {
                    c_req.setChunked(true);
                }
            }

            //c_req.setChunked(true);
            c_req.headers().setAll(req.headers());
            req.handler(data -> {
                System.out.println("Proxying request body " + data.toString("ISO-8859-1"));
                c_req.write(data);
            });
            req.endHandler((v) -> c_req.end());
        }).listen();
    }

    public void main(String args[]) {
        Proxy proxy = new Proxy();
        Vertx.vertx().deployVerticle(proxy);
    }
}   //Proxy
