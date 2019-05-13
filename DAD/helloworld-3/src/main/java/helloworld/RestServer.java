package helloworld;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class RestServer extends AbstractVerticle{
	
	private AsyncSQLClient mySQLClient;
	
	public void start(Future<Void> startFuture) {
		
		JsonObject config = new JsonObject()
				.put("host", "localhost")
				.put("username", "root")
				.put("password", "root")
				.put("database", "dad_db")
				.put("port", 3306);
		mySQLClient = 
				MySQLClient.createShared(vertx, config);
		
				
		
		Router router = Router.router(vertx);
		vertx.createHttpServer().requestHandler(router).
			listen(8081, result -> {
				if (result.succeeded()) {
					System.out.println("Servidor desplegado");
				}else {
					System.out.println("Error de despliegue");
				}
			});
		router.route().handler(BodyHandler.create());
		router.get("/sensors").handler(this::handleAllSensors);
		router.get("/products/:productID/info").handler(this::handleProduct);
		router.put("/products/:productID/:property").handler(this::handleProductProperty);
	}
	
	private void handleAllSensors(RoutingContext routingConext) {
		mySQLClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().query("SELECT * FROM sensor" , result -> {
					if (result.succeeded()) {
						String jsonResult = new JsonArray(result.result().getResults()).encodePrettily();
						routingConext.response().end(jsonResult);
					}else {
						System.out.println(result.cause().getMessage());
						routingConext.response().setStatusCode(400).end();
					}
				});
			}else {
				System.out.println(connection.cause().getMessage());
				routingConext.response().setStatusCode(400).end();
			}
		});
	}
	
	private void handleProduct(RoutingContext routingContext) {
		String paramStr = routingContext.pathParam("productID");
		int paramInt = Integer.parseInt(paramStr);
		JsonObject jsonObject = new JsonObject();
		jsonObject.put("serial", "asfas234ewsdcdwe24");
		jsonObject.put("id", paramInt);
		jsonObject.put("name", "TV Samsung");
		routingContext.response()
			.putHeader("content-type", "application/json")
			.end(jsonObject.encode());
	}
	
	private void handleProductProperty(RoutingContext routingContext) {
		String paramStr = routingContext.pathParam("productID");
		int paramInt = Integer.parseInt(paramStr);
		JsonObject body = routingContext.getBodyAsJson();
		// Petici�n BBDD
		routingContext.response()
		.putHeader("content-type", "application/json")
		.end(body.encode());
	}

}
