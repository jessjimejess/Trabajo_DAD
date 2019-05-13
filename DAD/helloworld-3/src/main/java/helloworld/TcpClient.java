package helloworld;

import java.util.Random;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;

public class TcpClient extends AbstractVerticle {

	public void start(Future<Void> startFuture) {
		NetClientOptions netClientOptions = new NetClientOptions();
		netClientOptions.setConnectTimeout(10000).setReconnectAttempts(5).setReconnectInterval(1000);
		NetClient netClient = vertx.createNetClient(netClientOptions);
		netClient.connect(8086, "127.0.0.1", connection -> {
			if (connection.succeeded()) {
				Random random = new Random();

				connection.result().handler(message -> {
					System.out.println("Cliente: " + message.toJsonObject().encodePrettily());
				});

				vertx.setPeriodic(2000 + random.nextInt(3000), h -> {
					JsonObject request = new JsonObject();
					switch (random.nextInt(3)) {
					case 0:
						request.put("req", 1);
						break;
					case 1:
						request.put("req", 2);
						switch (random.nextInt(3)) {
						case 0:
							request.put("content", "temperature");
							break;
						case 1:
							request.put("content", "humidity");
							break;
						default:
							request.put("content", "pressure");
						}
						break;
					default:
						request.put("req", 3);
						break;
					}
					connection.result().write(request.encode());
				});
			} else {
				System.out.println("Error durante la conexión al servicio. " + connection.cause().getMessage());
			}

		});
	}

}
