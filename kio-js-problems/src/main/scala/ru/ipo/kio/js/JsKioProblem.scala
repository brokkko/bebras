package ru.ipo.kio.js

import org.mozilla.javascript.{Context, ContextAction, ContextFactory, NativeArray, NativeFunction, NativeObject, Scriptable, ScriptableObject}

import java.io.File
import java.net.URLClassLoader
import scala.collection.JavaConverters._

case class ExternalChecker(jar: File, className: String)

/**
  * Created by ilya on 28.03.17.
  */
class JsKioProblem(jsCode: String, className: String, settingsJson: String, externalChecker: ExternalChecker) extends Ordering[Result] {

  import JsKioProblem._

  val (problemScope, problem) = ContextFactory.getGlobal.call(new ContextAction[(ScriptableObject, NativeObject)] {
    override def run(cx: Context): (ScriptableObject, NativeObject) = {
      val problemScope = cx.newObject(globalScope).asInstanceOf[ScriptableObject]
      cx.evaluateString(problemScope, jsCode, className + ".js", 1, null)

      val problem = cx.evaluateString(problemScope, s"new $className($settingsJson);", "", 1, null).asInstanceOf[NativeObject]

      (problemScope, problem)
    }
  })

  val parameters: Seq[Parameter] = extractParameters()
  private val parametersView = parameters.view

  private def extractParameters(): Seq[Parameter] = {
    val rawParameters = ScriptableObject.callMethod(problem, "parameters", Array()).asInstanceOf[NativeArray]
    val paramsCount: Int = rawParameters.getLength.asInstanceOf[Int]

    (0 until paramsCount).map { i =>
      val param = rawParameters.get(i).asInstanceOf[NativeObject]
      Parameter(
        this,
        ScriptableObject.getProperty(param, "name"),
        ScriptableObject.getProperty(param, "title"),
        ScriptableObject.getProperty(param, "ordering"),
        ScriptableObject.getProperty(param, "view"),
        ScriptableObject.getProperty(param, "normalize")
      )
    }
  }

  override def compare(x: Result, y: Result): Int =
    parametersView.map(p => p.compare(x, y)).find(_ != 0).getOrElse(0)

  def getParameters: java.util.List[Parameter] = parameters.asJava

  lazy val checkerFunction: String => String = {
    val checkerFun = ScriptableObject.getProperty(problem, "check")
    if (checkerFun == Scriptable.NOT_FOUND)
      checkerFunFromExternalChecker
    else
      x => {
        val nativeChecker = checkerFun.asInstanceOf[NativeFunction]
        ContextFactory.getGlobal.call(new ContextAction[String] {
          override def run(cx: Context): String = nativeChecker.call(
            cx,
            ScriptableObject.getTopLevelScope(problem), //TODO specify another scope
            problem,
            Array(x)
          ).asInstanceOf[String]
        })
      }
  }

  def checkerFunFromExternalChecker: String => String = {
    if (externalChecker == null)
//      throw new IllegalStateException("external checker is not defined")
        return x => null; //TODO or may be really thrown an exception?

    val ucl: URLClassLoader = new URLClassLoader(Array(externalChecker.jar.toURI.toURL))
    val checkerClass = ucl.loadClass(externalChecker.className)

    val checkerObject = checkerClass.newInstance()
    val check = checkerClass.getMethod("check", classOf[String])

    x => check.invoke(checkerObject, x).asInstanceOf[String]
  }

  def check(solutionJson: String): String = checkerFunction(solutionJson)
}

object JsKioProblem {

  val globalScope: Scriptable = ContextFactory.getGlobal.call(new ContextAction[Scriptable] {
    override def run(cx: Context): Scriptable = {
      val globalScope = cx.initSafeStandardObjects()
//      globalScope.sealObject()
      cx.evaluateString(globalScope,
        """
          |console = {log: () => {}};
        """.stripMargin, "polyfill", 1, null);
      globalScope
    }
  })

}
