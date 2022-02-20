name := """digitalpanda-backend-play"""
organization := "org.digitalpanda"
maintainer := "joel.vallone@gmail.com"

version := "0.1.0"

lazy val root = (project in file("."))
    .enablePlugins(PlayScala)
    // Use sbt default layout
    .disablePlugins(PlayLayoutPlugin)

scalaVersion := "2.13.8"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "org.digitalpanda.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "org.digitalpanda.binders._"
