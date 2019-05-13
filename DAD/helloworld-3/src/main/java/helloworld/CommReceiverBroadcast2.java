package helloworld;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

public class CommReceiverBroadcast2 extends AbstractVerticle{
	public void start(Future<Void> startFuture) {
		vertx.eventBus().consumer("mensaje-broadcast", 
				message -> {
					System.out.println("Eres broadcast2 : " + 
							message.body().toString());
					System.out.println("S�, eres broadcast2");
				});
	}
}