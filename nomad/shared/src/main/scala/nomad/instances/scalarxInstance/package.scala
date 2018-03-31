package nomad.instances

import cats.Eq
import nomad.{Sink, Source}
import rx._

package object scalarxInstance {

  implicit def cancelable: nomad.Cancelable[Obs] = (c: Obs) => c.kill()

  implicit def rxSource(implicit owner: Ctx.Owner): Source[Rx, Obs] = new Source[Rx, Obs] {
    override def foreach[A](s: Rx[A])(onNext: A => Unit)(implicit C: nomad.Cancelable[Obs]): Obs = s.foreach(onNext)

    override def distinctUntilChanged[A](s: Rx[A])(implicit eq: Eq[A]): Rx[A] = s

    override def flatMap[A, B](fa: Rx[A])(f: A => Rx[B]): Rx[B] = fa.flatMap(f)

    override def tailRecM[A, B](a: A)(f: A => Rx[Either[A, B]]): Rx[B] = f(a).flatMap {
      case Left(nextA) => tailRecM(nextA)(f)
      case Right(b) => pure(b)
    }

    override def pure[A](x: A): Rx[A] = Var(x)
  }
}
