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

class testMaxStayTime {

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

		testMaxStayTime()

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
	fun testMaxStayTime(){
		println(" --- testMaxStayTime ---")
		runBlocking{
			val MaxStayTime = waiter.pengine.solve("maxStayTime(X).")
					.getVarValue("X")
					.toString()
					.toLong()

			println(MaxStayTime)

			MsgUtil.sendMsg(
					MsgUtil.buildRequest("test","notify", "notify(36)", "smartbell"),
					smartbell)

			while( ! waiter.pengine.solve("client(1, choosing).").isSuccess())
				delay(50)

			MsgUtil.sendMsg(
					MsgUtil.buildDispatch("test","placeorder", "placeorder(1)" ,"waiter"),
					waiter)

			while( ! waiter.pengine.solve("client(1, consuming).").isSuccess())
				delay(50)
			
			assertTrue(waiter.pengine.solve("client(1, consuming).").isSuccess())
			
			delay(MaxStayTime * 3 / 2) // wait some more time
			
			assertTrue(waiter.pengine.solve("client(1, paying).").isSuccess()
						.or(waiter.pengine.solve("client(1, leaving).").isSuccess())
						.or(waiter.pengine.solve("client(1, left).").isSuccess()))
			
			println(" --- testMaxStayTime ---")	
		}
	}

}
