package holamundo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class Json extends AbstractVerticle {

	public void start(Future<Void> startFuture) {

		String pruebaJsonCadena = "{\"id\":8,\"nombre\":\"Juan Carlos\",\"empresa\":\"Bamburys\"}";
		JsonObject jsonObject = new JsonObject();
		JsonObject jsonObject2 = new JsonObject();
		jsonObject.put("elem", "Object");
		jsonObject.put("elem1", "Object");
		jsonObject.put("elem2", "Object");
		jsonObject.put("elem3", "Object");
		jsonObject.put("elem4", "Object");
		jsonObject.put("elem5", "Object");
		jsonObject.put("elem6", "Object");
		
		
		jsonObject2.put("elem7", "Object asas");
		jsonObject2.put("elem8", "Object asdasdas");
		jsonObject.put("elem9", jsonObject2);
		// System.out.println(jsonObject.toString()); //Inconveniente no lo formatea
		System.out.println(jsonObject.encodePrettily());
		System.out.println(jsonObject.encode());
	}
}
