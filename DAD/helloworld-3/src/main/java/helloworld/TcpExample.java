package helloworld;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;

public class TcpExample extends AbstractVerticle {
	public void start(Future<Void> startFuture) {
		NetServerOptions options = new NetServerOptions();
		options.setPort(8083);

		NetServer tcpServer = vertx.createNetServer(options);

		tcpServer.connectHandler(connection -> {
			System.out.println("Nueva conexión: " + connection.localAddress().toString());
			connection.write("Coneción establecida");
			connection.handler(msg -> {
				System.out.println("Recibido nuevo mensaje: " + msg.toString());
				connection.write("Recibido");
			});
		});

		tcpServer.listen(status -> {
			if (status.succeeded())
				System.out.println("Servidor TCP desplegado");
			else
				System.out.println("Error en el despliegue");
		});
	}
}
