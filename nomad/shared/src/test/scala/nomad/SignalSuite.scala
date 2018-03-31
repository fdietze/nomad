package nomad

class SignalSuite[
SIGNAL[_],
SINK[_],
SOURCE[_],
CANCELABLE
](
   implicit signalInstance: Subject[SIGNAL, SINK, SOURCE],
   sinkInstance: Sink[SINK],
   sourceInstance: Source[SOURCE, CANCELABLE],
   cancelableInstance: Cancelable[CANCELABLE]
 ) extends NomadTestSuite {
  test("distinct") {
   var triggered = 0
   val s:SIGNAL[String] = signalInstance.create("A")
   sourceInstance.foreach(signalInstance.source(s)) { _ =>
    triggered += 1
   }
   assert(triggered == 1)
   sinkInstance.onNext(signalInstance.sink(s))("B")
   assert(triggered == 2)
   sinkInstance.onNext(signalInstance.sink(s))("B")
   assert(triggered == 2)
   sinkInstance.onNext(signalInstance.sink(s))("A")
   assert(triggered == 3)
  }
}
