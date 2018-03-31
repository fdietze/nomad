package nomad.instances

import cats.Eq
import monix.execution.{Cancelable, Scheduler}
import monix.reactive.subjects.ReplaySubject
import monix.reactive.{Observable, Observer}
import nomad.{Sink, Source}

package object monixInstance {
  implicit def cancelable: nomad.Cancelable[monix.execution.Cancelable] = (c: Cancelable) => c.cancel()

  implicit def observer: Sink[Observer] = new Sink[Observer] {
    override def onNext[A](s: Observer[A])(elem: A): Unit = s.onNext(elem)
  }

  implicit def observable(implicit scheduler: Scheduler): Source[Observable, Cancelable] = new Source[Observable, Cancelable] {
    override def foreach[A](s: Observable[A])(onNext: A => Unit)(implicit C: nomad.Cancelable[Cancelable]): Cancelable = s.foreach(onNext)

    override def distinctUntilChanged[A](s: Observable[A])(implicit eq: Eq[A]): Observable[A] = s.distinctUntilChanged

    override def flatMap[A, B](fa: Observable[A])(f: A => Observable[B]): Observable[B] = fa.flatMap(f)

    override def tailRecM[A, B](a: A)(f: A => Observable[Either[A, B]]): Observable[B] = Observable.tailRecM(a)(f)

    override def pure[A](x: A): Observable[A] = Observable.pure(x)
  }

  implicit def replaySubject(implicit scheduler: Scheduler): nomad.Subject[ReplaySubject, Observer, Observable] = new nomad.Subject[ReplaySubject, Observer, Observable] {
    override def source[A](subject: ReplaySubject[A]): Observable[A] = subject

    override def sink[A](subject: ReplaySubject[A]): Observer[A] = subject

    override def create[A](seed: A): ReplaySubject[A] = ReplaySubject[A](seed)
  }
}
