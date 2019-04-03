package holamundo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;

public class MsgClient extends AbstractVerticle {
	public void start(Future<Void> startFuture) {
		EventBus eventBus = vertx.eventBus();
		vertx.setPeriodic(2000, m -> {
			eventBus.send("mensaje-punto-a-punto", "Local, ¿alguien me escucha?", reply -> {
				if (reply.succeeded()) {
					System.out.println("Respuesta recibida: " + reply.result().body().toString());
				} else {
					System.out.println("Error en la respuesta. " + reply.cause().getMessage());
				}
			});
			
			eventBus.publish("mensaje-broadcast", "Broadcast: ¿A quien le llega?", new DeliveryOptions());
		});
	}

}
