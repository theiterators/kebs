lazy val scalafmtSettings = Seq(
    scalafmtConfig := Some(file(".scalafmt.conf"))
  ) ++ reformatOnCompileSettings

val scala_2_11             = "2.11.8"
val scala_2_12             = "2.12.1"
val mainScalaVersion       = scala_2_11
val supportedScalaVersions = Seq(scala_2_11, scala_2_12)

lazy val baseSettings = Seq(
    organization := "pl.iterators",
    organizationName := "Iterators",
    organizationHomepage := Some(url("https://iterato.rs")),
    homepage := Some(url("https://github.com/theiterators/kebs")),
    scalaVersion := mainScalaVersion,
    scalacOptions := Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8")
  ) ++ scalafmtSettings

lazy val crossBuildSettings = Seq(crossScalaVersions := supportedScalaVersions)

lazy val commonMacroSettings = baseSettings ++ Seq(
    libraryDependencies += "org.scala-lang" % "scala-reflect"  % scalaVersion.value,
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided"
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
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
)

lazy val noPublishSettings =
  Seq(publishToNexus /*must be set for sbt-release*/, publishArtifact := false, releasePublishArtifactsAction := {
    val projectName = name.value
    streams.value.log.warn(s"Publishing for $projectName is turned off")
  })

def optional(dependency: ModuleID) = dependency % "provided"

val scalaTest = "org.scalatest"       %% "scalatest"  % "3.0.1"
val slick     = "com.typesafe.slick"  %% "slick"      % "3.2.0"
val slickPg   = "com.github.tminglei" %% "slick-pg"   % "0.15.0-RC"
val sprayJson = "io.spray"            %% "spray-json" % "1.3.3"
def playJson(scalaVersion: String) = {
  val version = CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, 12)) => "2.6.0-M4"
    case Some((2, 11)) => "2.5.12"
    case _ =>
      throw new IllegalArgumentException(s"Unsupported Scala version $scalaVersion")
  }

  "com.typesafe.play" %% "play-json" % version
}

val enumeratumVersion = "1.5.8"
val enumeratum        = "com.beachape" %% "enumeratum" % enumeratumVersion
def enumeratumInExamples = {
  val playJsonSupport = "com.beachape" %% "enumeratum-play-json" % enumeratumVersion
  Seq(enumeratum, playJsonSupport)
}
val optionalEnumeratum = optional(enumeratum)

val akkaHttpVersion = "10.0.4"
def akkaHttpInExamples = {
  val akkaHttp          = "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion
  val akkaHttpSprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion

  Seq(akkaHttp, akkaHttpSprayJson)
}
def akkaHttpInBenchmarks = {
  val akkaHttpTestkit = "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion
  akkaHttpInExamples :+ akkaHttpTestkit
}

lazy val commonSettings = baseSettings ++ Seq(
    scalacOptions ++= Seq("-language:experimental.macros", "-optimise"),
    (scalacOptions in Test) ++= Seq("-Ymacro-debug-lite", "-Xlog-implicits"),
    libraryDependencies += scalaTest % "test"
  )

lazy val slickSettings = commonSettings ++ Seq(
    libraryDependencies += slickPg % "test",
    libraryDependencies += optionalEnumeratum
  )

lazy val macroUtilsSettings = commonMacroSettings ++ Seq(
    libraryDependencies += optionalEnumeratum
  )

lazy val slickMacroSettings = commonMacroSettings ++ Seq(
    libraryDependencies += slick,
    libraryDependencies += optionalEnumeratum
  )

lazy val sprayJsonMacroSettings = commonMacroSettings ++ Seq(
    libraryDependencies += sprayJson,
    libraryDependencies += optionalEnumeratum
  )

lazy val sprayJsonSettings = commonSettings ++ Seq(
    libraryDependencies += optionalEnumeratum
  )

lazy val playJsonMacroSettings = commonMacroSettings ++ Seq(
    libraryDependencies += playJson(scalaVersion.value)
  )

lazy val playJsonSettings = commonSettings

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

lazy val slickMacros = project
  .in(file("slick-macros"))
  .dependsOn(macroUtils)
  .settings(slickMacroSettings: _*)
  .settings(crossBuildSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "slick-macros",
    description := "Macros supporting Kebs library",
    moduleName := "kebs-slick-macros"
  )

lazy val slickSupport = project
  .in(file("slick"))
  .dependsOn(slickMacros)
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

lazy val playJsonMacros = project
  .in(file("play-json-macros"))
  .dependsOn(macroUtils)
  .settings(playJsonMacroSettings: _*)
  .settings(crossBuildSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "play-json-macros",
    description := "Automatic generation of Play json formats for case-classes - macros",
    moduleName := "kebs-play-json-macros"
  )

lazy val playJsonSupport = project
  .in(file("play-json"))
  .dependsOn(playJsonMacros)
  .settings(playJsonSettings: _*)
  .settings(crossBuildSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "play-json",
    description := "Automatic generation of Play json formats for case-classes",
    moduleName := "kebs-play-json"
  )

lazy val examples = project
  .in(file("examples"))
  .dependsOn(slickSupport, sprayJsonSupport, playJsonSupport)
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
  .aggregate(macroUtils, slickMacros, slickSupport, sprayJsonMacros, sprayJsonSupport, playJsonMacros, playJsonSupport, examples)
  .settings(baseSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    name := "kebs",
    description := "Library to eliminate the boilerplate code that comes with the use of Slick"
  )
