package helloworld;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;

public class CommSender extends AbstractVerticle {
	public void start(Future<Void> startFuture) {
		EventBus eventBus = vertx.eventBus();
		vertx.setPeriodic(2000, tick -> {
			eventBus.publish("mensaje-broadcast", "Soy un broadcast, ¿estáis ahí?");
			
			eventBus.send("mensaje-punto-a-punto", 
					"Hola, ¿hay alguien ahí?", 
					response -> {
						if (response.succeeded()) {
							System.out.println(response.result().body().toString());
						}else {
							System.out.println(response.cause().getMessage());
						}
					});
		});
	}
}
