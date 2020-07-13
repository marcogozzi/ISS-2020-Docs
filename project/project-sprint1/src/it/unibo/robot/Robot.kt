/* Generated by AN DISI Unibo */ 
package it.unibo.robot

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Robot ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "s0"
	}
	@kotlinx.coroutines.ObsoleteCoroutinesApi
	@kotlinx.coroutines.ExperimentalCoroutinesApi			
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		 
				var CurrentPlannedMove = ""
				val mapname                    = "teaRoomExplored"
				var planInterrupted            = false
		//		val StepTime                   = 600L //firefox f11 on laptop registrazione
		//		val StepTime                   = 800L //firefox f11 on monitor registrazione
		//		val StepTime                   = 600L //firefox f11 on monitor no registrazione
				var X = ""
				var Y = ""
				var Debug = false
				var StepTime = 600L
				var StepFailWaitTime = 10000L
		return { //this:ActionBasciFsm
				state("s0") { //this:State
					action { //it:State
						solve("consult('tearoomKB_Proj.pl')","") //set resVar	
						solve("stepTime(X)","") //set resVar	
						if( currentSolution.isSuccess() ) { StepTime = getCurSol("X").toString().toLong()  
						}
						else
						{}
						solve("stepFailWaitTime(X)","") //set resVar	
						if( currentSolution.isSuccess() ) { StepFailWaitTime = getCurSol("X").toString().toLong()  
						}
						else
						{}
						solve("debug(X)","") //set resVar	
						if( currentSolution.isSuccess() ) { Debug = true  
						}
						else
						{}
						println("robot init")
						itunibo.planner.plannerUtil.initAI(  )
						itunibo.planner.plannerUtil.loadRoomMap( mapname  )
						if(Debug) 
						println("INITIAL MAP")
						if(Debug) 
						itunibo.planner.plannerUtil.showMap(  )
					}
					 transition(edgeName="t00",targetState="planForMoves",cond=whenRequest("moveTo"))
				}	 
				state("planForMoves") { //this:State
					action { //it:State
						if(Debug) 
						println("robot planForMoves")
						if( checkMsgContent( Term.createTerm("moveTo(OldX,OldY,X,Y)"), Term.createTerm("moveTo(A,B,X,Y)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								
												val A = payloadArg(0)
												val B = payloadArg(1)
												X = payloadArg(2)
												Y = payloadArg(3)
												itunibo.planner.plannerUtil.planForGoal(X,Y)
												if(Debug){
													println("Invio per iniziare da $A $B a goal $X $Y")
													println("Le mosse sono ${itunibo.planner.plannerUtil.getActions()}")
													readLine()
												}
												delay(500)
						}
					}
					 transition( edgeName="goto",targetState="execPlannedMoves", cond=doswitch() )
				}	 
				state("execPlannedMoves") { //this:State
					action { //it:State
						println("robot execPlannedMoves")
						if( checkMsgContent( Term.createTerm("stepdone(V)"), Term.createTerm("stepdone(A)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								if(  Debug  
								 ){
												val msg = "robot: stepdone $CurrentPlannedMove : ${payloadArg(0)}"
												itunibo.planner.plannerUtil.showMap() 
												itunibo.planner.plannerUtil.updateMap(CurrentPlannedMove,msg) 
								}
								else
								 {itunibo.planner.plannerUtil.updateMap( CurrentPlannedMove, ""  )
								 }
						}
						  
						 			CurrentPlannedMove = itunibo.planner.plannerUtil.getNextPlannedMove()
						 			if(Debug){
							 			println("Invio per vedere mosse rimanenti")
										readLine()
										itunibo.planner.plannerUtil.showMap() 
										println("Now $CurrentPlannedMove in ${itunibo.planner.plannerUtil.getActions()}")
									}
						if(  CurrentPlannedMove.length > 0  
						 ){forward("doMove", "doMove($CurrentPlannedMove)" ,"robot" ) 
						}
						else
						 {answer("moveTo", "moveOk", "moveOk($X,$Y)"   )  
						 }
					}
					 transition(edgeName="t01",targetState="planForMoves",cond=whenRequestGuarded("moveTo",{ CurrentPlannedMove.length == 0  
					}))
					transition(edgeName="t02",targetState="execTheMove",cond=whenDispatch("doMove"))
				}	 
				state("execTheMove") { //this:State
					action { //it:State
						if(Debug) 
						println("robot execTheMove")
						delay(100) 
						if( checkMsgContent( Term.createTerm("doMove(M)"), Term.createTerm("doMove(w)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								request("step", "step($StepTime)" ,"basicrobot" )  
						}else{
							forward("cmd", "cmd($CurrentPlannedMove)" ,"basicrobot" ) 
							if(Debug) 
							itunibo.planner.plannerUtil.updateMap( CurrentPlannedMove, "robot: turning $CurrentPlannedMove"  )
							else 
							itunibo.planner.plannerUtil.updateMap( CurrentPlannedMove, ""  )
							delay(300) 
						}
					}
					 transition( edgeName="goto",targetState="execPlannedMoves", cond=doswitchGuarded({ CurrentPlannedMove != "w"  
					}) )
					transition( edgeName="goto",targetState="waitReply", cond=doswitchGuarded({! ( CurrentPlannedMove != "w"  
					) }) )
				}	 
				state("waitReply") { //this:State
					action { //it:State
						if(Debug) 
						println("robot waitReply")
					}
					 transition(edgeName="t03",targetState="execPlannedMoves",cond=whenReply("stepdone"))
					transition(edgeName="t04",targetState="replyWithFailure",cond=whenReply("stepfail"))
				}	 
				state("replyWithFailure") { //this:State
					action { //it:State
						println("robot replyWithFailure")
						
						    		val NewX = itunibo.planner.plannerUtil.get_curPos().first
						    		val NewY = itunibo.planner.plannerUtil.get_curPos().second
						    		itunibo.planner.plannerUtil.showMap() 
						    		println("Move Robot manually to the r cell within $StepFailWaitTime seconds, then press Enter if Debug")
						    		if(Debug) 	readLine()
							    	else 		delay(StepFailWaitTime)
						answer("moveTo", "moveKo", "moveKo($NewX,$NewY)"   )  
					}
					 transition(edgeName="t05",targetState="planForMoves",cond=whenRequest("moveTo"))
				}	 
			}
		}
}
