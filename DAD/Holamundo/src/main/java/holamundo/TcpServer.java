package holamundo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;

public class TcpServer extends AbstractVerticle {

	public void start(Future<Void> startFuture) {
		NetServerOptions netServerOptions = new NetServerOptions();
		netServerOptions.setPort(8085);
		NetServer netServer = vertx.createNetServer(netServerOptions);
		netServer.connectHandler(connection -> {
			connection.write("Conexion realizada");// Mensahe de respuesta al cliente al realizar una conexion con
													// nosotros
			System.out.println("nuevo cliente conectado: " + connection.remoteAddress()); // direccion del cliente
			// El objeto connection lo necesitamos para realizar una transferencia pear to
			// pear
			// Definir un handler para que nuestro servidor pueda recibir una peticion
			connection.handler(msg -> {
				System.out.println(msg.toString());
				JsonObject rep = msg.toJsonObject();
				JsonObject jsonObject = new JsonObject();
				if (rep.containsKey("req")) { // comprobar el tipo de peticion, objecto compuesto por: "req -> int" y
												// "content -> string"
					switch (rep.getInteger("req")) {

					case 1:
						jsonObject.put("response", "LED desconectada ");
						jsonObject.put("status", 200);
						break;
					case 2:
						jsonObject.put("response", "Lectura obtenida ");
						jsonObject.put("status", 200);
						break;

					default:
						jsonObject.put("response", "Peticion valida ");
						jsonObject.put("status", 200);
						break;
					}

				} else {
					jsonObject.put("response", "Error en el mensaje");
					jsonObject.put("status", 301);
				}
				jsonObject.put("client", connection.remoteAddress().toString());
				connection.write(jsonObject.encodePrettily());
				// Cada vez que un cliente realiza un peticion se crea una connection,
				// si hubiera millones de clientes se saturaria, pero si pasa un tiempo
				// sin realizar nada "Timeout", se cierra esa connection

				connection.write(jsonObject.encode()); // .encode para evitar caracteres extras y mejor lectura

			});

		});
		netServer.listen(deploy -> {
			if (deploy.succeeded()) {
				System.out.println("Servidor desplegado");
				startFuture.complete();
			} else {
				System.out.println("Error en el despliegue: " + deploy.cause().getMessage());
				startFuture.fail(deploy.cause());
			}
		}); // Levantar servidor TCP

	}

}
