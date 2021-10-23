import sbt._

object Configs {
  val FunctionalTest  = config("ft")    extend (Runtime)
  val AcceptanceTest  = config("at")    extend (Runtime)
  val PerformanceTest = config("pt")    extend (Runtime)
  val ToolsTest       = config("tools") extend (Runtime)
}
