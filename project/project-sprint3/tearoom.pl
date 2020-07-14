%====================================================================================
% tearoom description   
%====================================================================================
mqttBroker("localhost", "1883", "unibo/gozzi/events").
context(ctxwaiter, "localhost",  "MQTT", "8050").
context(ctxsmartbell, "localhost",  "MQTT", "8051").
context(ctxbarman, "localhost",  "MQTT", "8056").
context(ctxbasicrobot, "127.0.0.1",  "MQTT", "8020").
 qactor( waiterkb, ctxwaiter, "it.unibo.waiterkb.Waiterkb").
  qactor( robot, ctxwaiter, "it.unibo.robot.Robot").
  qactor( basicrobot, ctxbasicrobot, "external").
  qactor( barman, ctxbarman, "it.unibo.barman.Barman").
  qactor( smartbell, ctxsmartbell, "it.unibo.smartbell.Smartbell").
  qactor( waiter, ctxwaiter, "it.unibo.waiter.Waiter").
