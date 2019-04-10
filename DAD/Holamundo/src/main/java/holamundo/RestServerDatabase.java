package holamundo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class RestServerDatabase extends AbstractVerticle {

	private AsyncSQLClient mySQLClient;

	public void start(Future<Void> startFuture) {
		JsonObject config = new JsonObject().put("host", "localhost").put("username", "root").put("password", "root")
				.put("database", "dad_db").put("port", 3306);
		mySQLClient = MySQLClient.createShared(vertx, config);

		Router router = Router.router(vertx);
		vertx.createHttpServer().requestHandler(router).listen(8090, result -> {
			if (result.succeeded()) {
				System.out.println("Servidor database desplegado");
			} else {
				System.out.println("Error de despliegue");
			}
		});

		router.route().handler(BodyHandler.create());
		router.post("/arranque").handler(this::handleArranque);
		router.post("/encendido").handler(this::handleEncendidoPS4);
		router.post("/apagado").handler(this::handleApagadoPS4);
		router.get("/usuarios").handler(this::handleAllSensors);
		router.post("/ActivacionControlParental").handler(this::handleActivacionControlParental);
		router.get("/products/:productID/info").handler(this::handleProduct);
		router.put("/products/:productID/:property").handler(this::handleProductProperty);
		router.put("/usuarios").handler(this::handleUsuario);

	}

	// ESTADO PLACA
	private void handleArranque(RoutingContext routingConext) {

		JsonObject idJson = routingConext.getBodyAsJson();
		int intId = idJson.getInteger("id");

		mySQLClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().query(
						"UPDATE `dad_db`.`placa` SET `estado_placa` = 1 WHERE `idplaca` = +" + intId + "; ", result -> {

							if (result.succeeded()) {

								JsonObject responseJson = new JsonObject();
								responseJson.put("id", intId);
								routingConext.response().end(responseJson.encode());
								System.out.println(result);

							} else {
								System.out.println(result.cause().getMessage());
								routingConext.response().setStatusCode(400).end();

							}
							connection.result().close();
						});
			} else {
				connection.result().close();
				System.out.println(connection.cause().getMessage());
				routingConext.response().setStatusCode(400).end();
			}
		});
	}

	// ACTIVACION CONTROL PARENTAL
	private void handleActivacionControlParental(RoutingContext routingConext) {
		
		//long unixTime2 = 0; // = System.currentTimeMillis() / 1000L;
		// convert seconds to milliseconds

		JsonObject idJson = routingConext.getBodyAsJson();
		int intId = idJson.getInteger("id");
		int activado = idJson.getInteger("activado");
		String fechaInicio = idJson.getString("fI");
		String fechaFin = idJson.getString("fF");
		DateFormat formate = new SimpleDateFormat("dd-MM-yyyy,HH:mm:ss");

		long fechaIni = convierteFecha(fechaInicio, formate).getTime()/1000L;
		long fechaFi = convierteFecha(fechaFin, formate).getTime()/1000L;
		//unixTime = fechaIni.getTime() / 1000L;

		mySQLClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().query(
						"UPDATE `dad_db`.`placa` SET `permitir_encendido` = 0 WHERE `idplaca` = +" + intId + "; ",
						result -> {

							if (result.succeeded()) {

								JsonObject responseJson = new JsonObject();

								connection.result().query(
										"INSERT INTO `dad_db`.`control_parental`(`activado`,`id_placa`,`fecha_hora_comienzo`,`fecha_hora_fin`)VALUES("
												+ activado + "," + intId + "," + fechaIni + "," + fechaFi + "); ",
										result2 -> {

											if (result.succeeded()) {

												responseJson.put("id", intId);
												routingConext.response().end(responseJson.encode());
												System.out.println(result);

											} else {
												System.out.println("1111111111111111");
												System.out.println(result.cause().getMessage());
												routingConext.response().setStatusCode(400).end();

											}
											connection.result().close();
										});

							} else {
								System.out.println("22222222222222222");
								System.out.println(result.cause().getMessage());
								routingConext.response().setStatusCode(400).end();

							}
							connection.result().close();
						});

			} else {
				System.out.println("33333333333333333");
				connection.result().close();
				System.out.println(connection.cause().getMessage());
				routingConext.response().setStatusCode(400).end();

			}
		});
	}

	private Date convierteFecha(String fechaInicio, DateFormat formate) {
		Date fechaIni = null;
		try {
			fechaIni = formate.parse(fechaInicio);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		}
		return fechaIni;
	}

	// DETECTAR ENCENDIDO ps4
	private void handleEncendidoPS4(RoutingContext routingConext) {
		long unixTime = System.currentTimeMillis() / 1000L;
		JsonObject idJson = routingConext.getBodyAsJson();
		int intId = idJson.getInteger("id");

		mySQLClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().query(
						"UPDATE `dad_db`.`placa` SET `estado_placa` = 1 WHERE `idplaca` = +" + intId + "; ", result -> {

							if (result.succeeded()) {

								JsonObject responseJson = new JsonObject();

								connection.result()
										.query("INSERT INTO `dad_db`.`historial`(`placa`,`fecha_hora_comienzo`)VALUES("
												+ intId + "," + unixTime + "); ", result2 -> {

													if (result.succeeded()) {

														responseJson.put("id", intId);
														routingConext.response().end(responseJson.encode());
														System.out.println(result);

													} else {
														System.out.println(result.cause().getMessage());
														routingConext.response().setStatusCode(400).end();

													}
													connection.result().close();
												});

							} else {
								System.out.println(result.cause().getMessage());
								routingConext.response().setStatusCode(400).end();

							}
							connection.result().close();
						});

			} else {
				connection.result().close();
				System.out.println(connection.cause().getMessage());
				routingConext.response().setStatusCode(400).end();
			}
		});
	}

	// DETECTAR APAGADO ps4
	private void handleApagadoPS4(RoutingContext routingConext) {
		long unixTime = System.currentTimeMillis() / 1000L;

		JsonObject idJson = routingConext.getBodyAsJson();
		int intId = idJson.getInteger("id");

		mySQLClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().query("SELECT MAX(idhistorial)FROM dad_db.historial WHERE placa = " + intId + " ;",
						result -> {
							JsonObject responseJson = new JsonObject();
							if (result.succeeded()) {
								List<JsonObject> l = result.result().getRows();
								int num = l.get(0).getInteger("MAX(idhistorial)");
								System.out.println(num);
								connection.result().query("UPDATE `dad_db`.`historial` SET `fecha_hora_fin` = "
										+ unixTime + " WHERE `idhistorial` = " + num + "; ", result2 -> {

											if (result.succeeded()) {

												responseJson.put("id", intId);
												routingConext.response().end(responseJson.encode());
												System.out.println(result);

											} else {
												System.out.println(result.cause().getMessage());
												routingConext.response().setStatusCode(400).end();

											}
											connection.result().close();
										});

								routingConext.response().setStatusCode(200).end();

							} else {
								System.out.println(result.cause().getMessage());
								routingConext.response().setStatusCode(400).end();

							}
							connection.result().close();
						});

			} else {
				connection.result().close();
				System.out.println(connection.cause().getMessage());
				routingConext.response().setStatusCode(400).end();
			}
		});
	}

	private void handleAllSensors(RoutingContext routingConext) {
		mySQLClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().query("SELECT * FROM dad_db.usuario;", result -> {
					if (result.succeeded()) {
						String jsonResult = result.result().toJson().encodePrettily();
						routingConext.response().end(jsonResult);
						System.out.println(result);
					} else {
						System.out.println(result.cause().getMessage());
						routingConext.response().setStatusCode(400).end();
					}
					connection.result().close();
				});
			} else {
				connection.result().close();
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
		routingContext.response().putHeader("content-type", "application/json").end(jsonObject.encode());
	}

	private void handleUsuario(RoutingContext routingContext) {
		try {

			JsonObject jsonObject = routingContext.getBodyAsJson();
			String strNom = jsonObject.getString("nombre_usuario");
			String strCon = jsonObject.getString("contraseña");
			System.out.println(strNom + " " + strCon);
			mySQLClient.getConnection(connection -> {
				if (connection.succeeded()) {
					connection.result().query("INSERT INTO `dad_db`.`usuario`(`nombre_usuario`, `contraseña`) VALUES(\""
							+ strNom + "\",\"" + strCon + "\");", result -> {
								if (result.succeeded()) {

									String jsonResult = result.result().toJson().encodePrettily();
									routingContext.response().end(jsonResult);
									System.out.println(result);
								} else {
									System.out.println(result.cause().getMessage());
									routingContext.response().setStatusCode(400).end();
								}
								connection.result().close();
							});
				} else {
					connection.result().close();
					System.out.println(connection.cause().getMessage());
					routingContext.response().setStatusCode(400).end();
				}
			});
		} catch (Exception e) {
			System.out.println(e);
			routingContext.response().setStatusCode(400).end();
		}
	}

	private void handleProductProperty(RoutingContext routingContext) {
		String paramStr = routingContext.pathParam("productID");
		int paramInt = Integer.parseInt(paramStr);
		JsonObject body = routingContext.getBodyAsJson();
		// Peticion BBDD
		routingContext.response().putHeader("content-type", "application/json").end(body.encode());
	}

}
