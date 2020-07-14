%====================================================================================
% tearoomclientsimulationweb description   
%====================================================================================
mqttBroker("localhost", "1883", "unibo/gozzi/events").
context(ctxwaiter, "localhost",  "MQTT", "8050").
context(ctxsmartbell, "localhost",  "MQTT", "8051").
context(ctxclient, "127.0.0.1",  "MQTT", "8055").
 qactor( clientweb1, ctxclient, "it.unibo.clientweb.Clientweb").
  qactor( waiter, ctxwaiter, "external").
  qactor( smartbell, ctxsmartbell, "external").
