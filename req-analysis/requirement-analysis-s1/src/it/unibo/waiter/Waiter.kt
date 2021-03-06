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
				val RestWaitTime 	= 5000L
		return { //this:ActionBasciFsm
				state("s0") { //this:State
					action { //it:State
						println("waiter RA-s1 starting")
						discardMessages = false
						solve("consult('tearoomKB-RA-sprint1.pl')","") //set resVar	
						solve("roomstate(S)","") //set resVar	
						if( currentSolution.isSuccess() ) {updateResourceRep( getCurSol("S").toString()  
						)
						}
						else
						{}
					}
					 transition(edgeName="t07",targetState="checkTableAvail",cond=whenRequest("table"))
				}	 
				state("checkTableAvail") { //this:State
					action { //it:State
						println("waiter checkTableAvail")
						if( checkMsgContent( Term.createTerm("table(CID)"), Term.createTerm("table(Cid)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 val Cid = payloadArg(0)  
								solve("occupyHall($Cid)","") //set resVar	
								solve("reserveTable(Num,$Cid)","") //set resVar	
								if( currentSolution.isSuccess() ) {solve("roomstate(S)","") //set resVar	
								if( currentSolution.isSuccess() ) {updateResourceRep( getCurSol("S").toString()  
								)
								}
								else
								{}
								answer("table", "available", "available($Cid)"   )  
								forward("clientatentrance", "clientatentrance($Cid)" ,"waiter" ) 
								}
								else
								{answer("table", "full", "full($Cid,$MaxStayTime)"   )  
								solve("freeHall($Cid)","") //set resVar	
								}
						}
					}
					 transition(edgeName="t08",targetState="reach",cond=whenDispatch("clientatentrance"))
				}	 
				state("reach") { //this:State
					action { //it:State
						println("waiter reach")
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
								solve("teatable(Num,reserved($Cid),clean)","") //set resVar	
								 val TableNum = getCurSol("Num").toString()  
								delay(DelayTime)
								solve("updateWaiter(X,teatable($TableNum))","") //set resVar	
								solve("engageTable($TableNum,$Cid)","") //set resVar	
								emit("attable", "attable($Cid)" ) 
								solve("roomstate(S)","") //set resVar	
								if( currentSolution.isSuccess() ) {updateResourceRep( getCurSol("S").toString()  
								)
								}
								else
								{}
						}
					}
					 transition(edgeName="t09",targetState="takeOrder",cond=whenDispatch("placeorder"))
				}	 
				state("takeOrder") { //this:State
					action { //it:State
						println("waiter takeOrder")
						if( checkMsgContent( Term.createTerm("placeorder(CID)"), Term.createTerm("placeorder(Cid)"), 
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
								request("relayorder", "relayorder($Cid)" ,"barman" )  
						}
					}
					 transition(edgeName="t010",targetState="serveOrder",cond=whenReply("orderready"))
				}	 
				state("serveOrder") { //this:State
					action { //it:State
						println("waiter serveOrder")
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
								solve("updateWaiter(X,teatable($TableNum))","") //set resVar	
								solve("roomstate(S)","") //set resVar	
								if( currentSolution.isSuccess() ) {updateResourceRep( getCurSol("S").toString()  
								)
								}
								else
								{}
								emit("delivered", "delivered($Cid)" ) 
						}
					}
					 transition(edgeName="t011",targetState="getPayment",cond=whenDispatch("payment"))
				}	 
				state("getPayment") { //this:State
					action { //it:State
						println("waiter getPayment")
						if( checkMsgContent( Term.createTerm("payment(Cid)"), Term.createTerm("payment(Cid)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 val Cid = payloadArg(0)  
								solve("teatable(Num,busy($Cid),dirty)","") //set resVar	
								 val TableNum = getCurSol("Num").toString()  
								delay(DelayTime)
								emit("paymentOk", "paymentOk($Cid)" ) 
								solve("updateWaiter(X,teatable($TableNum))","") //set resVar	
								solve("roomstate(S)","") //set resVar	
								if( currentSolution.isSuccess() ) {updateResourceRep( getCurSol("S").toString()  
								)
								}
								else
								{}
								delay(DelayTime)
								emit("exitOk", "exitOk($Cid)" ) 
								solve("updateWaiter(X,exitdoor)","") //set resVar	
								solve("freeTable($TableNum,$Cid)","") //set resVar	
								solve("roomstate(S)","") //set resVar	
								if( currentSolution.isSuccess() ) {updateResourceRep( getCurSol("S").toString()  
								)
								}
								else
								{}
						}
					}
					 transition( edgeName="goto",targetState="cleanTable", cond=doswitch() )
				}	 
				state("cleanTable") { //this:State
					action { //it:State
						println("waiter cleanTable")
						solve("dirtyTable(Num)","") //set resVar	
						if( currentSolution.isSuccess() ) { val TableNum = getCurSol("Num").toString()  
						delay(DelayTime)
						solve("updateWaiter(X,teatable($TableNum))","") //set resVar	
						solve("roomstate(S)","") //set resVar	
						if( currentSolution.isSuccess() ) {updateResourceRep( getCurSol("S").toString()  
						)
						}
						else
						{}
						delay(DelayTime)
						solve("cleanTable($TableNum)","") //set resVar	
						solve("roomstate(S)","") //set resVar	
						if( currentSolution.isSuccess() ) {updateResourceRep( getCurSol("S").toString()  
						)
						}
						else
						{}
						}
						else
						{}
						solve("roomstate(S)","") //set resVar	
						if( currentSolution.isSuccess() ) {println( "$tt After clean ${getCurSol("S").toString()}" )  
						updateResourceRep( getCurSol("S").toString()  
						)
						}
						else
						{}
						stateTimer = TimerActor("timer_cleanTable", 
							scope, context!!, "local_tout_waiter_cleanTable", RestWaitTime )
					}
					 transition(edgeName="t012",targetState="rest",cond=whenTimeout("local_tout_waiter_cleanTable"))   
					transition(edgeName="t013",targetState="checkTableAvail",cond=whenRequest("table"))
				}	 
				state("rest") { //this:State
					action { //it:State
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
					 transition(edgeName="t014",targetState="checkTableAvail",cond=whenRequest("table"))
				}	 
			}
		}
}
