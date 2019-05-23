package cp;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

public class Main extends AbstractVerticle{

	public void start(Future<Void> startFuture) { 
		vertx.deployVerticle(new RestServerDatabase());
		//vertx.deployVerticle(new MqttExample2());
		
	
	}
}
