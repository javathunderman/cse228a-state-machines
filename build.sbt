ThisBuild / scalaVersion     := "2.13.14"
ThisBuild / version          := "0.4.0"
ThisBuild / organization     := "com.github.javathunderman"

val chiselVersion = "3.6.1"
lazy val compiler = project.in(file("compiler"))
lazy val hw = (project in file("."))
  .dependsOn(compiler)
  .settings(
    name := "hw",
    Test / sourceGenerators += Def.task {
      val outputDir = (Test / sourceManaged).value / "generated"
      compiler.FSMCompilerInstrument.generate(outputDir)
      Seq(outputDir / "_test_unopt.scala")
    }.taskValue,
    libraryDependencies ++= Seq(
      "edu.berkeley.cs" %% "chisel3" % chiselVersion,
      "edu.berkeley.cs" %% "chiseltest" % "0.6.2" % "test"
    ),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-Xcheckinit",
      "-P:chiselplugin:genBundleElements",
    ),
    addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % chiselVersion cross CrossVersion.full),
  )
lazy val root = (project in file("."))
  .aggregate(compiler, hw)
  .settings(
    name := "cse228a-state-machines"
  )


libraryDependencies += "org.scalatestplus" %% "junit-4-13" % "3.2.15.0" % "test"
