/* Generated by AN DISI Unibo */ 
package it.unibo.barman

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Barman ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "s0"
	}
	@kotlinx.coroutines.ObsoleteCoroutinesApi
	@kotlinx.coroutines.ExperimentalCoroutinesApi			
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		return { //this:ActionBasciFsm
				state("s0") { //this:State
					action { //it:State
					}
					 transition(edgeName="t09",targetState="prepareTea",cond=whenRequest("relayorder"))
				}	 
				state("prepareTea") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("relayorder(Cid)"), Term.createTerm("relayorder(Cid)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 val Cid = payloadArg(0)  
								delay(1000) 
								answer("relayorder", "orderready", "orderready($Cid)"   )  
								updateResourceRep( "tea prepared for $Cid"  
								)
						}
					}
					 transition(edgeName="t010",targetState="prepareTea",cond=whenRequest("relayorder"))
				}	 
			}
		}
}
