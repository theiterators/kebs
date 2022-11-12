import sbt.librarymanagement.ConflictWarning

val scala_2_12             = "2.12.16"
val scala_2_13             = "2.13.8"
val scala_32               = "3.2.0"
val mainScalaVersion       = scala_32
val supportedScalaVersions = Seq(scala_2_12, scala_2_13, scala_32)

ThisBuild / crossScalaVersions := supportedScalaVersions
ThisBuild / scalaVersion := mainScalaVersion
ThisBuild / conflictWarning := ConflictWarning.disable


lazy val baseSettings = Seq(
  organization := "pl.iterators",
  organizationName := "Iterators",
  organizationHomepage := Some(url("https://iterato.rs")),
  homepage := Some(url("https://github.com/theiterators/kebs")),
  scalacOptions := Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8")
)

lazy val commonMacroSettings = baseSettings ++ Seq(
  libraryDependencies ++= (if (scalaVersion.value.startsWith("3")) Nil
                           else
                             Seq("org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
                                 "org.scala-lang" % "scala-reflect"  % scalaVersion.value))
)

lazy val metaSettings = commonSettings ++ Seq(
  scalacOptions ++= paradiseFlag(scalaVersion.value),
  libraryDependencies ++= paradisePlugin(scalaVersion.value)
)

lazy val crossBuildSettings = Seq(crossScalaVersions := supportedScalaVersions)

lazy val publishSettings = Seq(
  pomIncludeRepository := const(true),
  licenses := Seq("MIT License" -> url("http://opensource.org/licenses/MIT")),
  developers := List(
    Developer(id = "mrzeznicki",
              name = "Marcin Rzeźnicki",
              email = "mrzeznicki@iterato.rs",
              url = url("https://github.com/marcin-rzeznicki")),
    Developer(id = "jborkowski", name = "Jonatan Borkowski", email = "jborkowski@iterato.rs", url = url("https://github.com/jborkowski")),
    Developer(id = "pkiersznowski",
              name = "Paweł Kiersznowski",
              email = "pkiersznowski@iteratorshq.com",
              url = url("https://github.com/pk044"))
  ),
  scmInfo := Some(
    ScmInfo(browseUrl = url("https://github.com/theiterators/kebs"), connection = "scm:git:https://github.com/theiterators/kebs.git")),
) ++ crossBuildSettings

lazy val noPublishSettings =
  Seq(
    publishArtifact := false,
  )

def disableScala(v: String) = Def.settings(
  libraryDependencies := {
    if (scalaBinaryVersion.value == v) {
      Nil
    } else {
      libraryDependencies.value
    }
  },
  Seq(Compile, Test).map { x =>
    (x / sources) := {
      if (scalaBinaryVersion.value == v) {
        Nil
      } else {
        (x / sources).value
      }
    }
  },
  Test / test := {
    if (scalaBinaryVersion.value == v) {
      ()
    } else {
      (Test / test).value
    }
  },
  publish / skip := (scalaBinaryVersion.value == v)
)

def optional(dependency: ModuleID) = dependency % "provided"
def sv[A](scalaVersion: String, scala2_12Version: => A, scala2_13Version: => A) =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, 13)) => scala2_13Version
    case Some((2, 12)) => scala2_12Version
    case _ =>
      throw new IllegalArgumentException(s"Unsupported Scala version $scalaVersion")
  }

def paradiseFlag(scalaVersion: String): Seq[String] =
  if (scalaVersion == scala_2_12)
    Seq.empty
  else
    Seq("-Ymacro-annotations")

def paradisePlugin(scalaVersion: String): Seq[ModuleID] =
  if (scalaVersion == scala_2_12)
    Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full))
  else
    Seq.empty

val scalaTest       = "org.scalatest" %% "scalatest" % "3.2.14"
val scalaCheck      = "org.scalacheck" %% "scalacheck" % "1.17.0"
val slick           = "com.typesafe.slick" %% "slick" % "3.4.1"
val optionalSlick   = optional(slick)
val playJson        = "com.typesafe.play" %% "play-json" % "2.9.3"
val slickPg         = "com.github.tminglei" %% "slick-pg" % "0.21.0"
val doobie          = "org.tpolecat" %% "doobie-core" % "1.0.0-RC2"
val doobiePg        = "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC2"
val sprayJson       = "io.spray" %% "spray-json" % "1.3.6"
val circe           = "io.circe" %% "circe-core" % "0.14.3"
val circeAuto       = "io.circe" %% "circe-generic" % "0.14.3"
val circeAutoExtras = "io.circe" %% "circe-generic-extras" % "0.14.3"
val circeParser     = "io.circe" %% "circe-parser" % "0.14.3"
val optionalCirce   = optional(circe)

val jsonschema = "com.github.andyglow" %% "scala-jsonschema" % "0.7.9"

val scalacheck           = "org.scalacheck"             %% "scalacheck"                % "1.17.0" % "test"
val scalacheckShapeless  = "com.github.alexarchambault" %% "scalacheck-shapeless_1.15" % "1.3.0"
val scalacheckEnumeratum = "com.beachape"               %% "enumeratum-scalacheck"     % "1.7.0"

val enumeratumVersion         = "1.7.0"
val enumeratumPlayJsonVersion = "1.5.16"
val enumeratum                = "com.beachape" %% "enumeratum" % enumeratumVersion
def enumeratumInExamples = {
  val playJsonSupport = "com.beachape" %% "enumeratum-play-json" % enumeratumPlayJsonVersion
  Seq(enumeratum.cross(CrossVersion.for3Use2_13), playJsonSupport.cross(CrossVersion.for3Use2_13))
}
val optionalEnumeratum = optional(enumeratum.cross(CrossVersion.for3Use2_13))

val akkaVersion       = "2.6.19"
val akkaHttpVersion   = "10.2.10"
val akkaStream        = "com.typesafe.akka" %% "akka-stream" % akkaVersion
val akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion
val akkaHttp          = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
val akkaHttpTestkit   = "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion
def akkaHttpInExamples = {
  val akkaHttpSprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
  Seq(akkaStream.cross(CrossVersion.for3Use2_13),
      akkaHttp.cross(CrossVersion.for3Use2_13),
      akkaHttpSprayJson.cross(CrossVersion.for3Use2_13))
}

val http4sVersion = "0.23.16"
val http4s = "org.http4s" %% "http4s-dsl" % http4sVersion

def akkaHttpInBenchmarks = akkaHttpInExamples :+ (akkaHttpTestkit).cross(CrossVersion.for3Use2_13)

lazy val commonSettings = baseSettings ++ Seq(
  scalacOptions ++=
    (if (scalaVersion.value.startsWith("3"))
       Seq("-language:implicitConversions", "-Ykind-projector", "-Xignore-scala2-macros")
     else Seq("-language:implicitConversions", "-language:experimental.macros")),
//  (scalacOptions in Test) ++= Seq("-Ymacro-debug-lite" /*, "-Xlog-implicits"*/ ),
  libraryDependencies += scalaTest % "test"
)

lazy val slickSettings = commonSettings ++ Seq(
  libraryDependencies += slick.cross(CrossVersion.for3Use2_13),
  libraryDependencies += (slickPg % "test").cross(CrossVersion.for3Use2_13),
  libraryDependencies += optionalEnumeratum.cross(CrossVersion.for3Use2_13)
)

lazy val doobieSettings = commonSettings ++ Seq(
  libraryDependencies += doobie,
  libraryDependencies += (doobiePg % "test"),
  libraryDependencies += optionalEnumeratum.cross(CrossVersion.for3Use2_13),
)

lazy val macroUtilsSettings = commonMacroSettings ++ Seq(
  libraryDependencies += (scalaCheck % "test").cross(CrossVersion.for3Use2_13),
  libraryDependencies += optionalEnumeratum
)

lazy val sprayJsonMacroSettings = commonMacroSettings ++ Seq(
  libraryDependencies += sprayJson.cross(CrossVersion.for3Use2_13)
)

lazy val sprayJsonSettings = commonSettings ++ Seq(
  libraryDependencies += optionalEnumeratum.cross(CrossVersion.for3Use2_13)
)

lazy val playJsonSettings = commonSettings ++ Seq(
  libraryDependencies += playJson.cross(CrossVersion.for3Use2_13)
)

lazy val circeSettings = commonSettings ++ Seq(
  libraryDependencies += circe,
  libraryDependencies += circeAuto,
  libraryDependencies += circeAutoExtras.cross(CrossVersion.for3Use2_13),
  libraryDependencies += optionalEnumeratum.cross(CrossVersion.for3Use2_13),
  libraryDependencies += circeParser % "test"
)

lazy val akkaHttpSettings = commonSettings ++ Seq(
  libraryDependencies += (akkaHttp).cross(CrossVersion.for3Use2_13),
  libraryDependencies += (akkaStreamTestkit % "test").cross(CrossVersion.for3Use2_13),
  libraryDependencies += (akkaHttpTestkit   % "test").cross(CrossVersion.for3Use2_13),
  libraryDependencies += optionalEnumeratum.cross(CrossVersion.for3Use2_13),
  libraryDependencies ++= paradisePlugin(scalaVersion.value),
  scalacOptions ++= paradiseFlag(scalaVersion.value)
)

lazy val http4sSettings = commonSettings ++ Seq(
  libraryDependencies += http4s,
  libraryDependencies += optionalEnumeratum.cross(CrossVersion.for3Use2_13),
  libraryDependencies ++= paradisePlugin(scalaVersion.value),
  scalacOptions ++= paradiseFlag(scalaVersion.value)
)

lazy val jsonschemaSettings = commonSettings ++ Seq(
  libraryDependencies += jsonschema.cross(CrossVersion.for3Use2_13)
)

lazy val scalacheckSettings = commonSettings ++ Seq(
  libraryDependencies += scalacheck.cross(CrossVersion.for3Use2_13),
  libraryDependencies += scalacheckEnumeratum.cross(CrossVersion.for3Use2_13),
  libraryDependencies += scalacheckShapeless.cross(CrossVersion.for3Use2_13)
)

lazy val taggedSettings = commonSettings ++ Seq(
  libraryDependencies += optionalSlick.cross(CrossVersion.for3Use2_13),
  libraryDependencies += optionalCirce
)

lazy val opaqueSettings = commonSettings

lazy val examplesSettings = commonSettings ++ Seq(
  libraryDependencies += slickPg.cross(CrossVersion.for3Use2_13),
  libraryDependencies += circeParser,
  libraryDependencies ++= enumeratumInExamples,
  libraryDependencies ++= akkaHttpInExamples,
  libraryDependencies ++= paradisePlugin(scalaVersion.value),
  scalacOptions ++= paradiseFlag(scalaVersion.value)
)

lazy val benchmarkSettings = commonSettings ++ Seq(
  libraryDependencies += scalaTest,
  libraryDependencies += enumeratum.cross(CrossVersion.for3Use2_13),
  libraryDependencies ++= akkaHttpInBenchmarks
)

lazy val taggedMetaSettings = metaSettings ++ Seq(
  libraryDependencies += optional(sprayJson.cross(CrossVersion.for3Use2_13)),
  libraryDependencies += optional(circe)
)

lazy val instancesSettings = commonSettings

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
  .dependsOn(macroUtils, instances)
  .settings(slickSettings: _*)
  .settings(publishSettings: _*)
  .settings(disableScala("3"))
  .settings(
    name := "slick",
    description := "Library to eliminate the boilerplate code that comes with the use of Slick",
    moduleName := "kebs-slick",
    crossScalaVersions := supportedScalaVersions
  )

lazy val doobieSupport = project
  .in(file("doobie"))
  .dependsOn(instances, opaque)
  .settings(doobieSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "doobie",
    description := "Library to eliminate the boilerplate code that comes with the use of Doobie",
    moduleName := "kebs-doobie",
    crossScalaVersions := supportedScalaVersions
  )

lazy val sprayJsonMacros = project
  .in(file("spray-json-macros"))
  .dependsOn(macroUtils)
  .settings(sprayJsonMacroSettings: _*)
  .settings(publishSettings: _*)
  .settings(disableScala("3"))
  .settings(
    name := "spray-json-macros",
    description := "Automatic generation of Spray json formats for case-classes - macros",
    moduleName := "kebs-spray-json-macros",
    crossScalaVersions := supportedScalaVersions
  )

lazy val sprayJsonSupport = project
  .in(file("spray-json"))
  .dependsOn(sprayJsonMacros, instances)
  .settings(sprayJsonSettings: _*)
  .settings(publishSettings: _*)
  .settings(disableScala("3"))
  .settings(
    name := "spray-json",
    description := "Automatic generation of Spray json formats for case-classes",
    moduleName := "kebs-spray-json",
    crossScalaVersions := supportedScalaVersions
  )

lazy val playJsonSupport = project
  .in(file("play-json"))
  .dependsOn(macroUtils, instances)
  .settings(playJsonSettings: _*)
  .settings(publishSettings: _*)
  .settings(disableScala("3"))
  .settings(
    name := "play-json",
    description := "Automatic generation of Play json formats for case-classes",
    moduleName := "kebs-play-json",
    crossScalaVersions := supportedScalaVersions
  )

lazy val circeSupport = project
  .in(file("circe"))
  .dependsOn(macroUtils, instances)
  .settings(circeSettings: _*)
  .settings(crossBuildSettings: _*)
  .settings(publishSettings: _*)
  .settings(disableScala("3"))
  .settings(
    name := "circe",
    description := "Automatic generation of circe formats for case-classes",
    moduleName := "kebs-circe"
  )

lazy val akkaHttpSupport = project
  .in(file("akka-http"))
  .dependsOn(macroUtils, instances, tagged % "test -> test", taggedMeta % "test -> test")
  .settings(akkaHttpSettings: _*)
  .settings(publishSettings: _*)
  .settings(disableScala("3"))
  .settings(
    name := "akka-http",
    description := "Automatic generation of akka-http deserializers for 1-element case classes",
    moduleName := "kebs-akka-http",
    crossScalaVersions := supportedScalaVersions
  )

lazy val http4sSupport = project
  .in(file("http4s"))
  .dependsOn(macroUtils, instances, opaque % "test -> test", tagged % "test -> test", taggedMeta % "test -> test")
  .settings(http4sSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "http4s",
    description := "Automatic generation of http4s deserializers for 1-element case classes, opaque and tagged types",
    moduleName := "kebs-http4s",
    crossScalaVersions := supportedScalaVersions
  )

lazy val jsonschemaSupport = project
  .in(file("jsonschema"))
  .dependsOn(macroUtils)
  .settings(jsonschemaSettings: _*)
  .settings(publishSettings: _*)
  .settings(disableScala("3"))
  .settings(
    name := "jsonschema",
    description := "Automatic generation of JSON Schemas for case classes",
    moduleName := "kebs-jsonschema",
    crossScalaVersions := supportedScalaVersions
  )

lazy val scalacheckSupport = project
  .in(file("scalacheck"))
  .dependsOn(macroUtils)
  .settings(scalacheckSettings: _*)
  .settings(publishSettings: _*)
  .settings(disableScala("3"))
  .settings(
    name := "scalacheck",
    description := "Automatic generation of scalacheck generators for case classes",
    moduleName := "kebs-scalacheck",
    crossScalaVersions := supportedScalaVersions
  )

lazy val tagged = project
  .in(file("tagged"))
  .dependsOn(macroUtils)
  .settings(taggedSettings: _*)
  .settings(publishSettings: _*)
  .settings(disableScala("3"))
  .settings(
    name := "tagged",
    description := "Representation of tagged types",
    moduleName := "kebs-tagged",
    crossScalaVersions := supportedScalaVersions
  )

lazy val opaque = project
  .in(file("opaque"))
  .dependsOn(macroUtils)
  .settings(opaqueSettings: _*)
  .settings(disableScala("2.13"))
  .settings(disableScala("2.12"))
  .settings(publishSettings: _*)
  .settings(
    name := "opaque",
    description := "Representation of opaque types",
    moduleName := "kebs-opaque",
    crossScalaVersions := supportedScalaVersions,
    publish / skip := (scalaBinaryVersion.value == "2.13")
 )

lazy val taggedMeta = project
  .in(file("tagged-meta"))
  .dependsOn(
    macroUtils,
    tagged,
    sprayJsonSupport  % "test -> test",
    circeSupport      % "test -> test",
    jsonschemaSupport % "test -> test",
    scalacheckSupport % "test -> test"
  )
  .settings(taggedMetaSettings: _*)
  .settings(publishSettings: _*)
  .settings(disableScala("3"))
  .settings(
    name := "tagged-meta",
    description := "Representation of tagged types - code generation based on scala-meta",
    moduleName := "kebs-tagged-meta",
    crossScalaVersions := supportedScalaVersions
  )

lazy val examples = project
  .in(file("examples"))
  .dependsOn(slickSupport, sprayJsonSupport, playJsonSupport, akkaHttpSupport, taggedMeta, circeSupport, instances)
  .settings(examplesSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(disableScala("3"))
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

lazy val instances = project
  .in(file("instances"))
  .settings(instancesSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "instances",
    description := "Standard type mappings",
    moduleName := "kebs-instances"
  )

lazy val kebs = project
  .in(file("."))
  .aggregate(
    tagged,
    opaque,
    macroUtils,
    slickSupport,
    doobieSupport,
    sprayJsonMacros,
    sprayJsonSupport,
    playJsonSupport,
    circeSupport,
    jsonschemaSupport,
    scalacheckSupport,
    akkaHttpSupport,
    http4sSupport,
    taggedMeta,
    instances
  )
  .settings(baseSettings: _*)
  .settings(noPublishSettings)
  .settings(
    name := "kebs",
    description := "Library to eliminate the boilerplate code"
  )
