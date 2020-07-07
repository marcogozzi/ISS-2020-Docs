package it.unibo.robotWeb2020;
//https://www.baeldung.com/websockets-spring
//https://www.toptal.com/java/stomp-spring-boot-websocket

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import alice.tuprolog.*;
import connQak.connQakCoap;
import it.unibo.kactor.ApplMessage;
import it.unibo.kactor.MsgUtil;


@Controller 
public class RobotController { 
    String appName     ="robotGui";
    String viewModelRep="startup";
    String robotHost = ""; //ConnConfig.hostAddr;		
    String robotPort = ""; //ConnConfig.port;
    Prolog engine = new Prolog();
    String htmlPage  = "robotGuiSocket";
    int updateNum = 0;
    Boolean automatic = true;
    Map<String,String> clientStates = new HashMap<String,String>();

    connQakCoap connQakSupport;
    connQakCoap connClient;
    
    /*
     * Update the page vie socket.io when the application-resource changes.
     * Thanks to Eugenio Cerulo
     */
    	@Autowired
    	SimpMessagingTemplate simpMessagingTemplate;
    
    public RobotController() {    
        connQakSupport = new connQakCoap();  
        connQakSupport.createConnection("pageConfig.json");

        connClient = new connQakCoap() ; 
        connClient.createConnection("clientConfig.json");
          
        }

   

  @GetMapping("/") 		 
  public String entry(Model viewmodel) {
 	 viewmodel.addAttribute("arg", "Entry page loaded. Please use the buttons ");
 	 peparePageUpdating();
 	 return htmlPage;
  } 
   
  @GetMapping("/applmodel")
  @ResponseBody
  public String getApplicationModel(Model viewmodel) {
  	  ResourceRep rep = getWebPageRep(connQakSupport);
	  return rep.getContent();
  }     
  
    
	@PostMapping( path = "/move" ) 
	public String doMove( 
		@RequestParam(name="move", required=false, defaultValue="h") 
		//binds the value of the query string parameter name into the moveName parameter of the  method
		String moveName, Model viewmodel) {
		//System.out.println("------------------- RobotController doMove move=" + moveName  );
		//if( robotMoves.contains(moveName) ) {
			doBusinessJob(moveName, viewmodel);
 		//}else {
			viewmodel.addAttribute("arg", "Sorry: move unknown - Current Robot State:"+viewModelRep );
		//}		
		return htmlPage;
		//return "robotGuiSocket";  //ESPERIMENTO
	}	
	
	
	private void peparePageUpdating() {

		try {
	        File configfile = new File("mqttConfig.json");
	        FileInputStream fis = new FileInputStream(configfile);
	        byte[] data = new byte[(int) configfile.length()];
	        fis.read(data);
	        fis.close();
	        
	        String config = new String(data, "UTF-8");
	        JSONObject jsonObject	= new JSONObject( config )	;		
			String mqttBrokerHost 	=  jsonObject.getString("brokerhost");
			String maptopic 	=  jsonObject.getString("maptopic");
			String controltopic 	=  jsonObject.getString("controltopic");
			String statetopic 	=  jsonObject.getString("statetopic");
			
			MqttClient mqtt = new MqttClient(mqttBrokerHost, "webserver");
			
			MqttConnectOptions options = new MqttConnectOptions();
			options.setKeepAliveInterval(4800);
			options.setWill("unibo/clienterrors", "crashed".getBytes(), 2, true);
			mqtt.connect(options);
					
			System.out.println("\t&&&\t"+mqttBrokerHost+"\t&&&");//debug
			mqtt.subscribe(maptopic, new IMqttMessageListener() {	
				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					String ogMsg = new String(message.getPayload());
					simpMessagingTemplate.convertAndSend(WebSocketConfig.topicForMap, 
							new ResourceRep(ogMsg));	
				}
			});
			mqtt.subscribe(controltopic, new IMqttMessageListener() {
				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					String ogMsg = new String(message.getPayload());
					simpMessagingTemplate.convertAndSend(WebSocketConfig.topicForControl, 
							new ResourceRep(ogMsg));	
				}
			});
			//		unibo/qak/+/state
			mqtt.subscribe(statetopic, new IMqttMessageListener() {
				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					if(topic.matches(".*\\/.*\\/.*\\/state")) {
						String clientAct = topic.split("/")[2];
						if(clientAct.contains("client") ||
								clientAct.contains("bar")) {
							String ogMsg = new String(message.getPayload());
							clientStates.put(clientAct, ogMsg);
							
							StringBuilder sb = new StringBuilder();
							for(String k : clientStates.keySet()) {
								sb.append(k+": "+clientStates.get(k)+"\n");
							}
							simpMessagingTemplate.convertAndSend(WebSocketConfig.topicForClient, 
									new ResourceRep(sb.toString()));
						}
					}
				}
			});
			System.out.println("\t&&&\trobot controller correctly init\t&&&");//debug
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     
		
    	connQakSupport.getClient().observe(new CoapHandler() {
			@Override
			public void onLoad(CoapResponse response) {
				StringBuilder waiter = new StringBuilder();
				StringBuilder teatable1 = new StringBuilder();
				StringBuilder teatable2 = new StringBuilder();
				//System.out.println("RobotController --> CoapClient changed "+ updateNum++ +" ->" + response.getResponseText());
				try {
					engine.clearTheory();
					
					//OLD
					//state(waiter(rest,0,0),tables([teatable(1,free,clean),teatable(2,free,clean)])) 
					//NEW
					//state(waiter(rest,0,0),tables([teatable(1,free,clean),teatable(2,free,clean)]),clients([]))
					SolveInfo sol;
					String clientString = "";
					
					
					if(response.getResponseText().contains("clients(")) {
						engine.solve("assert("+response.getResponseText()+").");
						sol = engine.solve("state(waiter(W,WX,WY),tables([teatable(1,TOF,TOC),teatable(2,TTF,TTC)]),clients(C)).");
						waiter.append("waiter in state ")
							.append(sol.getVarValue("W"));
					
						teatable1.append("teatable 1 is ")
							.append(sol.getVarValue("TOF"))
							.append(" and ")
							.append(sol.getVarValue("TOC")); 
			
						teatable2.append("teatable 2 is ")
							.append(sol.getVarValue("TTF"))
							.append(" and ")
							.append(sol.getVarValue("TTC"));
						
						clientString = sol.getVarValue("C").toString()
								.replace("[", "\n")
								.replace("]", "\n")
								.replace("),", "\n")
								.replace("(", " ")
								.replace(",", " is ")
								.replace(")", "\n");
						
						simpMessagingTemplate.convertAndSend(WebSocketConfig.topicForRoom, 
								new ResourceRep("" + HtmlUtils.htmlEscape(
										waiter.toString()+"\n"+
										teatable1.toString()+"\n"+
										teatable2.toString()+"\n"+
										clientString)  ));
					}
					
					else if(response.getResponseText().contains("state(")) {
						engine.solve("assert("+response.getResponseText()+").");
						sol = engine.solve("state(waiter(W,WX,WY),tables([teatable(1,TOF,TOC),teatable(2,TTF,TTC)]))."); 
						waiter.append("waiter in state ")
						.append(sol.getVarValue("W"))
						.append(" at position(")
						.append(sol.getVarValue("WX"))
						.append(",")
						.append(sol.getVarValue("WY"))
						.append(")");
				
						teatable1.append("teatable 1 is ")
						.append(sol.getVarValue("TOF"))
						.append(" and ")
						.append(sol.getVarValue("TOC")); 
		
						teatable2.append("teatable 2 is ")
						.append(sol.getVarValue("TTF"))
						.append(" and ")
						.append(sol.getVarValue("TTC"));
						
						simpMessagingTemplate.convertAndSend(WebSocketConfig.topicForRoom, 
								new ResourceRep("" + HtmlUtils.htmlEscape(
										"CoAP update number: " + updateNum + "\n"+
										waiter.toString()+"\n"+
										teatable1.toString()+"\n"+
										teatable2.toString()+"\n")  ));
					}
						
						
					else
						simpMessagingTemplate.convertAndSend(WebSocketConfig.topicForRoom, 
							new ResourceRep("" + HtmlUtils.htmlEscape(response.getResponseText())));
					
					
					
					
				} catch (PrologException e) {
					System.out.println("error during prolog unify");
					e.printStackTrace();
				}
				
			}

			@Override
			public void onError() {
				System.out.println("RobotController --> CoapClient error!");
			}
		});
    	
    	connClient.getClient().observe(new CoapHandler() {
			@Override
			public void onLoad(CoapResponse response) {
				System.out.println("New Client is " + response.getResponseText());
				simpMessagingTemplate.convertAndSend(WebSocketConfig.topicForClient, 
						new ResourceRep("" + HtmlUtils.htmlEscape(response.getResponseText())));
			}

			@Override
			public void onError() {
				System.out.println("RobotController --> CoapClient error!");
			}
		
    	});
	}
	
	/*
	 * INTERACTION WITH THE BUSINESS LOGIC			
	 */
	protected void doBusinessJob( String moveName, Model viewmodel) {
		System.out.println("doBusinessJob " + moveName);
		try {
			if(moveName.equalsIgnoreCase("manual")) {
				automatic = ! automatic;
				connClient.getMqtt().publish("unibo/qak/robot", MsgUtil.buildDispatch("web", "manual", "manual(0)", "robot").toString(), 2, false);
			}
			else if ( ! automatic && moveName.equalsIgnoreCase("l")){	
				//System.out.println("doing l"); 
				connClient.getMqtt().publish("unibo/qak/basicrobot", MsgUtil.buildDispatch("web", "cmd", "cmd(l)", "basicrobot").toString(), 2, false);
			}
			else if ( ! automatic && moveName.equalsIgnoreCase("p")){
				//System.out.println("doing p"); 
				connClient.getMqtt().publish("unibo/qak/basicrobot", MsgUtil.buildRequest("web", "step", "step(200)", "basicrobot").toString(), 2, false);
			}
			else if ( ! automatic && moveName.equalsIgnoreCase("r")){	
				//System.out.println("doing r"); 
				connClient.getMqtt().publish("unibo/qak/basicrobot", MsgUtil.buildDispatch("web", "cmd", "cmd(r)", "basicrobot").toString(), 2, false);
			}
		}catch (Exception e) {
			// TODO: handle exception
		}		
					
				
	}

    @ExceptionHandler 
    public ResponseEntity<String> handle(Exception ex) {
    	HttpHeaders responseHeaders = new HttpHeaders();
        return new ResponseEntity<String>(
        		"RobotController ERROR " + ex.getMessage(), responseHeaders, HttpStatus.CREATED);
    }

/* ----------------------------------------------------------
   Message-handling Controller
  ----------------------------------------------------------
 */
//	@MessageMapping("/hello")
//	@SendTo("/topic/display")
//	public ResourceRep greeting(RequestMessageOnSock message) throws Exception {
//		Thread.sleep(1000); // simulated delay
//		return new ResourceRep("Hello by AN, " + HtmlUtils.htmlEscape(message.getName()) + "!");
//	}
	
    @MessageMapping("/move")
 	public void backtoclient(RequestMessageOnSock message) throws Exception {
		//System.out.println("backtoclient " + message.toString());
		doBusinessJob(message.getName(), null);
		//return getWebPageRep(connClient);
 	}
    
    @MessageMapping("/update")
    /* MessageMapping is the same as the function on button click in app.js
     * stompClient.send("/app/move", {}, JSON.stringify({'name': move }));
     * */
	@SendTo("/topic/roomstate")
    /*	SendTo is the same as app.js
        stompClient.subscribe('/topic/roomstate', function (msg) {
             showMsgRoom(JSON.parse(msg.body).content);
        }); 
     */
	public ResourceRep updateTheMap(@Payload String message) {
		System.out.println("updateTheMap");
		ResourceRep rep = getWebPageRep(connQakSupport);
		return rep;
	}

	@MessageMapping("/client")
	@SendTo("/topic/clientstate")
	public ResourceRep updateClientResEmit(@Payload String message) {
		//System.out.println("emitting");
 		ApplMessage msg = MsgUtil.buildEvent("web", "goOn", "goOn(0)");
		connClient.emit( msg );
		return updateClientRes(message);
	}

	@MessageMapping("/clientupdate")
	@SendTo("/topic/clientstate")
	public ResourceRep updateClientRes(@Payload String message) {
		//System.out.println("updateClientRes");
		ResourceRep rep = getWebPageRep(connClient);
		return rep;
	}

	

	public ResourceRep getWebPageRep(connQakCoap connection)   {
		String resourceRep = connection.readRep();
		//System.out.println("------------------- RobotController resourceRep=" + resourceRep  );
		return new ResourceRep("" + HtmlUtils.htmlEscape(resourceRep)  );
	}
	
	
	
	
 
/*
 * The @MessageMapping annotation ensures that, 
 * if a message is sent to the /hello destination, the greeting() method is called.    
 * The payload of the message is bound to a HelloMessage object, which is passed into greeting().
 * 
 * Internally, the implementation of the method simulates a processing delay by causing 
 * the thread to sleep for one second. 
 * This is to demonstrate that, after the client sends a message, 
 * the server can take as long as it needs to asynchronously process the message. 
 * The client can continue with whatever work it needs to do without waiting for the response.
 * 
 * After the one-second delay, the greeting() method creates a Greeting object and returns it. 
 * The return value is broadcast to all subscribers of /topic/display, 
 * as specified in the @SendTo annotation. 
 * Note that the name from the input message is sanitized, since, in this case, 
 * it will be echoed back and re-rendered in the browser DOM on the client side.
 */
    
 
/*
 * curl --location --request POST 'http://localhost:8080/move' --header 'Content-Type: text/plain' --form 'move=l'	
 * curl -d move=r localhost:8080/move
 */
}

