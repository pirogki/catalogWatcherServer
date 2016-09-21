package com.catalogWatcher.server;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

public class SimpleMqttServer implements MqttCallback {

	MqttClient myClient;
	MqttConnectOptions connOpt;

	static String watchDir = "src";
	static String brokerUrl = "tcp://localhost:1883";
	static final String TOPIC = "catalogWatcer";
	static final String REQUESTS_TOPIC = "requests";
	static final String CLIENT_ID = "subscriber";
	// the following two flags control whether this example is a publisher, a
	// subscriber or both
	static final Boolean subscriber = false;
	static final Boolean publisher = true;

	/**
	 * 
	 * connectionLost This callback is invoked upon losing the MQTT connection.
	 * 
	 */

	public void connectionLost(Throwable t) {
		System.out.println("Connection lost!");
		// code to reconnect to the broker would go here if desired
	}

	/**
	 * 
	 * deliveryComplete This callback is invoked when a message published by
	 * this client is successfully received by the broker.
	 * 
	 */

	public void deliveryComplete(MqttDeliveryToken token) {
		// System.out.println("Pub complete" + new
		// String(token.getMessage().getPayload()));
	}

	/**
	 * 
	 * messageArrived This callback is invoked when a message is received on a
	 * subscribed topic.
	 * 
	 */

	public void messageArrived(MqttTopic topic, MqttMessage message)
			throws Exception {
		System.out.println("-------------------------------------------------");
		System.out.println("| Topic:" + topic.getName());
		System.out.println("| Message: " + new String(message.getPayload()));
		System.out.println("-------------------------------------------------");
	}

	/**
	 * 
	 * MAIN
	 * 
	 * @throws Exception
	 * 
	 */
	public static void main(String[] args) throws Exception {
		if(args.length == 2)
		{
			brokerUrl = args[0];
			watchDir = args[1];
		}
		
		SimpleMqttServer smc = new SimpleMqttServer();
		smc.runServer();
	}

	public void publish(String stringMessage)
	{


		String myTopic = TOPIC;
		MqttTopic topic = myClient.getTopic(myTopic);
		
		String pubMsg = stringMessage;
		int pubQoS = 0;
		MqttMessage message = new MqttMessage(pubMsg.getBytes());
		message.setQos(pubQoS);
		message.setRetained(false);

		// Publish the message
		System.out.println("Publishing to topic \"" + topic
				+ "\" qos " + pubQoS + " "+ stringMessage);
		MqttDeliveryToken token = null;
		
		
		try {
			// publish message to broker
			token = topic.publish(message);
			// Wait until the message has been delivered to the
			// broker
			//token.waitForCompletion();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// disconnect
		/*try {
			// wait to ensure subscribed messages are delivered
			if (subscriber) {
				Thread.sleep(5000);
			}
			myClient.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}
	
	/**
	 * 
	 * runClient The main functionality of this simple example. Create a MQTT
	 * client, connect to broker, pub/sub, disconnect.
	 * 
	 * @throws Exception
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void runServer() throws Exception {

		
		String clientID = CLIENT_ID;
		connOpt = new MqttConnectOptions();

		//connOpt.setCleanSession(true);
		//connOpt.setKeepAliveInterval(30);

		// Connect to Broker
		try {
			myClient = new MqttClient(brokerUrl, clientID);
			myClient.setCallback(this);
			myClient.connect(connOpt);
			System.out.println("Connected to " + brokerUrl);
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		try {
			int subQoS = 0;
			myClient.subscribe(REQUESTS_TOPIC, subQoS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		

		
		Path dir = Paths.get(watchDir);
		
		// Создаем объект WatchService
		WatchService watcher = FileSystems.getDefault().newWatchService();
		// Регистрируем отслеживаемые события:
		WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE,
				ENTRY_MODIFY);
		while (true) { // бесконечный цикл
			key = watcher.take(); // ожидаем следующий набор событий
			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent<Path> ev = (WatchEvent<Path>) event;
				publish(ev.kind().name() + " " +  dir.resolve(ev.context()));
			}
			key.reset(); // сбрасываем состояние набора событий

			

			

			// setup topic
			// topics on m2m.io are in the form <domain>/<stuff>/<thing>


			

		}
	}

	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub

	}

	public void messageArrived(String topic, MqttMessage message)
			throws Exception {

		//Получаем текущее дерево файлов
		FileTreeViver ftv = new FileTreeViver(watchDir);
		publish(ftv.getTree());
	}
}