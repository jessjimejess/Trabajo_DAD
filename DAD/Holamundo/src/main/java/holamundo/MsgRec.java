package holamundo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;

public class MsgRec extends AbstractVerticle {
	public void start(Future<Void> startFuture) {
		vertx.deployVerticle(new MsgClient());
		vertx.deployVerticle(new MsgBroadcast());
		EventBus eventBus = vertx.eventBus();
		eventBus.consumer("mensaje-punto-a-punto", message -> {
			String messageRec = message.body().toString();
			System.out.println(messageRec);
			String messageResponse = "Si, yo te escucho. " + this.getClass().getName();
			message.reply(messageResponse);
		});
	}
	
	public void stop(Future<Void> stopFuture) {
		vertx.undeploy("MsgClient");
	}
}
