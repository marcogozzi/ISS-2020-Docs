/* Generated by AN DISI Unibo */ 
package it.unibo.ctxwaiter
import it.unibo.kactor.QakContext
import it.unibo.kactor.sysUtil
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
	QakContext.createContexts(
	        "localhost", this, "tearoomclientsimulation.pl", "sysRules.pl"
	)
}

