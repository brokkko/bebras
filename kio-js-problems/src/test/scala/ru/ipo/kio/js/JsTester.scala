package ru.ipo.kio.js

import java.nio.file.{Files, Paths}

import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by ilya on 28.03.17.
  */
class JsTester extends FlatSpec with Matchers {

  private def resourceToString(name: String): String = {
    val resource = classOf[JsTester].getResource(name)
    new String(Files.readAllBytes(
      Paths.get(resource.toURI)
    ))
  }

  private def getExampleProblem = new JsKioProblem(
    resourceToString("/ru/ipo/kio/js/task_example.js"),
    "task_example.TaskExample",
    "{level: 2}"
  )

  "Tanechka" should "not fail reading js kio problem" in {
    val p = getExampleProblem
  }

  it should "get problem parameters" in {
    val p = getExampleProblem
    p.compare()
  }

}
