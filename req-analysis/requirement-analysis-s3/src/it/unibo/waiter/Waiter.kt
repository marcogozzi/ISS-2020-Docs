/* Generated by AN DISI Unibo */ 
package it.unibo.waiter

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Waiter ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "s0"
	}
	@kotlinx.coroutines.ObsoleteCoroutinesApi
	@kotlinx.coroutines.ExperimentalCoroutinesApi			
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		
				val MaxStayTime 	= 100000L	
				val DelayTime 		= 1000L
				val RestWaitTime 	= 10000L
				
		//		var TeatableFree	= true
				var IsWaiterAtHome  = true
				
				var StepsToTable = 4
				var Debug = false
		return { //this:ActionBasciFsm
				state("s0") { //this:State
					action { //it:State
						println("waiter RA-s3 initial")
						discardMessages = false
						solve("consult('tearoomKB-RA-sprint3.pl')","") //set resVar	
						solve("debug(true)","") //set resVar	
						if( currentSolution.isSuccess() ) { Debug = true  
						}
						else
						{}
						solve("roomstate(S)","") //set resVar	
						if( currentSolution.isSuccess() ) {updateResourceRep( getCurSol("S").toString()  
						)
						}
						else
						{}
					}
					 transition( edgeName="goto",targetState="waitState", cond=doswitch() )
				}	 
				state("waitState") { //this:State
					action { //it:State
						solve("roomstate(S)","") //set resVar	
						if( currentSolution.isSuccess() ) {updateResourceRep( getCurSol("S").toString()  
						)
						}
						else
						{}
						if(Debug) 
						println(getCurSol("S").toString())
						solve("waiter(athome)","") //set resVar	
						if( currentSolution.isSuccess() ) { IsWaiterAtHome = true  
						}
						else
						{ IsWaiterAtHome = false  
						}
						stateTimer = TimerActor("timer_waitState", 
							scope, context!!, "local_tout_waiter_waitState", RestWaitTime )
					}
					 transition(edgeName="t07",targetState="maybeRest",cond=whenTimeout("local_tout_waiter_waitState"))   
					transition(edgeName="t08",targetState="checkTableAvail",cond=whenRequest("table"))
					transition(edgeName="t09",targetState="reach",cond=whenDispatch("clientatentrance"))
					transition(edgeName="t010",targetState="takeOrder",cond=whenDispatch("placeorder"))
					transition(edgeName="t011",targetState="serveOrder",cond=whenReply("orderready"))
					transition(edgeName="t012",targetState="getPayment",cond=whenDispatch("payment"))
					transition(edgeName="t013",targetState="thinkCleanTable",cond=whenDispatch("cleantable"))
				}	 
				state("checkTableAvail") { //this:State
					action { //it:State
						if(Debug) 
						println("$name in ${currentState.stateName} | $currentMsg")
						solve("roomstate(S)","") //set resVar	
						if( currentSolution.isSuccess() ) {if(Debug) 
						println("DEBUG ---------- ${getCurSol("S").toString()} ------------") 
						}
						else
						{}
						if( checkMsgContent( Term.createTerm("table(CID)"), Term.createTerm("table(Cid)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 val Cid = payloadArg(0)  
								solve("occupyHall($Cid)","") //set resVar	
								solve("reserveTable(Num,$Cid)","") //set resVar	
								if( currentSolution.isSuccess() ) {if(Debug) 
								println("DEBUG ---------- ${getCurSol("Num").toString()} ------------") 
								solve("roomstate(S)","") //set resVar	
								if( currentSolution.isSuccess() ) {updateResourceRep( getCurSol("S").toString()  
								)
								if(Debug) 
								println("DEBUG ---------- ${getCurSol("S").toString()} ------------") 
								}
								else
								{}
								answer("table", "available", "available($Cid)"   )  
								if(Debug) 
								println("DEBUG ---------- sending clientatantrence($Cid)  ------------") 
								forward("clientatentrance", "clientatentrance($Cid)" ,"waiter" ) 
								}
								else
								{answer("table", "full", "full($Cid,$MaxStayTime)"   )  
								solve("assert(client($Cid,onhold))","") //set resVar	
								}
						}
						 if(Debug){ println("endof checkTableAvail"); readLine(); }  
					}
					 transition( edgeName="goto",targetState="waitState", cond=doswitch() )
				}	 
				state("reach") { //this:State
					action { //it:State
						if(Debug) 
						println("$name in ${currentState.stateName} | $currentMsg")
						if( checkMsgContent( Term.createTerm("clientatentrance(Cid)"), Term.createTerm("clientatentrance(Cid)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 val Cid = payloadArg(0)  
								delay(DelayTime)
								solve("freeHall($Cid)","") //set resVar	
								solve("updateWaiter(X,entrancedoor)","") //set resVar	
								solve("roomstate(S)","") //set resVar	
								if( currentSolution.isSuccess() ) {updateResourceRep( getCurSol("S").toString()  
								)
								}
								else
								{}
								solve("updateClient($Cid,entering)","") //set resVar	
								solve("teatable(Num,reserved($Cid),clean)","") //set resVar	
								 val TableNum = getCurSol("Num").toString()  
								delay(DelayTime)
								solve("updateWaiter(X,teatable($TableNum))","") //set resVar	
								solve("engageTable($TableNum,$Cid)","") //set resVar	
								emit("attable", "attable($Cid)" ) 
								solve("updateClient($Cid,choosing)","") //set resVar	
								solve("roomstate(S)","") //set resVar	
								if( currentSolution.isSuccess() ) {updateResourceRep( getCurSol("S").toString()  
								)
								}
								else
								{}
						}
						 if(Debug){ println("endof reach"); readLine(); }  
					}
					 transition( edgeName="goto",targetState="waitState", cond=doswitch() )
				}	 
				state("takeOrder") { //this:State
					action { //it:State
						if(Debug) 
						println("$name in ${currentState.stateName} | $currentMsg")
						if( checkMsgContent( Term.createTerm("placeorder(CID)"), Term.createTerm("placeorder(Cid)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 val Cid = payloadArg(0)  
								solve("updateClient($Cid,placeorder)","") //set resVar	
								solve("teatable(Num,busy($Cid),dirty)","") //set resVar	
								 val TableNum = getCurSol("Num").toString()  
								delay(DelayTime)
								solve("updateWaiter(X,teatable($TableNum))","") //set resVar	
								solve("roomstate(S)","") //set resVar	
								if( currentSolution.isSuccess() ) {updateResourceRep( getCurSol("S").toString()  
								)
								}
								else
								{}
								solve("updateClient($Cid,orderplaced)","") //set resVar	
								request("relayorder", "relayorder($Cid)" ,"barman" )  
						}
						 if(Debug){ println("endof takeOrder"); readLine(); }  
					}
					 transition( edgeName="goto",targetState="waitState", cond=doswitch() )
				}	 
				state("serveOrder") { //this:State
					action { //it:State
						if(Debug) 
						println("$name in ${currentState.stateName} | $currentMsg")
						if( checkMsgContent( Term.createTerm("orderready(Cid)"), Term.createTerm("orderready(Cid)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 val Cid = payloadArg(0)  
								solve("teatable(Num,busy($Cid),dirty)","") //set resVar	
								 val TableNum = getCurSol("Num").toString()  
								delay(DelayTime)
								solve("updateWaiter(X,bar)","") //set resVar	
								solve("roomstate(S)","") //set resVar	
								if( currentSolution.isSuccess() ) {updateResourceRep( getCurSol("S").toString()  
								)
								}
								else
								{}
								delay(DelayTime)
								solve("updateClient($Cid,consuming)","") //set resVar	
								solve("updateWaiter(X,teatable($TableNum))","") //set resVar	
								solve("roomstate(S)","") //set resVar	
								if( currentSolution.isSuccess() ) {updateResourceRep( getCurSol("S").toString()  
								)
								}
								else
								{}
								emit("delivered", "delivered($Cid)" ) 
						}
						 if(Debug){ println("endof serveOrder"); readLine(); }  
					}
					 transition( edgeName="goto",targetState="waitState", cond=doswitch() )
				}	 
				state("getPayment") { //this:State
					action { //it:State
						if(Debug) 
						println("$name in ${currentState.stateName} | $currentMsg")
						if( checkMsgContent( Term.createTerm("payment(Cid)"), Term.createTerm("payment(Cid)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 val Cid = payloadArg(0)  
								solve("teatable(Num,busy($Cid),dirty)","") //set resVar	
								 val TableNum = getCurSol("Num").toString()  
								delay(DelayTime)
								solve("updateWaiter(X,teatable($TableNum))","") //set resVar	
								solve("roomstate(S)","") //set resVar	
								if( currentSolution.isSuccess() ) {updateResourceRep( getCurSol("S").toString()  
								)
								}
								else
								{}
								solve("updateClient($Cid,paying)","") //set resVar	
								delay(2000) 
								solve("freeTable($TableNum,$Cid)","") //set resVar	
								emit("paymentOk", "paymentOk($Cid)" ) 
								solve("updateClient($Cid,leaving)","") //set resVar	
								delay(DelayTime)
								emit("exitOk", "exitOk($Cid)" ) 
								solve("updateWaiter(X,exitdoor)","") //set resVar	
								forward("cleantable", "cleantable($TableNum)" ,"waiter" ) 
								solve("updateClient($Cid,left)","") //set resVar	
								solve("roomstate(S)","") //set resVar	
								if( currentSolution.isSuccess() ) {updateResourceRep( getCurSol("S").toString()  
								)
								}
								else
								{}
						}
						 if(Debug){ println("endof getPayment"); readLine(); }  
					}
					 transition( edgeName="goto",targetState="waitState", cond=doswitch() )
				}	 
				state("thinkCleanTable") { //this:State
					action { //it:State
						 StepsToTable = 4  
						if( checkMsgContent( Term.createTerm("cleantable(Num)"), Term.createTerm("cleantable(Num)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 val TableNum = payloadArg(0)  
								forward("cleantable", "cleantable($TableNum)" ,"waiter" ) 
						}
					}
					 transition(edgeName="t014",targetState="checkTableAvail",cond=whenRequest("table"))
					transition(edgeName="t015",targetState="reach",cond=whenDispatch("clientatentrance"))
					transition(edgeName="t016",targetState="takeOrder",cond=whenDispatch("placeorder"))
					transition(edgeName="t017",targetState="serveOrder",cond=whenReply("orderready"))
					transition(edgeName="t018",targetState="getPayment",cond=whenDispatch("payment"))
					transition(edgeName="t019",targetState="goCleanTable",cond=whenDispatch("cleantable"))
				}	 
				state("goCleanTable") { //this:State
					action { //it:State
						if(Debug) 
						println("$name in ${currentState.stateName} | $currentMsg")
						if( checkMsgContent( Term.createTerm("cleantable(Num)"), Term.createTerm("cleantable(Num)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 val TableNum = payloadArg(0)  
								if(  StepsToTable > 0  
								 ){ StepsToTable--  
								delay(100) 
								if(Debug) 
								println("$StepsToTable steps remaining...")
								}
								else
								 {solve("updateWaiter(X,teatable($TableNum))","") //set resVar	
								 solve("roomstate(S)","") //set resVar	
								 if( currentSolution.isSuccess() ) {updateResourceRep( getCurSol("S").toString()  
								 )
								 }
								 else
								 {}
								 }
								forward("cleantable", "cleantable($TableNum)" ,"waiter" ) 
						}
						 if(Debug){ println("endof cleanTable"); readLine(); }  
					}
					 transition(edgeName="t020",targetState="checkTableAvail",cond=whenRequest("table"))
					transition(edgeName="t021",targetState="reach",cond=whenDispatch("clientatentrance"))
					transition(edgeName="t022",targetState="takeOrder",cond=whenDispatch("placeorder"))
					transition(edgeName="t023",targetState="serveOrder",cond=whenReply("orderready"))
					transition(edgeName="t024",targetState="getPayment",cond=whenDispatch("payment"))
					transition(edgeName="t025",targetState="goCleanTable",cond=whenDispatchGuarded("cleantable",{ StepsToTable > 0  
					}))
					transition(edgeName="t026",targetState="doCleanTable",cond=whenDispatchGuarded("cleantable",{ StepsToTable == 0  
					}))
				}	 
				state("doCleanTable") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("cleantable(Num)"), Term.createTerm("cleantable(Num)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 val TableNum = payloadArg(0)  
								delay(5000) 
								solve("cleanTable($TableNum,NewStatus)","") //set resVar	
								if(  getCurSol("NewStatus").toString() == "clean"  
								 ){forward("cleantableok", "cleantableok($TableNum)" ,"waiter" ) 
								}
								else
								 {forward("cleantable", "cleantable($TableNum)" ,"waiter" ) 
								 }
								solve("roomstate(S)","") //set resVar	
								updateResourceRep( getCurSol("S").toString()  
								)
								println(getCurSol("S").toString())
						}
					}
					 transition(edgeName="t027",targetState="checkClientOnHold",cond=whenDispatch("cleantableok"))
					transition(edgeName="t028",targetState="checkTableAvail",cond=whenRequest("table"))
					transition(edgeName="t029",targetState="reach",cond=whenDispatch("clientatentrance"))
					transition(edgeName="t030",targetState="takeOrder",cond=whenDispatch("placeorder"))
					transition(edgeName="t031",targetState="serveOrder",cond=whenReply("orderready"))
					transition(edgeName="t032",targetState="getPayment",cond=whenDispatch("payment"))
					transition(edgeName="t033",targetState="doCleanTable",cond=whenDispatch("cleantable"))
				}	 
				state("checkClientOnHold") { //this:State
					action { //it:State
						if(Debug) 
						println("table cleaned letting somebody in")
						 if(Debug)  
						solve("clients(L)","") //set resVar	
						 if(Debug) println("clients are: ${getCurSol("L").toString()}")  
						solve("client(Cid,onhold)","") //set resVar	
						if( currentSolution.isSuccess() ) { val OnHoldCid = getCurSol("Cid").toString()  
						 if(Debug)println("ok, client waiting is num: $OnHoldCid")  
						if(  Debug  
						 ){solve("roomstate(S)","") //set resVar	
						if( currentSolution.isSuccess() ) { println("------------------------------------pre solves ${getCurSol("S").toString()}")  
						}
						else
						{}
						}
						solve("retract(client($OnHoldCid,onhold))","") //set resVar	
						solve("reserveTable(N,$OnHoldCid)","") //set resVar	
						if(  Debug  
						 ){solve("roomstate(S)","") //set resVar	
						if( currentSolution.isSuccess() ) { println("------------------------------------post solves ${getCurSol("S").toString()}")  
						}
						else
						{}
						}
						if( currentSolution.isSuccess() ) {solve("roomstate(S)","") //set resVar	
						if( currentSolution.isSuccess() ) {updateResourceRep( getCurSol("S").toString()  
						)
						}
						else
						{}
						if(Debug) println("DEBUG ---------- sending clientatantrence($OnHoldCid)  ------------")  
						forward("clientatentrance", "clientatentrance($OnHoldCid)" ,"waiter" ) 
						}
						else
						{}
						}
						else
						{}
					}
					 transition( edgeName="goto",targetState="waitState", cond=doswitch() )
				}	 
				state("maybeRest") { //this:State
					action { //it:State
					}
					 transition( edgeName="goto",targetState="waitState", cond=doswitchGuarded({ IsWaiterAtHome  
					}) )
					transition( edgeName="goto",targetState="rest", cond=doswitchGuarded({! ( IsWaiterAtHome  
					) }) )
				}	 
				state("rest") { //this:State
					action { //it:State
						solve("waiter(athome)","") //set resVar	
						if( currentSolution.isSuccess() ) {if(Debug) 
						println("already at home")
						}
						else
						{if(Debug) 
						println("waiter rest")
						delay(DelayTime)
						solve("updateWaiter(X,athome)","") //set resVar	
						solve("roomstate(S)","") //set resVar	
						if( currentSolution.isSuccess() ) {updateResourceRep( getCurSol("S").toString()  
						)
						}
						else
						{}
						}
						 if(Debug){  println("endof rest"); readLine();}  
					}
					 transition( edgeName="goto",targetState="waitState", cond=doswitch() )
				}	 
			}
		}
}
