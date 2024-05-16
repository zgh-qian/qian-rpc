package com.qian.qianrpc.server.tcp;

import com.qian.qianrpc.server.HttpServer;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;

/**
 * Vert.x TCP 服务器
 */
public class VertxTcpServer implements HttpServer {
    @Override
    public void doStart(int port) {
        // 创建 Vertx 实例
        Vertx vertx = Vertx.vertx();
        // 创建 NetServer 实例
        NetServer server = vertx.createNetServer();
        // 处理连接请求
        server.connectHandler(socket -> {
            socket.handler(buffer -> {
                // 处理请求数据
                byte[] requestData = buffer.getBytes();
                // 处理请求
                byte[] responseData = handleRequest(requestData);
                // 发送响应数据
                socket.write(Buffer.buffer(responseData));
            });
        });
        // 启动 TCP 服务器
        server.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("TCP server started on port " + port);
            } else {
                System.out.println("TCP server start failed: " + result.cause());
            }
        });
    }

    private byte[] handleRequest(byte[] requestData) {
        return "Hello,client!".getBytes();
    }

    public static void main(String[] args) {
        new VertxTcpServer().doStart(8888);
    }
}
