package nomad.common

import monix.execution.ExecutionModel.SynchronousExecution
import monix.execution.Scheduler
import monix.execution.schedulers.TrampolineScheduler
import org.scalajs.dom._
import org.scalajs.dom.ext.LocalStorage

import collection.mutable
import org.scalajs.dom.window.localStorage
import cats.implicits._
import nomad.{NomadTestSuite, instances}
import nomad.instances.monixInstance

trait LocalStorageMock {

  import scala.collection.mutable
  import scala.scalajs.js


  if (js.isUndefined(window.localStorage)) {
    js.Dynamic.global.window.updateDynamic("localStorage")(new js.Object {
      private val map = new mutable.HashMap[String, String]

      def getItem(key: String): String = map.getOrElse(key, null)

      def setItem(key: String, value: String): Unit = {
        map += key -> value
      }

      def removeItem(key: String): Unit = {
        map -= key
      }

      def clear(): Unit = map.clear()
    })
  }

  def dispatchStorageEvent(key: String, newValue: String, oldValue: String): Unit = {
    if (key == null) window.localStorage.clear()
    else window.localStorage.setItem(key, newValue)

    def initEvent(e: Event)(eventTypeArg: String, canBubbleArg: Boolean,
                            cancelableArg: Boolean): Unit = e.initEvent(eventTypeArg, canBubbleArg, cancelableArg)

    val event = document.createEvent("Events")
    initEvent(event)("storage", canBubbleArg = true, cancelableArg = false)
    event.asInstanceOf[js.Dynamic].key = key
    event.asInstanceOf[js.Dynamic].newValue = newValue
    event.asInstanceOf[js.Dynamic].oldValue = oldValue
    event.asInstanceOf[js.Dynamic].storageArea = window.localStorage
    window.dispatchEvent(event)
    ()
  }

  implicit val scheduler = TrampolineScheduler(Scheduler.global, SynchronousExecution)
}

class LocalStorageSuite extends NomadTestSuite with LocalStorageMock {

  test("LocalStorage") {
    window.localStorage.clear()

    val key = "banana"
    val triggeredHandlerEvents = mutable.ArrayBuffer.empty[Option[String]]

    assert(localStorage.getItem(key) == null)

    import nomad.instances.{Monix => monixInstance}
    val storageHandler = Storage.localStorage(instances.monixInstance.replaySubject, instances.monixInstance.observer, instances.monixInstance.observable, instances.monixInstance.cancelable).handler(key)
    storageHandler.foreach { e => triggeredHandlerEvents += e }
    assert(localStorage.getItem(key) == null)
    assert(triggeredHandlerEvents.toList == List(None))

    storageHandler.onNext(Some("joe"))
    assert(localStorage.getItem(key) == "joe")
    assert(triggeredHandlerEvents.toList == List(None == Some("joe")))

    var initialValue: Option[String] = null
    storageHandler.foreach {
      initialValue = _
    }
    assert(initialValue == Some("joe"))

    storageHandler.onNext(None)
    assert(localStorage.getItem(key) == null)
    assert(triggeredHandlerEvents.toList == List(None, Some("joe") == None))

    // simulate localStorage.setItem(key, "split") from another window
    dispatchStorageEvent(key, newValue = "split", null)
    assert(localStorage.getItem(key) == "split")
    assert(triggeredHandlerEvents.toList == List(None, Some("joe"), None, Some("split")))

    // simulate localStorage.removeItem(key) from another window
    dispatchStorageEvent(key, null, "split")
    assert(localStorage.getItem(key) == null)
    assert(triggeredHandlerEvents.toList == List(None, Some("joe"), None, Some("split"), None))

    // only trigger handler if value changed
    storageHandler.onNext(None)
    assert(localStorage.getItem(key) == null)
    assert(triggeredHandlerEvents.toList == List(None, Some("joe"), None, Some("split"), None))

    storageHandler.onNext(Some("rhabarbar"))
    assert(localStorage.getItem(key) == "rhabarbar")
    assert(triggeredHandlerEvents.toList == List(None, Some("joe"), None, Some("split"), None, Some("rhabarbar")))

    // localStorage.clear() from another window
    dispatchStorageEvent(null, null, null)
    assert(localStorage.getItem(key) == null)
    assert(triggeredHandlerEvents.toList == List(None, Some("joe"), None, Some("split"), None, Some("rhabarbar"), None))
  }
}
