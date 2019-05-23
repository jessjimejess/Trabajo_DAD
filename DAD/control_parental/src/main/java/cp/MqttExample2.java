package cp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Stream;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttTopicSubscription;
import io.vertx.mqtt.messages.MqttPublishMessage;

public class MqttExample2 extends AbstractVerticle {

	private static Multimap<String, MqttEndpoint> clientTopics;

	public void start(Future<Void> startFuture) {
		MqttClient mqttClient = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));

		/*
		 * Nos conectamos al servidor que está desplegado por el puerto 1883 en la
		 * propia máquina. Recordad que localhost debe ser sustituido por la IP de
		 * vuestro servidor. Esta IP puede cambiar cuando os desconectáis de la red, por
		 * lo que aseguraros siempre antes de lanzar el cliente que la IP es correcta.
		 */
		mqttClient.connect(1883, "192.168.43.48", s -> {

			/*
			 * Nos suscribimos al topic_2. Aquí debera indicar el nombre del topic al que os
			 * queréis suscribir. Además, podéis indicar el QoS, en este caso AT_LEAST_ONCE
			 * para asegurarnos de que el mensaje llega a su destinatario.
			 */
			mqttClient.subscribe("topic_2", MqttQoS.AT_LEAST_ONCE.value(), handler -> {
				if (handler.succeeded()) {
					/*
					 * En este punto el cliente ya está suscrito al servidor, puesto que se ha
					 * ejecutado la función de handler
					 */
					System.out.println("Cliente " + mqttClient.clientId() + " suscrito correctamente al canal topic_2");

					/*
					 * Además de suscribirnos al servidor, registraremos un manejador para
					 * interceptar los mensajes que lleguen a nuestro cliente. De manera que el
					 * proceso sería el siguiente: El cliente exterbi envía un mensaje al servidor
					 * -> el servidor lo recibe y busca los clientes suscritos al topic -> el
					 * servidor reenvía el mensaje a esos clientes -> los clientes (en este caso el
					 * cliente actual) recibe el mensaje y lo procesa si fuera necesario.
					 */
					Random random = new Random();
					mqttClient.publish("topic_2", Buffer.buffer(new JsonObject().put("action", random.nextInt(2) == 0 ? "hola" : "adion")
							.put("timestamp", Calendar.getInstance().getTimeInMillis())
							.put("clientId", mqttClient.clientId()).encode()),MqttQoS.AT_LEAST_ONCE, false, false);
					mqttClient.publishHandler(new Handler<MqttPublishMessage>() {
						@Override
						public void handle(MqttPublishMessage arg0) {
							/*
							 * Si se ejecuta este código es que el cliente 2 ha recibido un mensaje
							 * publicado en algún topic al que estaba suscrito (en este caso, al topic_2).
							 */
							JsonObject message = new JsonObject(arg0.payload());
							System.out.println("-----" + message.getString("clientId"));
							System.out.println("-----" + mqttClient.clientId());
							if (!message.getString("clientId").equals(mqttClient.clientId()))
								System.out.println("Mensaje recibido por el cliente: " + arg0.payload().toString());
						}
					});
				}
			});

			/*
			 * Este timer envía mensajes desde el cliente al servidor cada 3 segundos.
			 */
			new Timer().scheduleAtFixedRate(new TimerTask() {

				@Override
				public void run() {
					/*
					 * Publicamos un mensaje en el topic "topic_2"
					 */
					// El cliente envia un mensaje cada 3 segundos
					Random random = new Random();
					if (mqttClient.isConnected()) {
						mqttClient.publish("topic_2",
								Buffer.buffer(new JsonObject().put("action", random.nextInt(2) == 0 ? "on" : "off")
										.put("timestamp", Calendar.getInstance().getTimeInMillis())
										.put("clientId", mqttClient.clientId()).encode()),
								MqttQoS.AT_LEAST_ONCE, false, false);
					}
				}
			}, 1000, 3000);
		});
	}

	/**
	 * Método encargado de gestionar las suscripciones de los clientes a los
	 * diferentes topics. En este método se registrará el cliente asociado al topic
	 * al que se suscribe
	 * 
	 * @param endpoint
	 */
	private static void handleSubscription(MqttEndpoint endpoint) {
		endpoint.subscribeHandler(subscribe -> {
			// Los niveles de QoS permiten saber el tipo de entrega que se realizará:
			// - AT_LEAST_ONCE: Se asegura que los mensajes llegan a los clientes, pero no
			// que se haga una única vez (pueden llegar duplicados)
			// - EXACTLY_ONCE: Se asegura que los mensajes llegan a los clientes un única
			// vez (mecanismo más costoso)
			// - AT_MOST_ONCE: No se asegura que el mensaje llegue al cliente, por lo que no
			// es necesario ACK por parte de éste
			List<MqttQoS> grantedQosLevels = new ArrayList<>();
			for (MqttTopicSubscription s : subscribe.topicSubscriptions()) {
				System.out.println("Suscripción al topic " + s.topicName() + " con QoS " + s.qualityOfService());
				grantedQosLevels.add(s.qualityOfService());

				// Añadimos al cliente en la lista de clientes suscritos al topic
				clientTopics.put(s.topicName(), endpoint);
			}

			/*
			 * Enviamos el ACK al cliente de que se ha suscrito al topic con los niveles de
			 * QoS indicados
			 */
			endpoint.subscribeAcknowledge(subscribe.messageId(), grantedQosLevels);
		});
	}
}