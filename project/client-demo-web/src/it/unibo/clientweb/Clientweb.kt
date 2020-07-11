/* Generated by AN DISI Unibo */ 
package it.unibo.clientweb

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Clientweb ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "s0"
	}
	@kotlinx.coroutines.ObsoleteCoroutinesApi
	@kotlinx.coroutines.ExperimentalCoroutinesApi			
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		
				val Maxstaytime = 100000L
				val StartDelay = kotlin.random.Random.nextLong(0, Maxstaytime/2*3)
				var Cid = ""
				var WasItMe = false
				var Debug = false
				val WaitTime = 3000L
				val Name = name
		return { //this:ActionBasciFsm
				state("s0") { //this:State
					action { //it:State
						updateResourceRep( "initial web... click to notify smartbell"  
						)
						println("initial web")
					}
					 transition(edgeName="t00",targetState="notify",cond=whenEvent("goOn"))
				}	 
				state("notify") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("goOn(Actorname)"), Term.createTerm("goOn($Name)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 WasItMe = true  
								updateResourceRep( "notifying to smartbell"  
								)
								println("notifying to smartbell")
								request("notify", "notify(36)" ,"smartbell" )  
						}else{
							 WasItMe = false  
						}
					}
					 transition(edgeName="t01",targetState="entering",cond=whenReply("accept"))
					transition(edgeName="t02",targetState="evalStay",cond=whenReply("full"))
					transition(edgeName="t03",targetState="leave",cond=whenReply("deny"))
					transition(edgeName="t04",targetState="notify",cond=whenEventGuarded("goOn",{ ! WasItMe  
					}))
				}	 
				state("leave") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("exitOk(Cid)"), Term.createTerm("exitOk($Cid)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								updateResourceRep( "client $Cid has left the tearoom... click to restart"  
								)
								println("client $Cid has left the tearoom...")
								delay(5000) 
								 WasItMe = true  
						}else{
							 WasItMe = false  
						}
					}
					 transition(edgeName="t05",targetState="s0",cond=whenEventGuarded("goOn",{ WasItMe  
					}))
					transition(edgeName="t06",targetState="leave",cond=whenEventGuarded("exitOk",{ ! WasItMe  
					}))
				}	 
				state("entering") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("accept(CID)"), Term.createTerm("accept(CID)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 Cid = payloadArg(0)  
						}
						updateResourceRep( "client $Cid about to enter"  
						)
						println("client $Cid about to enter")
					}
					 transition(edgeName="t07",targetState="sitting",cond=whenEvent("attable"))
				}	 
				state("sitting") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("attable(Cid)"), Term.createTerm("attable($Cid)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								updateResourceRep( "client $Cid sitting... click to proceed"  
								)
								println("client $Cid sitting")
								 WasItMe = true  
								delay(WaitTime)
						}else{
							 WasItMe = false  
						}
					}
					 transition(edgeName="t08",targetState="ordering",cond=whenEventGuarded("goOn",{ WasItMe  
					}))
					transition(edgeName="t09",targetState="sitting",cond=whenEventGuarded("attable",{ ! WasItMe  
					}))
				}	 
				state("ordering") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("goOn(Actorname)"), Term.createTerm("goOn($Name)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 WasItMe = true  
								updateResourceRep( "client $Cid placing order"  
								)
								println("client $Cid placing order")
								 if(Debug) println("press button to place order")  
								 if(Debug) readLine()  
								forward("placeorder", "placeorder($Cid)" ,"waiter" ) 
						}else{
							 WasItMe = false  
						}
					}
					 transition(edgeName="t110",targetState="ordering",cond=whenEventGuarded("goOn",{ ! WasItMe  
					}))
					transition(edgeName="t111",targetState="consuming",cond=whenEventGuarded("delivered",{ WasItMe  
					}))
				}	 
				state("consuming") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("delivered($Cid)"), Term.createTerm("delivered($Cid)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								updateResourceRep( "client $Cid consuming order... press button to request payment"  
								)
								println("client $Cid consuming order")
								if(  Debug  
								 ){ if(Debug) println("press button to pay")  
								 if(Debug) readLine()  
								}
								 WasItMe = true  
						}else{
							 WasItMe = false  
						}
					}
					 transition(edgeName="t012",targetState="reqPayment",cond=whenEventGuarded("goOn",{ WasItMe  
					}))
					transition(edgeName="t013",targetState="consuming",cond=whenEventGuarded("delivered",{ ! WasItMe  
					}))
				}	 
				state("reqPayment") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("goOn(Actorname)"), Term.createTerm("goOn($Name)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 WasItMe = true  
								updateResourceRep( "client $Cid requested payment"  
								)
								println("client $Cid requested payment")
								forward("payment", "payment($Cid)" ,"waiter" ) 
						}else{
							 WasItMe = false  
						}
					}
					 transition(edgeName="t114",targetState="reqPayment",cond=whenEventGuarded("goOn",{ ! WasItMe  
					}))
					transition(edgeName="t115",targetState="leaving",cond=whenEventGuarded("paymentOk",{ WasItMe  
					}))
				}	 
				state("leaving") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("paymentOk(Cid)"), Term.createTerm("paymentOk($Cid)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								updateResourceRep( "client $Cid is about to leave"  
								)
								println("client $Cid leaving")
								 WasItMe = true  
								delay(WaitTime)
						}else{
							 WasItMe = false  
						}
					}
					 transition(edgeName="t116",targetState="leaving",cond=whenEventGuarded("paymentOk",{ ! WasItMe  
					}))
					transition(edgeName="t117",targetState="leave",cond=whenEventGuarded("exitOk",{ WasItMe  
					}))
				}	 
				state("evalStay") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("full(CID,WTIME)"), Term.createTerm("full(Cid,WTIME)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 Cid = payloadArg(0)  
								updateResourceRep( "client $Cid is waiting for a table"  
								)
								println("client $Cid is waiting for a table")
						}
					}
					 transition(edgeName="t018",targetState="sitting",cond=whenEvent("attable"))
				}	 
			}
		}
}
