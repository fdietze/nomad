package nomad.common

import nomad._
import org.scalajs.dom
import cats.implicits._
import org.scalajs.dom.StorageEvent


class Storage[SUBJECT[_], SINK[_], SOURCE[_], CANCELABLE]
(domStorage: dom.Storage)
(
  implicit subjectInstance: Subject[SUBJECT, SINK, SOURCE],
  sinkInstance: Sink[SINK],
  sourceInstance: Source[SOURCE, CANCELABLE],
  cancelableInstance: Cancelable[CANCELABLE]
) {
  def handler(key: String): SUBJECT[Option[String]] = {
    val storage = new dom.ext.Storage(domStorage)

    val h: SUBJECT[Option[String]] = subjectInstance.create(storage(key))
    sourceInstance.foreach(subjectInstance.source(h)) {
      case Some(data) =>
        storage.update(key, data)
      case None =>
        storage.remove(key)
    }

    // TODO: use scala-dom-types like in outwatch
    // StorageEvents are only fired if the localStorage was changed in another window
    dom.window.addEventListener("storage", { (e: StorageEvent) =>
      val data = e match {
        case e: StorageEvent if e.storageArea == domStorage && e.key == key =>
          // newValue is either String or null if removed or cleared
          // Option() transformes this to Some(string) or None
          Option(e.newValue.asInstanceOf[String]) //TODO: https://github.com/scala-js/scala-js-dom/pull/308
        case e: StorageEvent if e.storageArea == domStorage && e.key == null =>
          // storage.clear() emits an event with key == null
          None
      }

      sinkInstance.onNext(subjectInstance.sink(h))(data)
    })
//    subjectInstance.transformSource(h)((source: SOURCE[Option[String]]) => sourceInstance.distinctUntilChanged(source))
    h

    //  def handlerWithoutEvents[S[_], H[_], E[_] : Effect](key: String)(implicit s: Source[S, C], h: Subject[H, C]): E[H[Option[String]]] = {
    //    handlerWithTransform(key, identity)
    //  }

    // def handler(key: String)(implicit scheduler: Scheduler): IO[Handler[Option[String]]] = {
    //   // StorageEvents are only fired if the localStorage was changed in another window
    //   val storageEvents: Observable[Option[String]] = events.window.onStorage
    //     .collect {
    //       case e: StorageEvent if e.storageArea == domStorage && e.key == key =>
    //         // newValue is either String or null if removed or cleared
    //         // Option() transformes this to Some(string) or None
    //         Option(e.newValue.asInstanceOf[String]) //TODO: https://github.com/scala-js/scala-js-dom/pull/308
    //       case e: StorageEvent if e.storageArea == domStorage && e.key == null =>
    //         // storage.clear() emits an event with key == null
    //         None
    //     }

    //   handlerWithTransform(key, Observable.merge(_, storageEvents))
    // }
  }
}

object Storage {
  def localStorage[SUBJECT[_], SINK[_], SOURCE[_], CANCELABLE](implicit subjectInstance: Subject[SUBJECT, SINK, SOURCE], sinkInstance: Sink[SINK], sourceInstance: Source[SOURCE, CANCELABLE], cancelableInstance: Cancelable[CANCELABLE]) = new Storage[SUBJECT, SINK, SOURCE, CANCELABLE](org.scalajs.dom.window.localStorage)


  //TODO: sessionstorage
}
