package helloworld;

import java.util.Random;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;

public class TcpServer extends AbstractVerticle {

	public void start(Future<Void> startFuture) {
		NetServerOptions netServerOptions = new NetServerOptions();
		netServerOptions.setPort(8086);
		// netServerOptions.setIdleTimeout(idleTimeout)

		NetServer netServer = vertx.createNetServer(netServerOptions);
		netServer.connectHandler(connection -> {
			JsonObject jsonObject = new JsonObject();
			jsonObject.put("body", "Conexión realizada correctamente").put("status", 200)
					.put("serverAddress", connection.localAddress().toString()).put("clientAddress", connection.remoteAddress().toString());
			System.out.println("Server: " + jsonObject.encodePrettily());
			connection.write(jsonObject.encode());
			connection.handler(request -> {
				JsonObject requestMsg = request.toJsonObject();
				Random random = new Random();
				JsonObject response = new JsonObject();
				if (requestMsg.containsKey("req")) {
					switch (requestMsg.getInteger("req")) {
					case 1:
						response.put("response", "El dispositivo se ha encendido correctamente");
						response.put("status", 200);
						break;
					case 2:
						response.put("response", "El valor se ha obtenido con éxito");
						if (requestMsg.getString("content").equals("temperature")) {
							response.put("value", 10 + random.nextInt(20));
						} else if (requestMsg.getString("content").equals("pressure")) {
							response.put("value", 1000 + random.nextInt(50));
						} else if (requestMsg.getString("content").equals("humidity")) {
							response.put("value", 40 + random.nextInt(60));
						} else {
							response.put("value", -1);
						}
						response.put("status", 200);
						break;

					default:
						response.put("response", "El dispositivo se ha reseteado");
						response.put("status", 200);
						break;
					}
				} else {
					response.put("response", "");
					response.put("status", 300);
				}
				response.put("clientAddress", connection.remoteAddress().toString());
				System.out.println("Server: " + response.encodePrettily());
				connection.write(response.encode());
			});
		});
		
		netServer.listen(deploy -> {
			if (deploy.succeeded()) {
				System.out.println("Servidor desplegado correctamente");
				startFuture.complete();
			}else {
				System.out.println(deploy.cause().getMessage());
				startFuture.fail(deploy.cause());
			}
		});

	}

}
