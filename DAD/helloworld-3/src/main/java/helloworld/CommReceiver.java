package helloworld;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

public class CommReceiver extends AbstractVerticle{
	public void start(Future<Void> startFuture) {
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		vertx.eventBus().consumer("mensaje-punto-a-punto", 
				message -> {
					System.out.println(message.body().toString());
					String response = "Sí, yo estoy aquí. " + 
							this.getClass().getName();
					message.reply(response);
				});
	}
}
