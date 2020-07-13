package kb

import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import org.eclipse.paho.client.mqttv3.MqttMessage
import alice.tuprolog.Prolog
import it.unibo.kactor.ApplMessage
import it.unibo.kactor.ActorBasic
import kotlinx.coroutines.delay
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch



object kbMqtt{
	
	fun initKB(mqttAddr:String){
		try{
			mqttC = MqttClient(mqttAddr, "kbMqtt")
			val options = MqttConnectOptions()
			options.setKeepAliveInterval(4800);
			options.setWill("unibo/clienterrors", "crashed".toByteArray(), 2, true);
			mqttC.connect(options);
			
			mqttC.subscribe("unibo/qak/robot/map", handleMap)
			mqttC.subscribe("unibo/qak/robot/state", handleRobotPosition)
			mqttC.subscribe("unibo/qak/waiter/state", handleTearoomState)

		}catch(e:Exception){System.out.println(e)}
	 }
	
	 private lateinit var mqttC : MqttClient
	
     val engine = Prolog();
	
	 var htmlBaseMap = "<table/>"
	 var htmlMap = "<table/>"
	
	 var robotX = ""
	 var robotY = ""
	 var robotDir = ""
	
	 var waiterState = ""
	 var tables = HashMap<Int,Pair<String,String>>()
	 var clients = HashMap<Int,String>()
	
	val handleMap =
		{ topic: String, message: MqttMessage ->
			try{
//			mqttC.publish("unibo/qak/web/test","handleMap".toByteArray(),2,false)
//			System.out.println("handleMap")
//			System.out.println("----------------------$topic")
//			System.out.println("----------------------${message.toString()}")
			
			htmlBaseMap = message.toString()
			refreshView()
			}catch(e:Exception){System.out.println(e)}
		}
	
	//robot(0,0,DOWN)
	val handleRobotPosition =
		{ topic: String, message: MqttMessage ->
			try{
//			mqttC.publish("unibo/qak/web/test","handleRobotPosition".toByteArray(),2,false)
//			System.out.println("handleRobotPosition")
//			System.out.println("----------------------$topic")
//			System.out.println("----------------------${message.toString()}")
			
		  val robotState = message.toString()
			  				.replace("robot(","")
			  				.replace(")","")
			  				.split(",")
			robotX = robotState.get(0)
			robotY = robotState.get(1)
			robotDir = robotState.get(2)
//			System.out.println(robotState)
			refreshView()
		}catch(e:Exception){System.out.println(e)}
		}
	
	//state(waiter(rest(0)),tables([teatable(1,free,clean),teatable(2,free,clean)]),clients([]))
	//state(waiter(rest(0)),tables([teatable(1,busy(1),clean),teatable(2,free,clean)]),clients([]))
	val handleTearoomState =
		{ topic: String, message: MqttMessage ->
			try{
//			System.out.println("handleTearoomState")
//			mqttC.publish("test","handleTearoomState".toByteArray(),2,false)		
				
			engine.clearTheory();
			engine.solve("assert(${message.toString()}).")				
			val sol = engine.solve("state(waiter(W),tables(List),clients(C)).");

			waiterState = sol.getVarValue("W").toString()
			
			val tableList = sol.getVarValue("List")
								.toString()				//[teatable(1,busy(1),clean),teatable(2,free,clean)]
								.replace("[", "")
								.replace("]", "")			//teatable(1,busy(1),clean),teatable(2,free,clean)
                                .replace(",teatable",";")	//teatable(1,busy(1),clean);(2,free,clean)
                                .replace("teatable","")		//(1,busy(1),clean);(2,free,clean)
                                .split(";")					//list[(1,busy(1),clean), (2,free,clean)]
			
			for(t:String in tableList){
		        val tableProperties = t.substring(1,t.length-1).split(",")
				val tNum = tableProperties.get(0)
				val tFree = tableProperties.get(1)
				val tClean = tableProperties.get(2)
				tables.put(tNum.toInt(),Pair<String,String>(tFree,tClean))
		    }
			
			val clientList = sol.getVarValue("C")
								.toString()
								.replace("[", "")
								.replace("]", "")
								.replace("),", ";")
								.replace("(", " ")
								.replace(")", "")
								.replace("client ", "")
								.split(";")
			
			if( clientList.toString() != "[]"){
				for(c:String in clientList){
			        val cProperties = c.split(",")
					val cNum = cProperties.get(0)
					val cState = cProperties.get(1)
					clients.put(cNum.toInt(),cState)
			    }
			}
			
			refreshView()
			
			}catch(e :Exception){
				System.out.println(e)
			}
		}
	

	
	
	
	private fun refreshView(){
		try{	
				htmlMap = htmlBaseMap.replace("<td class=\"obstacle\"/><td class=\"obstacle\"/>",
									"<td class=\"wall\"/><td class=\"wall\"/>")
				
				htmlMap = htmlMap.replace("<td class=\"obstacle\"/></tr>",
									"<td class=\"wall\"/></tr>")
				
				htmlMap = htmlMap.replace("<td class=\"obstacle\"/><td class=\"free\"/><td class=\"obstacle\"/>",
									"<td class=\"table1\"/><td class=\"free\"/><td class=\"table2\"/>")
					
	
				
				for(tableNum :Int in tables.keys){
					if(tables.get(tableNum)!!.first.contains("reserve"))
						htmlMap = htmlMap.replace("table$tableNum","tablereserved")
					else{
						when(tables.get(tableNum)!!.second){
							"clean" -> htmlMap = htmlMap.replace("table$tableNum","tableclean")
							"dirty" -> htmlMap = htmlMap.replace("table$tableNum","tabledirty")
							"cleanedA" -> htmlMap = htmlMap.replace("table$tableNum","tablea")
							"cleanedB" -> htmlMap = htmlMap.replace("table$tableNum","tableb")
						}
					}
				}
				GlobalScope.launch{
					if(mqttC.isConnected())
						mqttC.publish("unibo/qak/kb/web/map",htmlMap.toByteArray(),0,false)		
				}
			}
		catch(e:Exception)
		{
			System.out.println(e.toString())
		}
	}

}