package mqttUtils

import org.eclipse.paho.client.mqttv3.*

var mqtttraceOn = false

class MqttUtils(val owner: String )  {
	//protected var clientid: String? = null
	protected var eventId: String? = "mqtt"
	protected var eventMsg: String? = ""
	protected lateinit var client: MqttClient
	protected var isConnected = false
	
	protected val RETAIN = false;

	fun trace( msg : String ){
		if( mqtttraceOn ) println("$msg")
	}

	fun connect(clientid: String, brokerAddr: String ): Boolean {
		try {
  			trace("       %%% MqttUtils $owner | doing connect for $clientid to $brokerAddr "  );
			client = MqttClient(brokerAddr, clientid)
            //trace("connect $brokerAddr client = $client" )
			val options = MqttConnectOptions()
			options.setKeepAliveInterval(480)
			options.setWill("unibo/clienterrors", "crashed".toByteArray(), 2, true)
			client.connect(options)
			println("       %%% MqttUtils $owner | connect DONE $clientid to $brokerAddr " )//+ " " + client
 			isConnected = true
		} catch (e: Exception) {
			println("       %%% MqttUtils $owner | for $clientid connect ERROR for: $brokerAddr" )
			isConnected = false
			println(e)
		}
		return isConnected
	}

	fun connectDone() : Boolean{
		return isConnected
	}
	
	fun disconnect() {
		try {
			println("       %%% MqttUtils $owner | disconnect " + client)
			client.disconnect()
		} catch (e: Exception) {
			println("       %%% MqttUtils $owner | disconnect ERROR ${e}")
		}
	}



	/*
         * sends to a tpoic a content of the form
         * 	 msg( MSGID, MSGTYPE, SENDER, RECEIVER, CONTENT, SEQNUM )
     */
	@Throws(MqttException::class)
	fun publish( topic: String, msg: String?, qos: Int=2, retain: Boolean=false) {
		val message = MqttMessage()
		message.setRetained(retain)
		if (qos == 0 || qos == 1 || qos == 2) {
			//qos=0 fire and forget; qos=1 at least once(default);qos=2 exactly once
			message.setQos(0)
		}
		message.setPayload(msg!!.toByteArray())
		try {
			client.publish(topic, message)
		} catch (e:Exception) {
			println("       %%% MqttUtils $owner | publish ERROR $e topic=$topic msg=$msg"  )
 		}
	}
	@Throws(MqttException::class)
	fun emit( msg: String?, qos: Int=2, retain: Boolean=false) {
		publish("unibo/gozzi/events",msg)
	}
	
}