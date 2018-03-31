
lazy val commonSettings = Seq(
  organization := "com.github.fdietze",
  name         := "nomad",
  version      := "master-SNAPSHOT",

  scalaVersion := "2.12.5",
  crossScalaVersions := Seq("2.11.12", "2.12.5"),

  resolvers ++= (
    ("jitpack" at "https://jitpack.io") ::
    Nil
  ),

  scalacOptions ++=
    "-encoding" :: "UTF-8" ::
    "-unchecked" ::
    "-deprecation" ::
    "-explaintypes" ::
    "-feature" ::
    "-language:_" ::
    "-Xfuture" ::
    "-Xlint" ::
    "-Ypartial-unification" ::
    "-Yno-adapted-args" ::
    "-Ywarn-infer-any" ::
    "-Ywarn-value-discard" ::
    "-Ywarn-nullary-override" ::
    "-Ywarn-nullary-unit" ::
    Nil,

  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 12)) =>
        "-Ywarn-extra-implicit" ::
        Nil
      case _ =>
        Nil
    }
  },

  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6"),

  // initialCommands in console := """
  // import nomad._
  // """,
)

val catsVersion = "1.1.0"

lazy val nomad = crossProject
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.github.mpilquist" %% "simulacrum" % "0.12.0" % "provided",
      "org.typelevel" %%% "cats-core" % catsVersion,
      "org.scalatest" %% "scalatest" % "3.0.5" % "test",
      "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
      "org.typelevel" %% "discipline" % "0.9.0",

      "io.monix" %%% "monix" % "3.0.0-M3",
      "com.lihaoyi" %%% "scalarx" % "0.3.2"
    ),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.5"
    ),
    requiresDOM in Test := true,
    useYarn := true,
    scalacOptions += "-P:scalajs:sjsDefinedByDefault",
    scalacOptions ++= git.gitHeadCommit.value.map { headCommit =>
      val local = baseDirectory.value.toURI
      val remote = s"https://raw.githubusercontent.com/fdietze/colorado/${headCommit}/"
      s"-P:scalajs:mapSourceURI:$local->$remote"
    }
  )

lazy val nomadJS = nomad.js
lazy val nomadJVM = nomad.jvm
