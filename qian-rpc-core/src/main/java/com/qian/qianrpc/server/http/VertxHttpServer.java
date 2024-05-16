package com.qian.qianrpc.server.http;


import com.qian.qianrpc.server.HttpServer;
import io.vertx.core.Vertx;

public class VertxHttpServer implements HttpServer {

    @Override
    public void doStart(int port) {
        // 创建 Vert.x 实例
        Vertx vertx = Vertx.vertx();
        // 创建 HTTP 服务器
        io.vertx.core.http.HttpServer server = vertx.createHttpServer();
        // 监听端口并处理请求
        /*server.requestHandler(request -> {
            // 处理请求
            System.out.println("Request received: " + request.method() + " " + request.uri());
            // 发送响应
            request.response()
                    .putHeader("content-type", "text/plain")
                    .end("Hello from Vert.x HTTP server!");
        });*/
        server.requestHandler(new HttpServerHandler());
        // 启动 HTTP 服务器并监听端口
        server.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("Server started on port " + port);
            } else {
                System.err.println("Failed to start server on port " + result.cause());
            }
        });
    }
}
