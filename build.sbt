import sbt.librarymanagement.ConflictWarning

val scala_2_13             = "2.13.11"
val scala_3                = "3.3.0"
val mainScalaVersion       = scala_3
val supportedScalaVersions = Seq(scala_2_13, scala_3)

ThisBuild / crossScalaVersions := supportedScalaVersions
ThisBuild / scalaVersion := mainScalaVersion
ThisBuild / conflictWarning := ConflictWarning.disable


lazy val baseSettings = Seq(
  organization := "pl.iterators",
  organizationName := "Iterators",
  organizationHomepage := Some(url("https://iterato.rs")),
  homepage := Some(url("https://github.com/theiterators/kebs")),
  scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8")
)

lazy val commonMacroSettings = baseSettings ++ Seq(
  libraryDependencies ++= (if (scalaVersion.value.startsWith("3")) Nil
                           else
                             Seq("org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
                                 "org.scala-lang" % "scala-reflect"  % scalaVersion.value))
)

lazy val metaSettings = commonSettings ++ Seq(
  scalacOptions ++= paradiseFlag(scalaVersion.value),
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

def disableScala(v: List[String]) =
  Def.settings(
  libraryDependencies := {
    if (v.contains(scalaBinaryVersion.value)) {
      Nil
    } else {
      libraryDependencies.value
    }
  },
  Seq(Compile, Test).map { x =>
    (x / sources) := {
      if (v.contains(scalaBinaryVersion.value)) {
        Nil
      } else {
        (x / sources).value
      }
    }
  },
  Test / test := {
    if (v.contains(scalaBinaryVersion.value)) {
      ()
    } else {
      (Test / test).value
    }
  },
  publish / skip := (v.contains(scalaBinaryVersion.value)))


def optional(dependency: ModuleID) = dependency % "provided"
def sv[A](scalaVersion: String, scala2_12Version: => A, scala2_13Version: => A) =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, 13)) => scala2_13Version
    case Some((2, 12)) => scala2_12Version
    case _ =>
      throw new IllegalArgumentException(s"Unsupported Scala version $scalaVersion")
  }

def paradiseFlag(scalaVersion: String): Seq[String] =
  if (scalaVersion == scala_3)
    Seq.empty
  else
    Seq("-Ymacro-annotations")

val scalaTest       = Def.setting("org.scalatest" %%% "scalatest" % "3.2.16")
val scalaCheck      = Def.setting("org.scalacheck" %%% "scalacheck" % "1.17.0")
val slick           = "com.typesafe.slick" %% "slick" % "3.4.1"
val optionalSlick   = optional(slick)
val playJson        = "com.typesafe.play" %% "play-json" % "2.9.4"
val slickPg         = "com.github.tminglei" %% "slick-pg" % "0.21.1"
val doobie          = "org.tpolecat" %% "doobie-core" % "1.0.0-RC4"
val doobiePg        = "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC4"
val sprayJson       = "io.spray" %% "spray-json" % "1.3.6"
val circeV = "0.14.5"
val circe           = "io.circe" %% "circe-core" % circeV
val circeAuto       = "io.circe" %% "circe-generic" % circeV
val circeAutoExtras = "io.circe" %% "circe-generic-extras" % "0.14.3"
val circeParser     = "io.circe" %% "circe-parser" % circeV

val jsonschema = "com.github.andyglow" %% "scala-jsonschema" % "0.7.11"

val scalacheck           = "org.scalacheck"             %% "scalacheck"                % "1.17.0" % "test"
val scalacheckShapeless  = "com.github.alexarchambault" %% "scalacheck-shapeless_1.15" % "1.3.0"
val scalacheckDerived  = "io.github.martinhh" %% "scalacheck-derived" % "0.4.1"
val scalacheckEnumeratum = "com.beachape"               %% "enumeratum-scalacheck"     % "1.7.3"

val enumeratumVersion         = "1.7.3"
val enumeratumPlayJsonVersion = "1.5.16"
val enumeratum                = "com.beachape" %% "enumeratum" % enumeratumVersion
def enumeratumInExamples = {
  val playJsonSupport = "com.beachape" %% "enumeratum-play-json" % enumeratumPlayJsonVersion
  Seq(enumeratum, playJsonSupport.cross(CrossVersion.for3Use2_13))
}
val optionalEnumeratum = optional(enumeratum)

val akkaVersion       = "2.6.20"
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

val pekkoVersion       = "1.0.1"
val pekkoHttpVersion   = "1.0.0"
val pekkoHttpJsonV     = "2.0.0"
val pekkoStream        = "org.apache.pekko" %% "pekko-stream" % pekkoVersion
val pekkoStreamTestkit = "org.apache.pekko" %% "pekko-stream-testkit" % pekkoVersion
val pekkoHttp          = "org.apache.pekko" %% "pekko-http" % pekkoHttpVersion
val pekkoHttpTestkit   = "org.apache.pekko" %% "pekko-http-testkit" % pekkoHttpVersion

def pekkoHttpInExamples = {
  val pekkoHttpSprayJson = "org.apache.pekko" %% "pekko-http-spray-json" % pekkoHttpVersion
  Seq(pekkoStream, pekkoHttp, pekkoHttpSprayJson)
}

val http4sVersion = "0.23.23"
val http4s = "org.http4s" %% "http4s-dsl" % http4sVersion

val http4sStirVersion = "0.2"
val http4sStir = "pl.iterators" %% "http4s-stir" % http4sStirVersion
val http4sStirTestkit = "pl.iterators" %% "http4s-stir-testkit" % http4sStirVersion

def akkaHttpInBenchmarks = akkaHttpInExamples :+ (akkaHttpTestkit).cross(CrossVersion.for3Use2_13)

def pekkoHttpInBenchmarks = pekkoHttpInExamples :+ pekkoHttpTestkit

lazy val commonSettings = baseSettings ++ Seq(
  scalacOptions ++=
    (if (scalaVersion.value.startsWith("3"))
       Seq("-language:implicitConversions", "-Ykind-projector", "-Xignore-scala2-macros")
     else Seq("-language:implicitConversions", "-language:experimental.macros")),
//  (scalacOptions in Test) ++= Seq("-Ymacro-debug-lite" /*, "-Xlog-implicits"*/ ),
  libraryDependencies += scalaTest.value % "test"
)

lazy val slickSettings = commonSettings ++ Seq(
  libraryDependencies += slick.cross(CrossVersion.for3Use2_13),
  libraryDependencies += (slickPg % "test").cross(CrossVersion.for3Use2_13),
  libraryDependencies += optionalEnumeratum
)

lazy val doobieSettings = commonSettings ++ Seq(
  libraryDependencies += doobie,
  libraryDependencies += (doobiePg % "test"),
  libraryDependencies += optionalEnumeratum,
)

lazy val coreSettings = commonMacroSettings ++ Seq(
  libraryDependencies += (scalaCheck.value % "test").cross(CrossVersion.for3Use2_13),
  libraryDependencies += optionalEnumeratum
)

lazy val enumSettings = commonMacroSettings ++ Seq(
  libraryDependencies += scalaCheck.value % "test",
  libraryDependencies += scalaTest.value,
  libraryDependencies += optionalEnumeratum,
  scalacOptions ++= paradiseFlag(scalaVersion.value)
)

lazy val enumeratumSettings = commonMacroSettings ++ Seq(
  libraryDependencies += scalaCheck.value % "test",
  libraryDependencies += scalaTest.value,
  libraryDependencies += optionalEnumeratum,
  scalacOptions ++= paradiseFlag(scalaVersion.value) ++ (if (scalaVersion.value.startsWith("3")) Seq("-Yretain-trees") else Seq.empty)
)

lazy val sprayJsonMacroSettings = commonMacroSettings ++ Seq(
  libraryDependencies += sprayJson.cross(CrossVersion.for3Use2_13)
)

lazy val sprayJsonSettings = commonSettings ++ Seq(
  libraryDependencies += optionalEnumeratum
)

lazy val playJsonSettings = commonSettings ++ Seq(
  libraryDependencies += playJson.cross(CrossVersion.for3Use2_13)
)

lazy val circeSettings = commonSettings ++ Seq(
  libraryDependencies += circe,
  libraryDependencies += circeAuto,
  libraryDependencies += optionalEnumeratum,
  libraryDependencies += (circeParser % "test")
) ++ Seq(
  libraryDependencies ++= (if (scalaVersion.value.startsWith("3")) Nil
  else Seq(circeAutoExtras)))

lazy val akkaHttpSettings = commonSettings ++ Seq(
  libraryDependencies += (akkaHttp).cross(CrossVersion.for3Use2_13),
  libraryDependencies += (akkaStreamTestkit % "test").cross(CrossVersion.for3Use2_13),
  libraryDependencies += (akkaHttpTestkit   % "test").cross(CrossVersion.for3Use2_13),
  libraryDependencies += optionalEnumeratum,
  scalacOptions ++= paradiseFlag(scalaVersion.value)
)

lazy val pekkoHttpSettings = commonSettings ++ Seq(
  libraryDependencies += pekkoHttp,
  libraryDependencies += pekkoStream,
  libraryDependencies += pekkoStreamTestkit % "test",
  libraryDependencies += pekkoHttpTestkit   % "test",
  libraryDependencies += optionalEnumeratum,
  scalacOptions ++= paradiseFlag(scalaVersion.value)
)

lazy val http4sSettings = commonSettings ++ Seq(
  libraryDependencies += http4s,
  libraryDependencies += optionalEnumeratum,
  scalacOptions ++= paradiseFlag(scalaVersion.value)
)

lazy val http4sStirSettings = commonSettings ++ Seq(
  libraryDependencies += http4s,
  libraryDependencies += http4sStir,
  libraryDependencies += http4sStirTestkit % "test",
  libraryDependencies += optionalEnumeratum,
  scalacOptions ++= paradiseFlag(scalaVersion.value)
)

lazy val jsonschemaSettings = commonSettings ++ Seq(
  libraryDependencies += jsonschema.cross(CrossVersion.for3Use2_13)
)

lazy val scalacheckSettings = commonSettings ++ Seq(
  libraryDependencies += scalacheck.cross(CrossVersion.for3Use2_13),
  libraryDependencies += scalacheckEnumeratum.cross(CrossVersion.for3Use2_13),
  libraryDependencies += scalacheckShapeless.cross(CrossVersion.for3Use2_13),
) ++ Seq(
  libraryDependencies ++= (if (scalaVersion.value.startsWith("3")) Seq(scalacheckDerived)
  else Nil))

lazy val taggedSettings = commonSettings ++ Seq(
  libraryDependencies += optionalSlick.cross(CrossVersion.for3Use2_13),
  libraryDependencies += optional(circe)
)

lazy val opaqueSettings = commonSettings

lazy val examplesSettings = commonSettings ++ Seq(
  libraryDependencies += slickPg.cross(CrossVersion.for3Use2_13),
  libraryDependencies += circeParser,
  libraryDependencies ++= enumeratumInExamples,
  libraryDependencies ++= pekkoHttpInExamples,
  scalacOptions ++= paradiseFlag(scalaVersion.value)
)

lazy val benchmarkSettings = commonSettings ++ Seq(
  libraryDependencies += scalaTest.value,
  libraryDependencies ++= pekkoHttpInBenchmarks,
  libraryDependencies += enumeratum,
  libraryDependencies ++= akkaHttpInBenchmarks
)

lazy val taggedMetaSettings = metaSettings ++ Seq(
  libraryDependencies += optional(sprayJson.cross(CrossVersion.for3Use2_13)),
  libraryDependencies += optional(circe)
)

lazy val instancesSettings = commonSettings

lazy val macroUtilsSettings = coreSettings ++ Seq(
  Compile / scalaSource := baseDirectory.value  / ".." / ".." / "core" / "src" / "main" / "scala",
  Test / scalaSource := baseDirectory.value / ".." / ".." / "core" / "src" / "test" / "scala"
)

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(coreSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "core",
    description := "Macros and utils supporting Kebs library",
    moduleName := "kebs-core",
)

lazy val macroUtils = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("macro-utils"))
  .settings(macroUtilsSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "macro-utils",
    description := "Macros and utils supporting Kebs library",
    moduleName := "kebs-macro-utils",
  )

lazy val slickSupport = project
  .in(file("slick"))
  .dependsOn(core.jvm, instances % "test -> test")
  .settings(slickSettings: _*)
  .settings(publishSettings: _*)
  .settings(disableScala(List("3")))
  .settings(
    name := "slick",
    description := "Library to eliminate the boilerplate code that comes with the use of Slick",
    moduleName := "kebs-slick",
    crossScalaVersions := supportedScalaVersions
  )

lazy val doobieSupport = project
  .in(file("doobie"))
  .dependsOn(instances, opaque.jvm % "test -> test")
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
  .dependsOn(core.jvm)
  .settings(sprayJsonMacroSettings: _*)
  .settings(publishSettings: _*)
  .settings(disableScala(List("3")))
  .settings(
    name := "spray-json-macros",
    description := "Automatic generation of Spray json formats for case-classes - macros",
    moduleName := "kebs-spray-json-macros",
    crossScalaVersions := supportedScalaVersions
  )

lazy val sprayJsonSupport = project
  .in(file("spray-json"))
  .dependsOn(sprayJsonMacros, instances % "test -> test")
  .settings(sprayJsonSettings: _*)
  .settings(publishSettings: _*)
  .settings(disableScala(List("3")))
  .settings(
    name := "spray-json",
    description := "Automatic generation of Spray json formats for case-classes",
    moduleName := "kebs-spray-json",
    crossScalaVersions := supportedScalaVersions
  )

lazy val playJsonSupport = project
  .in(file("play-json"))
  .dependsOn(core.jvm, instances % "test -> test")
  .settings(playJsonSettings: _*)
  .settings(publishSettings: _*)
  .settings(disableScala(List("3")))
  .settings(
    name := "play-json",
    description := "Automatic generation of Play json formats for case-classes",
    moduleName := "kebs-play-json",
    crossScalaVersions := supportedScalaVersions
  )

lazy val circeSupport = project
  .in(file("circe"))
  .dependsOn(core.jvm, instances % "test -> test")
  .settings(circeSettings: _*)
  .settings(crossBuildSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "circe",
    description := "Automatic generation of circe formats for case-classes",
    moduleName := "kebs-circe"
  )

lazy val akkaHttpSupport = project
  .in(file("akka-http"))
  .dependsOn(core.jvm, instances % "test -> test", tagged.jvm % "test -> test", taggedMeta % "test -> test")
  .settings(akkaHttpSettings: _*)
  .settings(publishSettings: _*)
  .settings(disableScala(List("3")))
  .settings(
    name := "akka-http",
    description := "Automatic generation of akka-http deserializers for 1-element case classes",
    moduleName := "kebs-akka-http",
    crossScalaVersions := supportedScalaVersions
  )

lazy val pekkoHttpSupport = project
  .in(file("pekko-http"))
  .dependsOn(core.jvm, instances % "test -> test", tagged.jvm % "test -> test", taggedMeta % "test -> test")
  .settings(pekkoHttpSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "pekko-http",
    description := "Automatic generation of pekko-http deserializers for 1-element case classes",
    moduleName := "kebs-pekko-http",
    crossScalaVersions := supportedScalaVersions
  )

lazy val http4sSupport = project
  .in(file("http4s"))
  .dependsOn(core.jvm, instances, opaque.jvm % "test -> test", tagged.jvm % "test -> test", taggedMeta % "test -> test")
  .settings(http4sSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "http4s",
    description := "Automatic generation of http4s deserializers for 1-element case classes, opaque and tagged types",
    moduleName := "kebs-http4s",
    crossScalaVersions := supportedScalaVersions
  )


lazy val http4sStirSupport = project
  .in(file("http4s-stir"))
  .dependsOn(core.jvm, instances, opaque.jvm % "test -> test", tagged.jvm % "test -> test", taggedMeta % "test -> test")
  .settings(http4sStirSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "http4s-stir",
    description := "Automatic generation of http4s-stir deserializers for 1-element case classes, opaque and tagged types",
    moduleName := "kebs-http4s-stir",
    crossScalaVersions := supportedScalaVersions
  )

lazy val jsonschemaSupport = project
  .in(file("jsonschema"))
  .dependsOn(core.jvm)
  .settings(jsonschemaSettings: _*)
  .settings(publishSettings: _*)
  .settings(disableScala(List("3")))
  .settings(
    name := "jsonschema",
    description := "Automatic generation of JSON Schemas for case classes",
    moduleName := "kebs-jsonschema",
    crossScalaVersions := supportedScalaVersions
  )

lazy val scalacheckSupport = project
  .in(file("scalacheck"))
  .dependsOn(core.jvm, opaque.jvm % "test -> test")
  .settings(scalacheckSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "scalacheck",
    description := "Automatic generation of scalacheck generators for case classes",
    moduleName := "kebs-scalacheck",
    crossScalaVersions := supportedScalaVersions
  )

lazy val tagged = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("tagged"))
  .dependsOn(core)
  .settings(taggedSettings: _*)
  .settings(publishSettings: _*)
  .settings(disableScala(List("3")))
  .settings(
    name := "tagged",
    description := "Representation of tagged types",
    moduleName := "kebs-tagged",
    crossScalaVersions := supportedScalaVersions
  )

lazy val opaque = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("opaque"))
  .dependsOn(core)
  .settings(opaqueSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "opaque",
    description := "Representation of opaque types",
    moduleName := "kebs-opaque",
    crossScalaVersions := supportedScalaVersions
 )
  .settings(disableScala(List("2.13")))

lazy val taggedMeta = project
  .in(file("tagged-meta"))
  .dependsOn(
    core.jvm,
    tagged.jvm,
    sprayJsonSupport  % "test -> test",
    circeSupport      % "test -> test",
    jsonschemaSupport % "test -> test",
    scalacheckSupport % "test -> test"
  )
  .settings(taggedMetaSettings: _*)
  .settings(publishSettings: _*)
  .settings(disableScala(List("3")))
  .settings(
    name := "tagged-meta",
    description := "Representation of tagged types - code generation based on scala-meta",
    moduleName := "kebs-tagged-meta",
    crossScalaVersions := supportedScalaVersions
  )

lazy val examples = project
  .in(file("examples"))
  .dependsOn(slickSupport, sprayJsonSupport, playJsonSupport, pekkoHttpSupport, taggedMeta, circeSupport, instances)
  .settings(examplesSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(disableScala(List("3")))
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
  .dependsOn(core.jvm)
  .settings(instancesSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "instances",
    description := "Standard type mappings",
    moduleName := "kebs-instances"
  )

lazy val enumSupport = project
  .in(file("enum"))
  .dependsOn(core.jvm)
  .settings(enumSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "enum",
    moduleName := "kebs-enum"
  )

lazy val enumeratumSupport = project
  .in(file("enumeratum"))
  .dependsOn(core.jvm)
  .settings(enumeratumSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "enumeratum",
    moduleName := "kebs-enumeratum"
  )


lazy val kebs = project
  .in(file("."))
  .aggregate(
    tagged.jvm,
    tagged.js,
    opaque.jvm,
    opaque.js,
    core.jvm,
    core.js,
    macroUtils.jvm,
    macroUtils.js,
    slickSupport,
    doobieSupport,
    sprayJsonMacros,
    sprayJsonSupport,
    playJsonSupport,
    circeSupport,
    jsonschemaSupport,
    scalacheckSupport,
    akkaHttpSupport,
    pekkoHttpSupport,
    http4sSupport,
    http4sStirSupport,
    taggedMeta,
    instances,
    enumSupport,
    enumeratumSupport
  )
  .settings(baseSettings: _*)
  .settings(noPublishSettings)
  .settings(
    name := "kebs",
    description := "Library to eliminate the boilerplate code"
  )
