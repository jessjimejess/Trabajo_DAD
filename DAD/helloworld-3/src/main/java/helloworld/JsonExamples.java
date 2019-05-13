package helloworld;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class JsonExamples extends AbstractVerticle{
	public void start(Future<Void> startFuture) {
		JsonObject jsonObject = new JsonObject();
		JsonObject jsonObject2 = new JsonObject();
		jsonObject.put("elem", 1231);
		jsonObject.put("elem1", "1231");
		jsonObject.put("elem2", "Object");
		jsonObject.put("elem3", "Object");
		jsonObject.put("elem4", "Object");
		jsonObject.put("elem5", "Object");
		jsonObject2.put("elem6", "Object sdfs");
		jsonObject2.put("elem7", "Object asdasd");
		jsonObject.put("elem8", jsonObject2);
		System.out.println(jsonObject.encodePrettily());
		System.out.println(jsonObject.encode());
		
		JsonObject decode = new JsonObject(jsonObject.encode());
		System.out.println(decode.getString("elem1"));
		System.out.println(decode.getInteger("elem"));
		System.out.println(decode.getDouble("elem"));
	}
}
