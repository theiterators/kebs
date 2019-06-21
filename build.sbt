val scala_2_11                      = "2.11.12"
val scala_2_12                      = "2.12.8"
val scala_2_13                      = "2.13.0"
val mainScalaVersion                = scala_2_12
val fullySupportedScalaVersions     = Seq(scala_2_11, scala_2_12)
val partiallySupportedScalaVersions = Seq(scala_2_11, scala_2_12, scala_2_13)

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

lazy val metaSettings = commonSettings ++ Seq(
  addCompilerPlugin("org.scalameta"           % "paradise"   % "3.0.0-M11" cross CrossVersion.full),
  libraryDependencies ++= Seq("org.scalameta" %% "scalameta" % "1.8.0", scalaTest % "test")
)

lazy val publishToNexus = publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

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
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
)

lazy val noPublishSettings =
  Seq(
    publishToNexus /*must be set for sbt-release*/,
    publishArtifact := false,
    releaseCrossBuild := false,
    releasePublishArtifactsAction := {
      val projectName = name.value
      streams.value.log.warn(s"Publishing for $projectName is turned off")
    }
  )

def optional(dependency: ModuleID) = dependency % "provided"
def sv[A](scalaVersion: String, scala2_11Version: => A, scala2_12Version: => A, scala2_13Version: => A) =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, 13)) => scala2_13Version
    case Some((2, 12)) => scala2_12Version
    case Some((2, 11)) => scala2_11Version
    case _ =>
      throw new IllegalArgumentException(s"Unsupported Scala version $scalaVersion")
  }

val scalaTest     = "org.scalatest" %% "scalatest" % "3.0.8"
val slick         = "com.typesafe.slick" %% "slick" % "3.3.2"
val optionalSlick = optional(slick)
val slickPg       = "com.github.tminglei" %% "slick-pg" % "0.17.3"
val sprayJson     = "io.spray" %% "spray-json" % "1.3.5"
val playJson      = "com.typesafe.play" %% "play-json" % "2.7.4"

val enumeratumVersion = "1.5.13"
val enumeratum        = "com.beachape" %% "enumeratum" % enumeratumVersion
def enumeratumInExamples = {
  val playJsonSupport = "com.beachape" %% "enumeratum-play-json" % enumeratumVersion
  Seq(enumeratum, playJsonSupport)
}
val optionalEnumeratum = optional(enumeratum)

val akkaVersion       = "2.5.23"
val akkaHttpVersion   = "10.1.8"
val akkaStream        = "com.typesafe.akka" %% "akka-stream" % akkaVersion
val akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion
val akkaHttp          = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
val akkaHttpTestkit   = "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion
def akkaHttpInExamples = {
  val akkaHttpSprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
  Seq(akkaStream, akkaHttp, akkaHttpSprayJson)
}
def akkaHttpInBenchmarks = akkaHttpInExamples :+ akkaHttpTestkit

val avroVersion = "1.9.0"
val avro        = "com.sksamuel.avro4s" %% "avro4s-core" % avroVersion

lazy val commonSettings = baseSettings ++ Seq(
  scalacOptions ++= Seq("-language:experimental.macros"),
  (scalacOptions in Test) ++= Seq("-Ymacro-debug-lite", "-Xlog-implicits"),
  libraryDependencies += scalaTest % "test"
)

lazy val slickSettings = commonSettings ++ Seq(
  crossScalaVersions := fullySupportedScalaVersions,
  libraryDependencies += slick,
  libraryDependencies += slickPg % "test",
  libraryDependencies += optionalEnumeratum
)

lazy val macroUtilsSettings = commonMacroSettings ++ Seq(
  crossScalaVersions := partiallySupportedScalaVersions,
  libraryDependencies += optionalEnumeratum
)

lazy val sprayJsonMacroSettings = commonMacroSettings ++ Seq(
  crossScalaVersions := partiallySupportedScalaVersions,
  libraryDependencies += sprayJson
)

lazy val sprayJsonSettings = commonSettings ++ Seq(
  crossScalaVersions := partiallySupportedScalaVersions,
  libraryDependencies += optionalEnumeratum
)

lazy val playJsonSettings = commonSettings ++ Seq(
  crossScalaVersions := partiallySupportedScalaVersions,
  libraryDependencies += playJson
)

lazy val akkaHttpSettings = commonSettings ++ Seq(
  crossScalaVersions := partiallySupportedScalaVersions,
  libraryDependencies ++= sv(scalaVersion.value, Seq(akkaStream, akkaHttp), Seq(akkaHttp), Seq(akkaHttp)),
  libraryDependencies += akkaStreamTestkit % "test",
  libraryDependencies += akkaHttpTestkit   % "test",
  libraryDependencies += optionalEnumeratum
)

lazy val avroSettings = commonSettings ++ Seq(
  crossScalaVersions := fullySupportedScalaVersions,
  libraryDependencies += avro
)

lazy val taggedSettings = commonSettings ++ Seq(
  crossScalaVersions := partiallySupportedScalaVersions,
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

lazy val taggedMetaSettings = metaSettings ++ Seq(
  crossScalaVersions := fullySupportedScalaVersions,
  libraryDependencies += optional(sprayJson)
)

lazy val macroUtils = project
  .in(file("macro-utils"))
  .settings(macroUtilsSettings: _*)
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
  .settings(publishSettings: _*)
  .settings(
    name := "avro",
    description := "Automatic generation of avro4s custom mappings for 1-element case classes",
    moduleName := "kebs-avro"
  )

lazy val tagged = project
  .in(file("tagged"))
  .settings(taggedSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "tagged",
    description := "Representation of tagged types",
    moduleName := "kebs-tagged"
  )

lazy val taggedMeta = project
  .in(file("tagged-meta"))
  .dependsOn(macroUtils, tagged, sprayJsonSupport % "test -> test")
  .settings(taggedMetaSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "tagged-meta",
    description := "Representation of tagged types - code generation based on scala-meta",
    moduleName := "kebs-tagged-meta"
  )

lazy val examples = project
  .in(file("examples"))
  .dependsOn(slickSupport, sprayJsonSupport, playJsonSupport, akkaHttpSupport, taggedMeta)
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
    taggedMeta
  )
  .settings(baseSettings: _*)
  .settings(
    name := "kebs",
    description := "Library to eliminate the boilerplate code",
    publishToNexus, /*must be set for sbt-release*/
    releaseCrossBuild := true,
    publishArtifact := false
  )
