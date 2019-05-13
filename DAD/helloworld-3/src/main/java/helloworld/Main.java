package helloworld;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

public class Main extends AbstractVerticle {

	public void start(Future<Void> startFuture) {
		// vertx.deployVerticle(new CommReceiver());
		// vertx.deployVerticle(new JsonExamples());
		//vertx.deployVerticle(new HttpServer());
		// vertx.deployVerticle(new TcpExample());
		// vertx.deployVerticle(new CommSender());
		// vertx.deployVerticle(new CommReceiverBroadcast());
		// vertx.deployVerticle(new CommReceiverBroadcast2());
		//vertx.deployVerticle(new TcpServer());
		//vertx.deployVerticle(new RestServer());
		vertx.deployVerticle(new RestServerDatabase());
		vertx.deployVerticle(new MqttExample());

		/*for (int i = 0; i < 10000; i++) {
			vertx.deployVerticle(new TcpClient());
		}*/
	}
}
