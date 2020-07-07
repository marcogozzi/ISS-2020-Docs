%====================================================================================
% tearoom description   
%====================================================================================
mqttBroker("localhost", "1883", "unibo/gozzi/events").
context(ctxwaiter, "localhost",  "MQTT", "8050").
context(ctxsmartbell, "localhost",  "MQTT", "8051").
context(ctxbarman, "localhost",  "MQTT", "8056").
context(ctxbasicrobot, "192.168.31.141",  "MQTT", "8020").
 qactor( robot, ctxwaiter, "it.unibo.robot.Robot").
  qactor( basicrobot, ctxbasicrobot, "external").
  qactor( barman, ctxbarman, "it.unibo.barman.Barman").
  qactor( smartbell, ctxsmartbell, "it.unibo.smartbell.Smartbell").
  qactor( waiter, ctxwaiter, "it.unibo.waiter.Waiter").
