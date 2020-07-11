package connQak

import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.coap.MediaTypeRegistry
import it.unibo.kactor.MsgUtil
import it.unibo.kactor.ApplMessage
import org.eclipse.californium.core.CoapResponse
import java.io.File
import org.json.JSONObject
import java.net.InetAddress
 

class connQakCoap( )  {
	var client   : CoapClient = CoapClient(  )
	
	/*
	@Deprecated("please use parameter")
	 fun createConnection(  ){
 			val url = "coap://${configurator.hostAddr}:${configurator.port}/${configurator.ctxqadest}/${configurator.qakdest}"
 			System.out.println("connQakCoap | url=${url.toString()}")
 			//uriStr: coap://192.168.1.22:8060/ctxdomains/waiter
			//client = CoapClient(  )
		    client.uri = url.toString()
			client.setTimeout( 1000L )
 			//initialCmd: to make console more reactive at the first user cmd
 		    val respGet  = client.get( ) //CoapResponse
			if( respGet != null )
				System.out.println("connQakCoap | createConnection doing  get | CODE=  ${respGet.code} content=${respGet.getResponseText()}")
			else
				System.out.println("connQakCoap | url=  ${url} FAILURE")
	}*/
	
	fun createConnection(configFile: String){
		try{
				val configfile =   File(configFile)
				val config     =   configfile.readText()	//charset: Charset = Charsets.UTF_8
				val jsonObject	=  JSONObject( config )			
				val pageTemplate 	=  jsonObject.getString("page") 
				val hostAddr    	=  jsonObject.getString("host") 
				val port    		=  jsonObject.getString("port")
				val qakdest         =  jsonObject.getString("qakdest")
				val ctxqadest		=  jsonObject.getString("ctxqadest")
			
				//uriStr: coap://192.168.1.22:8060/ctxdomains/waiter
				val url = "coap://$hostAddr:$port/$ctxqadest/$qakdest"
	 			
			    client.uri = url.toString()
				client.setTimeout( 1000L )
	 		    val respGet  = client.get( ) //CoapResponse
				if( respGet != null )
					System.out.println("connQakCoap | createConnection doing  get | CODE=  ${respGet.code} content=${respGet.getResponseText()}")
				else
					System.out.println("connQakCoap | url=  ${url} FAILURE")
			
				}
		catch(e:Exception){
			System.out.println( "SORRY $configFile NOT FOUND ")
		}
	}
	
	 fun forward( msg: ApplMessage ){		
        System.out.println("connQakCoap | PUT forward ${msg}  ")		
        val respPut = client.put(msg.toString(), MediaTypeRegistry.TEXT_PLAIN)
        System.out.println("connQakCoap | RESPONSE CODE=  ${respPut.code}")		
	}
	
	 fun request( msg: ApplMessage ){
 		val respPut = client.put(msg.toString(), MediaTypeRegistry.TEXT_PLAIN)
  		if( respPut != null ) System.out.println("connQakCoap | answer= ${respPut.getResponseText()}")		
		
	}
	
	 fun emit( msg: ApplMessage){
//		val url = "coap://$hostIP:$port/ctx$destName"		//TODO
//		client = CoapClient( url )
        //println("PUT emit url=${url} ")
		 //System.out.println("Debug: emit " + msg)	
         val respPut = client.put(msg.toString(), MediaTypeRegistry.TEXT_PLAIN)

         //System.out.println("connQakCoap | PUT emit ${msg} RESPONSE CODE=  ${respPut.code}")		
		
	}
	
	 fun readRep(   ) : String{
		val respGet : CoapResponse = client.get( )
		return respGet.getResponseText()
	}
}