package test

import org.junit.Before
import org.junit.After
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import it.unibo.kactor.ActorBasic
import it.unibo.kactor.MqttUtils
import it.unibo.kactor.MsgUtil

class testTearoomRA {
		
	lateinit var waiter    : ActorBasic
	lateinit var smartbell    : ActorBasic
	val mqttTest   	      = MqttUtils("test")
	val initDelayTime     = 4000L   // 
	val useMqttInTest 	  = true
	val mqttbrokerAddr 	  = "localhost"

	fun println(v: Any?){
		System.out.println(v)
	}
	@kotlinx.coroutines.ObsoleteCoroutinesApi
	@kotlinx.coroutines.ExperimentalCoroutinesApi
	@Before
	fun systemSetUp() {
		kotlin.concurrent.thread(start = true) {	 
			it.unibo.ctxwaiter.main()
				if( useMqttInTest ){
					while( ! mqttTest.connectDone() ){
						println( "	attempting MQTT-conn to $mqttbrokerAddr for the test unit ... " )
						Thread.sleep(1000)
						mqttTest.connect("test", mqttbrokerAddr )					 
					}
				}	
		}
		Thread.sleep(3000)
		waiter = it.unibo.kactor.sysUtil.getActor("waiter")!!
		smartbell = it.unibo.kactor.sysUtil.getActor("smartbell")!! 
	}
			
@kotlinx.coroutines.ObsoleteCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
	@Test
	fun testwaiter(){
	
		testNotify()
		
	    testExit()	
		
		testClean()
		
		testRest()
	
		println("test waiter BYE")
	}
	
@kotlinx.coroutines.ObsoleteCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
	@After
	fun terminate() {
		println("testTearoom terminated ")
	}
	
@kotlinx.coroutines.ObsoleteCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
	fun testNotify(){
		println(" --- testNotify ---")
 		runBlocking{
			while(it.unibo.kactor.sysUtil.getAllActorNames().size == 0)
				delay(100)
			assertTrue(waiter.pengine
					.solve("hall(free).")
					.isSuccess())
			
 			MsgUtil.sendMsg(
 				MsgUtil.buildRequest("test","notify", "notify(36)", "smartbell"),
				smartbell)
			
			//notify
 			while(waiter.pengine
					.solve("hall(free).")
					.isSuccess())//the smartbell has not received the request
				delay(50)

			assertTrue(waiter.pengine
					.solve("hall(busy).")//the smartbell has received the request but the waiter has not processed it
					.isSuccess())
			//delay(100)
			
			//accept
			assertTrue(waiter.pengine
					.solve("teatable(_, reserved(_), _).")//the waiter has processed the request but the client is not sitting at the table
					.isSuccess()
			.xor(waiter.pengine
  					.solve("teatable(_, busy(_), _).")//the waiter has processed the request and the client is sitting at the table
					.isSuccess()))
			
			while(waiter.pengine
					.solve("teatable(_, reserved(_), _).")
					.isSuccess())
				delay(50)
			
			//reach
  			assertTrue(waiter.pengine
  					.solve("teatable(_, busy(_), _).")//the waiter has processed the request and the client is sitting at the table
					.isSuccess())
			
			delay(2999)
			println(waiter.geResourceRep())
					
 			
		}
}
	
	
@kotlinx.coroutines.ObsoleteCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
	fun testExit(){
		println(" --- testExit ---")
		runBlocking{

			val resBeforeExit = waiter.pengine
  					.solve("findall(N1, teatable(N1, busy(_), dirty), L1), length(L1,Len1), Len1 > 0, findall(N2, teatable(N2, free, dirty), L2), length(L2,Len2).")

			
			println("------------------------------------------------------------------------------------------------------------------------")
			println(resBeforeExit)
			println("------------------------------------------------------------------------------------------------------------------------")
			
			
			MsgUtil.sendMsg(
 				MsgUtil.buildDispatch("test","placeorder", "placeorder(1)" ,"waiter"),
				waiter)
					
			//new in sprint2: ASSUMPTION all clients perform all actions in order (i.e. wait for order to be delivered before asking payment)
			while(! waiter.pengine
  					.solve("client(_, consuming).")
					.isSuccess())
			delay(49)
				
 			MsgUtil.sendMsg(
 				MsgUtil.buildDispatch("test","payment", "payment(1)" ,"waiter"),
				waiter)
			
 			while(! waiter.pengine
  					.solve("waiter(exitdoor).")
					.isSuccess())
			delay(49)
			 
			println("--------"+waiter.pengine.solve("roomstate(X).").getSolution())

			
			//ends with waiter at the exitdoor
			val resAfterExit = waiter.pengine
  					.solve("findall(N3, teatable(N3, busy(_), dirty), L3), length(L3,Len3), findall(N4, teatable(N4, free, dirty), L4), length(L4,Len4), Len4 > 0.")
			
			assertTrue(resBeforeExit.getVarValue( "Len1" ).toString().toInt() - 1 == resAfterExit.getVarValue( "Len3" ).toString().toInt())
			assertTrue(resBeforeExit.getVarValue( "Len2" ).toString().toInt() + 1 == resAfterExit.getVarValue( "Len4" ).toString().toInt())
		println(" --- testExit ends---")
		}
}
	
@kotlinx.coroutines.ObsoleteCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
	fun testClean(){
		println(" --- testClean ---")
		runBlocking{
					
			val resBeforeClean = waiter.pengine
  					.solve("findall(N1, teatable(N1, free, dirty), L1), length(L1,Len1), Len1 > 0, findall(N2, teatable(N2, free, clean), L2), length(L2,Len2).")
			println("--------"+waiter.pengine.solve("roomstate(X).").getSolution())
			println("------------------------------------------------------------------------------------------------------------------------")
			println(resBeforeClean)
			println("------------------------------------------------------------------------------------------------------------------------")

			while( ! waiter.geResourceRep().contains("dirty"))
				delay(100)
			while( ! waiter.geResourceRep().contains("cleanedA"))
				delay(100)
			while( ! waiter.geResourceRep().contains("cleanedB"))
				delay(100)
			while( waiter.geResourceRep().contains("cleanedB"))
				delay(100)
			
			val resAfterClean = waiter.pengine
  					.solve("findall(N3, teatable(N3, free, dirty), L3), length(L3,Len3), findall(N4, teatable(N4, free, clean), L4), length(L4,Len4), Len4 > 0.")
			println("--------"+waiter.pengine.solve("roomstate(X).").getSolution())
			println("------------------------------------------------------------------------------------------------------------------------")
			println(resAfterClean)
			println("------------------------------------------------------------------------------------------------------------------------")
			
			assertTrue(resBeforeClean.getVarValue( "Len1" ).toString().toInt() - 1 == resAfterClean.getVarValue( "Len3" ).toString().toInt())
			assertTrue(resBeforeClean.getVarValue( "Len2" ).toString().toInt() + 1 == resAfterClean.getVarValue( "Len4" ).toString().toInt())
		}
	}
	
	@kotlinx.coroutines.ObsoleteCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
	fun testRest(){
		println(" --- testRest ---")
		runBlocking{
						println("--------"+waiter.pengine.solve("roomstate(X).").getSolution())

			val resBefore = waiter.pengine
  					.solve("findall(N1, teatable(N1, free, dirty), L), length(L,Len1), Len1 == 0.")
						
			assertTrue(resBefore.isSuccess())
//			delay(6000)//some time to get home
			
			//new in sprint2: ASSUMPTION all clients perform all actions in order (i.e. wait for order to be delivered before asking payment)
			while(! waiter.pengine
  					.solve("waiter(athome).")
					.isSuccess())
			delay(49)
						println("--------"+waiter.pengine.solve("roomstate(X).").getSolution())

			val resAfter = waiter.pengine
  					.solve("waiter(athome).")
			
			assertTrue(resAfter.isSuccess())
		}
	}
	


}