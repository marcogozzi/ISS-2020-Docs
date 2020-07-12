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

class testStopCleaning {

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

		testStopCleaning()

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
	fun testStopCleaning(){
		println(" --- testStopCleaning ---")
		runBlocking{
			
			//start from two occupied tables
			
			waiter.pengine
					.solve(" retract( teatable( 1, _, _) ). ")
			waiter.pengine
					.solve(" assert( teatable( 1, busy(8), dirty) ). ")
			waiter.pengine
					.solve(" assert( client(8, consuming) ). ")
			
			MsgUtil.sendMsg(
 				MsgUtil.buildDispatch("test","payment", "payment(8)" ,"waiter"),
				waiter)
			
			while( ! waiter.geResourceRep().contains("gocleanteatable"))
				delay(100)
			
			// a new client asks to enter the tearoom
			// there is a clean teatable available
			// so the waiter must stop the clean task
			// and take the client to the clean teatable
			MsgUtil.sendMsg(
 				MsgUtil.buildRequest("test","notify", "notify(36)", "smartbell"),
				smartbell)
			
			// give the waiter some time to react to the notify
			while( waiter.geResourceRep().contains("gocleanteatable"))
				delay(50)
			
			assertTrue(waiter.geResourceRep().contains("waiter(entrancedoor")
					.or(waiter.geResourceRep().contains("waiter(teatable")))
			
			println(" --- testStopCleaning ---")	
		}
	}
	/*
	state(waiter(athome),tables([teatable(1,free,clean),teatable(2,free,clean)]),clients([]))
	state(waiter(teatable(1)),tables([teatable(1,busy(8),dirty),teatable(2,free,clean)]),clients([client(8,consuming)]))
	msg(paymentOk,event,waiter,none,paymentOk(8),19)
	msg(exitOk,event,waiter,none,exitOk(8),20)
	state(waiter(exitdoor),tables([teatable(1,free,dirty),teatable(2,free,clean)]),clients([client(8,left)]))
	state(waiter(gocleanteatable(1)),tables([teatable(1,free,dirty),teatable(2,free,clean)]),clients([client(8,left)]))
	msg(table,request,smartbell,waiter,table(1),26)
	state(waiter(gocleanteatable(1)),tables([teatable(1,free,dirty),teatable(2,reserved(1),clean)]),clients([client(8,left),client(1,accepted)]))
	msg(available,reply,waiter,smartbell,available(1),28)
	state(waiter(gocleanteatable(1)),tables([teatable(1,free,dirty),teatable(2,reserved(1),clean)]),clients([client(8,left),client(1,accepted)]))
	msg(accept,reply,smartbell,test,accept(1),31)
	state(waiter(entrancedoor),tables([teatable(1,free,dirty),teatable(2,reserved(1),clean)]),clients([client(8,left),client(1,accepted)]))
	 */
	
	
}
