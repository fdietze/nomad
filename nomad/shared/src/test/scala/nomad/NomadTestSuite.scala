package nomad

import cats.instances.AllInstances
import cats.syntax.AllSyntax
import org.scalatest._
import org.typelevel.discipline.scalatest.Discipline

trait NomadTestSuite extends FunSuite
  with Matchers
  with Discipline
  with AllInstances
  with AllSyntax
