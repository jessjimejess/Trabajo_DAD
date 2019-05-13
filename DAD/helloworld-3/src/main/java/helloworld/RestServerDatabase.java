package helloworld;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.google.gson.Gson;

import helloworld.entities.Sensor;
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

public class RestServerDatabase extends AbstractVerticle{
	
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
					System.out.println("Servidor database desplegado");
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
						Gson gson = new Gson();
						List<Sensor> sensors = new ArrayList<>();
						for (JsonObject json : result.result().getRows()) {
						sensors.add(gson.fromJson(json.encode(), 
								Sensor.class));	
						}
						
						sensors.get(0).setValue("Adiós");
						
						routingConext.response().end(gson.toJson(sensors));
						
						
						//String jsonResult = new JsonArray(result.result().getRows()).encodePrettily();
						//routingConext.response().end(jsonResult);
					}else {
						System.out.println(result.cause().getMessage());
						routingConext.response().setStatusCode(400).end();
					}
					connection.result().close();
				});
			}else {
				connection.result().close();
				System.out.println(connection.cause().getMessage());
				routingConext.response().setStatusCode(400).end();
			}
		});
	}
	
	private void handleProduct(RoutingContext routingContext) {
		String paramStr = routingContext.pathParam("productID");
		JsonObject jsonObject = new JsonObject();
		jsonObject.put("serial", "asfas234ewsdcdwe24");
		jsonObject.put("id", paramStr);
		jsonObject.put("name", "TV Samsung");
		routingContext.response()
			.putHeader("content-type", "application/json")
			.end(jsonObject.encode());
	}
	
	private void handleProductProperty(RoutingContext routingContext) {
		String paramStr = routingContext.pathParam("productID");
		int paramInt = Integer.parseInt(paramStr);
		JsonObject body = routingContext.getBodyAsJson();
		// Petición BBDD
		routingContext.response()
		.putHeader("content-type", "application/json")
		.end(body.encode());
	}

}
