/* Generated by AN DISI Unibo */ 
package it.unibo.ctxclient
import it.unibo.kactor.QakContext
import it.unibo.kactor.sysUtil
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = runBlocking {
	QakContext.createContexts(
	        "127.0.0.1", this, args.get(0), "sysRules.pl"
	)
}

