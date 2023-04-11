logLevel := Level.Warn

addSbtPlugin("com.lucidchart"     % "sbt-scalafmt" % "1.16")
addSbtPlugin("pl.project13.scala" % "sbt-jmh"      % "0.4.4")
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.11")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.13.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.0")