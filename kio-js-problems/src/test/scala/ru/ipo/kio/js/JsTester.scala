package ru.ipo.kio.js

import jdk.nashorn.api.scripting.{AbstractJSObject, ScriptObjectMirror}
import org.scalatest.{FlatSpec, Matchers}
import ru.ipo.Resource

/**
  * Created by ilya on 28.03.17.
  */
class JsTester extends FlatSpec with Matchers {

  private def getExampleProblem = new JsKioProblem(
    Resource("/ru/ipo/kio/js/task_example.js").asString(),
    "task_example.TaskExample",
    "{level: 2}",
    null
  )

  "Problem reader" should "not fail reading js kio problem" in {
    val p = getExampleProblem
  }

  it should "sort results correctly" in {
    val p = getExampleProblem

    val x1 = new Result(Map("steps" -> 42, "max" -> 100, "info1" -> 0.2))
    val x2 = new Result(Map("steps" -> 239, "max" -> 100, "info1" -> 0.3))
    val x3 = new Result(Map("steps" -> 42, "max" -> 200, "info1" -> 0.4))
    val x4 = new Result(Map("steps" -> 239, "max" -> 200, "info1" -> 0.5))
    val results = List(x1, x2, x3, x4)

    val sortedResults = results.sorted(p)

    sortedResults should equal (List(x3, x1, x4, x2))
  }

  it should "display results correctly" in {
    val p = getExampleProblem
    val r = new Result(Map("steps" -> 42, "max" -> 100, "info1" -> 0.2))
//    p.parameters.map(param => param.)
  }
}
