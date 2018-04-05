val scala_2_11             = "2.11.11"
val scala_2_12             = "2.12.6"
val mainScalaVersion       = scala_2_12
val supportedScalaVersions = Seq(scala_2_11, scala_2_12)

lazy val baseSettings = Seq(
  organization := "pl.iterators",
  organizationName := "Iterators",
  organizationHomepage := Some(url("https://iterato.rs")),
  homepage := Some(url("https://github.com/theiterators/kebs")),
  scalaVersion := mainScalaVersion,
  scalacOptions := Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8"),
  scalafmtVersion := "1.3.0",
  scalafmtOnCompile := true
)

lazy val commonMacroSettings = baseSettings ++ Seq(
  libraryDependencies += "org.scala-lang" % "scala-reflect"  % scalaVersion.value,
  libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided"
)

lazy val metaSettings = baseSettings ++ Seq(
  addCompilerPlugin("org.scalameta"           % "paradise"   % "3.0.0-M10" cross CrossVersion.full),
  libraryDependencies ++= Seq("org.scalameta" %% "scalameta" % "1.8.0" % Provided, scalaTest % "test")
)

lazy val publishToNexus = publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

lazy val crossBuildSettings = Seq(crossScalaVersions := supportedScalaVersions, releaseCrossBuild := true)

lazy val publishSettings = Seq(
  publishToNexus,
  publishMavenStyle := true,
  pomIncludeRepository := const(false),
  licenses := Seq("MIT License" -> url("http://opensource.org/licenses/MIT")),
  developers := List(
    Developer(id = "mrzeznicki",
              name = "Marcin Rzeźnicki",
              email = "mrzeznicki@iterato.rs",
              url = url("https://github.com/marcin-rzeznicki"))),
  scmInfo := Some(
    ScmInfo(browseUrl = url("https://github.com/theiterators/kebs"), connection = "scm:git:https://github.com/theiterators/kebs.git")),
  useGpg := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
) ++ crossBuildSettings

lazy val noPublishSettings =
  Seq(
    publishToNexus /*must be set for sbt-release*/,
    publishArtifact := false,
    releasePublishArtifactsAction := {
      val projectName = name.value
      streams.value.log.warn(s"Publishing for $projectName is turned off")
    }
  )

def optional(dependency: ModuleID) = dependency % "provided"
def sv(scalaVersion: String, scala2_11Version: String, scala2_12Version: String) =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, 12)) => scala2_12Version
    case Some((2, 11)) => scala2_11Version
    case _ =>
      throw new IllegalArgumentException(s"Unsupported Scala version $scalaVersion")
  }

val scalaTest     = "org.scalatest" %% "scalatest" % "3.0.4"
val slick         = "com.typesafe.slick" %% "slick" % "3.2.2"
val optionalSlick = optional(slick)
val slickPg       = "com.github.tminglei" %% "slick-pg" % "0.16.0"
val sprayJson     = "io.spray" %% "spray-json" % "1.3.3"
val playJson      = "com.typesafe.play" %% "play-json" % "2.6.7"

val enumeratumVersion = "1.5.12"
val enumeratum        = "com.beachape" %% "enumeratum" % enumeratumVersion
def enumeratumInExamples = {
  val playJsonSupport = "com.beachape" %% "enumeratum-play-json" % enumeratumVersion
  Seq(enumeratum, playJsonSupport)
}
val optionalEnumeratum = optional(enumeratum)

val akkaHttpVersion = "10.0.5"
val akkaHttp        = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
val akkaHttpTestkit = "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion
def akkaHttpInExamples = {
  val akkaHttpSprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
  Seq(akkaHttp, akkaHttpSprayJson)
}
def akkaHttpInBenchmarks = akkaHttpInExamples :+ akkaHttpTestkit

val avroVersion = "1.7.0"
val avro        = "com.sksamuel.avro4s" %% "avro4s-core" % avroVersion

lazy val commonSettings = baseSettings ++ Seq(
  scalacOptions ++= Seq("-language:experimental.macros"),
  (scalacOptions in Test) ++= Seq("-Ymacro-debug-lite", "-Xlog-implicits"),
  libraryDependencies += scalaTest % "test"
)

lazy val slickSettings = commonSettings ++ Seq(
  libraryDependencies += slick,
  libraryDependencies += slickPg % "test",
  libraryDependencies += optionalEnumeratum
)

lazy val macroUtilsSettings = commonMacroSettings ++ Seq(
  libraryDependencies += optionalEnumeratum
)

lazy val sprayJsonMacroSettings = commonMacroSettings ++ Seq(
  libraryDependencies += sprayJson
)

lazy val sprayJsonSettings = commonSettings ++ Seq(
  libraryDependencies += optionalEnumeratum
)

lazy val playJsonSettings = commonSettings ++ Seq(
  libraryDependencies += playJson
)

lazy val akkaHttpSettings = commonSettings ++ Seq(
  libraryDependencies += akkaHttp,
  libraryDependencies += akkaHttpTestkit % "test",
  libraryDependencies += optionalEnumeratum
)

lazy val avroSettings = commonSettings ++ Seq(
  libraryDependencies += avro
)

lazy val taggedSettings = commonSettings ++ Seq(
  libraryDependencies += optionalSlick
)

lazy val examplesSettings = commonSettings ++ Seq(
  libraryDependencies += slickPg,
  libraryDependencies ++= enumeratumInExamples,
  libraryDependencies ++= akkaHttpInExamples
)

lazy val benchmarkSettings = commonSettings ++ Seq(
  libraryDependencies += scalaTest,
  libraryDependencies += enumeratum,
  libraryDependencies ++= akkaHttpInBenchmarks
)

lazy val taggedMetaSettings = metaSettings

lazy val macroUtils = project
  .in(file("macro-utils"))
  .settings(macroUtilsSettings: _*)
  .settings(crossBuildSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "macro-utils",
    description := "Macros supporting Kebs library",
    moduleName := "kebs-macro-utils"
  )

lazy val slickSupport = project
  .in(file("slick"))
  .dependsOn(macroUtils)
  .settings(slickSettings: _*)
  .settings(crossBuildSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "slick",
    description := "Library to eliminate the boilerplate code that comes with the use of Slick",
    moduleName := "kebs-slick"
  )

lazy val sprayJsonMacros = project
  .in(file("spray-json-macros"))
  .dependsOn(macroUtils)
  .settings(sprayJsonMacroSettings: _*)
  .settings(crossBuildSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "spray-json-macros",
    description := "Automatic generation of Spray json formats for case-classes - macros",
    moduleName := "kebs-spray-json-macros"
  )

lazy val sprayJsonSupport = project
  .in(file("spray-json"))
  .dependsOn(sprayJsonMacros)
  .settings(sprayJsonSettings: _*)
  .settings(crossBuildSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "spray-json",
    description := "Automatic generation of Spray json formats for case-classes",
    moduleName := "kebs-spray-json"
  )

lazy val playJsonSupport = project
  .in(file("play-json"))
  .dependsOn(macroUtils)
  .settings(playJsonSettings: _*)
  .settings(crossBuildSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "play-json",
    description := "Automatic generation of Play json formats for case-classes",
    moduleName := "kebs-play-json"
  )

lazy val akkaHttpSupport = project
  .in(file("akka-http"))
  .dependsOn(macroUtils)
  .settings(akkaHttpSettings: _*)
  .settings(crossBuildSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "akka-http",
    description := "Automatic generation of akka-http deserializers for 1-element case classes",
    moduleName := "kebs-akka-http"
  )

lazy val avroSupport = project
  .in(file("avro"))
  .dependsOn(macroUtils)
  .settings(avroSettings: _*)
  .settings(crossBuildSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "avro",
    description := "Automatic generation of avro4s custom mappings for 1-element case classes",
    moduleName := "kebs-avro"
  )

lazy val tagged = project
  .in(file("tagged"))
  .settings(taggedSettings: _*)
  .settings(crossBuildSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "tagged",
    description := "Representation of tagged types",
    moduleName := "kebs-tagged"
  )

lazy val taggedMeta = project
  .in(file("tagged-meta"))
  .dependsOn(tagged)
  .settings(taggedMetaSettings: _*)
  .settings(crossBuildSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "tagged-meta",
    description := "Representation of tagged types - code generation based on scala-meta",
    moduleName := "kebs-tagged-meta"
  )

lazy val examples = project
  .in(file("examples"))
  .dependsOn(slickSupport, sprayJsonSupport, playJsonSupport, akkaHttpSupport)
  .settings(examplesSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    name := "examples",
    moduleName := "kebs-examples"
  )

lazy val benchmarks = project
  .in(file("benchmarks"))
  .dependsOn(sprayJsonSupport)
  .enablePlugins(JmhPlugin)
  .settings(benchmarkSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    name := "benchmarks",
    moduleName := "kebs-benchmarks"
  )

import ReleaseTransformations._
lazy val kebs = project
  .in(file("."))
  .aggregate(
    tagged,
    macroUtils,
    slickSupport,
    sprayJsonMacros,
    sprayJsonSupport,
    playJsonSupport,
    akkaHttpSupport,
    avroSupport,
    taggedMeta,
    examples
  )
  .settings(baseSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    name := "kebs",
    description := "Library to eliminate the boilerplate code",
    releaseCrossBuild := false /*to work with sbt-doge*/,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      releaseStepCommandAndRemaining("+test"),
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommandAndRemaining("+publishSigned"),
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )
