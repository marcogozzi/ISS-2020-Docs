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

class testTearoomOnhold {
		
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
	
		testOnHold()
		
	    testOnHoldEnters()	
		
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
	fun testOnHold(){
		println(" --- testOnHold ---")
 		runBlocking{
			while(it.unibo.kactor.sysUtil.getAllActorNames().size == 0)
				delay(100)
			
			//start from two occupied tables
			waiter.pengine
					.solve(" retract( teatable( _, _, _) ). ")
			waiter.pengine
					.solve(" retract( teatable( _, _, _) ). ")
			waiter.pengine
					.solve(" assert( teatable( 1, busy(9), dirty) ). ")
			waiter.pengine
					.solve(" assert( teatable( 2, busy(8), dirty) ). ")
			waiter.pengine
					.solve(" assert( client(9, consuming) ). ")
			waiter.pengine
					.solve(" assert( client(8, consuming) ). ")
			
 			MsgUtil.sendMsg(
 				MsgUtil.buildRequest("test","notify", "notify(36)", "smartbell"),
				smartbell)
			
			delay(1000)
			
			//no table available, client waits at entrancedoor
			assertTrue(waiter.pengine
  					.solve(" client(_, onhold). ")
					.isSuccess())
		}
}
	
	
@kotlinx.coroutines.ObsoleteCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
	fun testOnHoldEnters(){
		println(" --- testOnHoldEnters ---")
		runBlocking{
			
			// 2 teatables are occupied
			// client at table2 requests payment, pays, leaves
			MsgUtil.sendMsg(
 				MsgUtil.buildDispatch("test","payment", "payment(8)" ,"waiter"),
				waiter)
			
			//waiter goes to table, gets payment, takes client to exit, cleans table, takes client onhold to table 2
			while(! waiter.pengine
  					.solve("client(1,choosing).")
					.isSuccess())
			delay(10)
			
			assertTrue(waiter.pengine
  					.solve(" client(1, choosing). ")
					.isSuccess())
			assertTrue(waiter.pengine
  					.solve(" teatable(2,busy(1),dirty). ")
					.isSuccess())
//			println("--------"+waiter.pengine.solve("roomstate(X).").getSolution())
			
		println(" --- testOnHoldEnters ends---")
		}
}
}