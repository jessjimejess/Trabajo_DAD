package holamundo;

import java.util.concurrent.ThreadLocalRandom;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;

public class TcpClient extends AbstractVerticle {

	public void start(Future<Void> startFuture) {

		NetClientOptions netClientOptions = new NetClientOptions().setConnectTimeout(10000).setReconnectAttempts(5)
				.setReconnectInterval(5000);
		NetClient netClient = vertx.createNetClient(netClientOptions);
		netClient.connect(8085, "localhost", conn -> {
			if (conn.succeeded()) {
				conn.result().handler(response -> {
					System.out.println(response.toString());
				});
				vertx.setPeriodic(5000, m -> { // Genera lo mismo cada 2 segundos
					JsonObject jsonObject = new JsonObject();
					int numero = ThreadLocalRandom.current().nextInt(0, 3);
					jsonObject.put("req", numero);
					jsonObject.put("content", "temperature");
					conn.result().write(jsonObject.encode());
				});

			} else {
				System.out.println("Error durante la conexion al servicio: " + conn.cause().getMessage());
			}
		});
	}

}
