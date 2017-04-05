package ru.ipo.kio.js

import javax.script.{Bindings, ScriptContext, ScriptEngineFactory, ScriptEngineManager}

import jdk.nashorn.api.scripting.{JSObject, ScriptObjectMirror}
import ru.ipo.Resource

/**
  * Created by ilya on 28.03.17.
  */
class JsKioProblem(jsCode: String, className: String, settingsJson: String) {

  import JsKioProblem._

  private val scriptEngine = factory.getEngineByName("nashorn")

  val problem = createProblem()
  val parameters = getParameters()

  private def createProblem(): ScriptObjectMirror = {
    scriptEngine.eval(jsCode)
    scriptEngine.eval(s"new $className($settingsJson);").asInstanceOf[ScriptObjectMirror]
  }

  private def getParameters(): Seq[Parameter] = {
    val rawParameters = problem.callMember("parameters").asInstanceOf[ScriptObjectMirror]
    val paramsCount: Int = rawParameters.getMember("length").asInstanceOf[Int]

    (0 until paramsCount).map { i =>
      val param = rawParameters.getSlot(i).asInstanceOf[ScriptObjectMirror]
      Parameter(
        param.getMember("name").asInstanceOf[String],
        param.getMember("title").asInstanceOf[String],
        param.getMember("ordering").asInstanceOf[String],
        param.getMember("view"),
        param.getMember("normalize")
      )
    }
  }

}

object JsKioProblem {
  private val factory: ScriptEngineManager = new ScriptEngineManager()
}
