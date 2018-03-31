package nomad

import cats._
import simulacrum._

@typeclass
trait Cancelable[C] {
  def cancel(c: C): Unit
}

object Source {
  def apply[M[_], C](implicit S: Source[M, C]): Source[M, C] = S
}

trait Source[M[_], C] extends Monad[M] {
  def foreach[A](s: M[A])(cb: A => Unit)(implicit C: Cancelable[C]): C
  def distinctUntilChanged[A](s: M[A])(implicit eq: Eq[A]):M[A]
}

@typeclass
trait Sink[M[_]] {
  def onNext[A](s: M[A])(elem: A): Unit
}

//object Subject {
//  def apply[SUBJECT[_], SOURCE[_], CANCELABLE](implicit S: Subject[SUBJECT, SOURCE, CANCELABLE]): Subject[SUBJECT, SOURCE, CANCELABLE] = S
//}

trait Subject[SUBJECT[_],SINK[_],SOURCE[_]] { // TODO: extends Profunctor[SUBJECT] {
  def source[A](subject:SUBJECT[A]):SOURCE[A]
  def sink[A](subject:SUBJECT[A]):SINK[A]
  def create[A](seed: A): SUBJECT[A]
}

trait Signal[SUBJECT[_],SINK[_],SOURCE[_]] {
  def source[A](subject:SUBJECT[A]):SOURCE[A]
  def sink[A](subject:SUBJECT[A]):SINK[A]
  def create[A](seed: A): SUBJECT[A]
}

