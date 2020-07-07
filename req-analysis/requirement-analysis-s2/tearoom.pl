%====================================================================================
% tearoom description   
%====================================================================================
mqttBroker("localhost", "1883", "unibo/gozzi/events").
context(ctxwaiter, "localhost",  "MQTT", "8050").
context(ctxsmartbell, "localhost",  "MQTT", "8051").
context(ctxbarman, "localhost",  "MQTT", "8056").
 qactor( barman, ctxbarman, "it.unibo.barman.Barman").
  qactor( smartbell, ctxsmartbell, "it.unibo.smartbell.Smartbell").
  qactor( waiter, ctxwaiter, "it.unibo.waiter.Waiter").
