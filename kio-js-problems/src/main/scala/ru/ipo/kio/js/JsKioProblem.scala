package ru.ipo.kio.js

import java.io.File
import java.net.{URL, URLClassLoader}
import javax.script.ScriptEngineManager

import scala.collection.JavaConverters._
import jdk.nashorn.api.scripting.ScriptObjectMirror

case class ExternalChecker(jar: File, className: String)

/**
  * Created by ilya on 28.03.17.
  */
class JsKioProblem(jsCode: String, className: String, settingsJson: String, externalChecker: ExternalChecker) extends Ordering[Result] {

  import JsKioProblem._

  private val scriptEngine = factory.getEngineByName("nashorn")

  val problem: ScriptObjectMirror = createProblem()
  val parameters: Seq[Parameter] = extractParameters()
  private val parametersView = parameters.view

  private def createProblem(): ScriptObjectMirror = {
    scriptEngine.eval(jsCode)
    scriptEngine.eval(s"new $className($settingsJson);").asInstanceOf[ScriptObjectMirror]
  }

  private def extractParameters(): Seq[Parameter] = {
    val rawParameters = problem.callMember("parameters").asInstanceOf[ScriptObjectMirror]
    val paramsCount: Int = rawParameters.getMember("length").asInstanceOf[Number].intValue()

    (0 until paramsCount).map { i =>
      val param = rawParameters.getSlot(i).asInstanceOf[ScriptObjectMirror]
      Parameter(
        param.getMember("name"),
        param.getMember("title"),
        param.getMember("ordering"),
        param.getMember("view"),
        param.getMember("normalize")
      )
    }
  }

  override def compare(x: Result, y: Result): Int =
    parametersView.map(p => p.compare(x, y)).find(_ != 0).getOrElse(0)

  def getParameters: java.util.List[Parameter] = parameters.asJava

  lazy val checkerFunction: String => String = {
    val checkerFun = problem.getMember("check")
    if (ScriptObjectMirror.isUndefined(checkerFun))
      checkerFunFromExternalChecker
    else
      x => checkerFun.asInstanceOf[ScriptObjectMirror].call(null, x).asInstanceOf[String]
  }

  def checkerFunFromExternalChecker: String => String = {
    if (externalChecker == null)
      throw new IllegalStateException("external checker is not defined")

    val ucl: URLClassLoader = new URLClassLoader(Array(externalChecker.jar.toURI.toURL))
    val checkerClass = ucl.loadClass(externalChecker.className)

    val checkerObject = checkerClass.newInstance()
    val check = checkerClass.getMethod("check", classOf[String])

    x => check.invoke(checkerObject, x).asInstanceOf[String]
  }

  def check(solutionJson: String): String = checkerFunction(solutionJson)
}

object JsKioProblem {
  private val classLoader: ClassLoader = ClassLoader.getSystemClassLoader.getParent

  private val factory: ScriptEngineManager = new ScriptEngineManager(classLoader)
}
