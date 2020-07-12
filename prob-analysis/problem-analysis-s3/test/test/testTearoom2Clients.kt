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

class testTearoom2Clients {
		
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
//			it.unibo.kactor.sysUtil.getActor("waiter")?.terminateCtx(0)
//			it.unibo.kactor.sysUtil.getActor("smartbell")?.terminateCtx(0)
//			Thread.sleep(3000)
			while(it.unibo.kactor.sysUtil.getActor("waiter") != null )
				Thread.sleep(1000)
			it.unibo.ctxwaiter.main()
			if( useMqttInTest ){
				while( ! mqttTest.connectDone() ){
					println( "	attempting MQTT-conn to $mqttbrokerAddr for the test unit ... " )
					Thread.sleep(1000)
					mqttTest.connect("test", mqttbrokerAddr )					 
				}
			}	
		}
		Thread.sleep(8000)
		waiter = it.unibo.kactor.sysUtil.getActor("waiter")!!
		smartbell = it.unibo.kactor.sysUtil.getActor("smartbell")!! 
	}
			
@kotlinx.coroutines.ObsoleteCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
	@Test
	fun testwaiter(){
	
		testEnter()
		
	    testInterleaving()	
		
		println("test waiter BYE")
	}
	
@kotlinx.coroutines.ObsoleteCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
	@After
	fun terminate() {
		waiter.terminateCtx(0)
		smartbell.terminateCtx(0)
		println("testTearoom terminated ")
	}
	
@kotlinx.coroutines.ObsoleteCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
	fun testEnter(){
		println(" --- testEnter ---")
 		runBlocking{
			while(it.unibo.kactor.sysUtil.getAllActorNames().size == 0)
				delay(100)
			
			//start from one occupied table
			waiter.pengine
					.solve(" retract( teatable( _, _, _) ). ")
			waiter.pengine
					.solve(" retract( teatable( _, _, _) ). ")
			waiter.pengine
					.solve(" assert( teatable( 1, busy(9), dirty) ). ")
			waiter.pengine
					.solve(" assert( teatable( 2, free, clean) ). ")
			waiter.pengine
					.solve(" assert( client(9, consuming) ). ")
			
 			MsgUtil.sendMsg(
 				MsgUtil.buildRequest("test","notify", "notify(36)", "smartbell"),
				smartbell)
			
			delay(5000)
//			println("--------"+waiter.pengine.solve("roomstate(X).").getSolution())
			//table 2 available, client is taken there
			assertTrue(waiter.pengine
  					.solve(" client(1, choosing). ")
					.isSuccess())
			assertTrue(waiter.pengine
  					.solve(" teatable( 2, busy(1), dirty). ")
					.isSuccess())
		}
}
	
	
@kotlinx.coroutines.ObsoleteCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
	fun testInterleaving(){
		println(" --- testInterleaving ---")
		runBlocking{
			
			// 2 teatables are occupied
			// client at table2 requests payment, pays, leaves
			MsgUtil.sendMsg(
 				MsgUtil.buildDispatch("test","payment", "payment(9)" ,"waiter"),
				waiter)
			
			delay(100)
			
			MsgUtil.sendMsg(
 				MsgUtil.buildDispatch("test","placeorder", "placeorder(1)" ,"waiter"),
				waiter)
			
			//waiter goes to table, gets payment, takes client to exit, cleans table, takes client onhold to table 2
			while(! waiter.pengine
  					.solve("client(9, left).")
					.isSuccess())
			delay(10)
			assertTrue(waiter.pengine
  					.solve(" teatable(1, free, dirty). ")
					.isSuccess())
			
			delay(5000)
			
			assertTrue(waiter.pengine
  					.solve(" client(1, consuming). ")
					.isSuccess())
			
//			println("--------"+waiter.pengine.solve("roomstate(X).").getSolution())
			
		println(" --- testInterleaving ends---")
		}
}

}