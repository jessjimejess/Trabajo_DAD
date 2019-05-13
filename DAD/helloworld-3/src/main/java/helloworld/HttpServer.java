package helloworld;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

public class HttpServer extends AbstractVerticle {

	public void start(Future<Void> startFuture) {
		vertx.createHttpServer().requestHandler(r -> {
			r.response().end("<b>Hola cliente</b><br/>Ejemplo");
		}).listen(8081, status -> {
			if (status.succeeded()) {
				System.out.println("Servidor HTTP desplegado");
				startFuture.succeeded();
			} else {
				System.out.println("Error en el despliegue. " + status.cause().getMessage());
				startFuture.fail(status.cause());
			}
		});

	}
}