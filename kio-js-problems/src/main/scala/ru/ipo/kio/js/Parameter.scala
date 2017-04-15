package ru.ipo.kio.js

import jdk.nashorn.api.scripting.ScriptObjectMirror

abstract sealed class ResultsOrdering

case object Maximize extends ResultsOrdering

case object Minimize extends ResultsOrdering

class Parameter(
                 name: String,
                 title: String,
                 ordering: ResultsOrdering,
                 val view: Any => String,
                 normalize: Any => Double
               ) extends Ordering[Result] {
  val x: Map[String, String] = Map()

  override def compare(x: Result, y: Result): Int = {
    val xVal: Double = normalize(x(name))
    val yVal: Double = normalize(y(name))

    def double2sign(d: Double): Int = d match {
      case _ if Math.abs(d) < 1e-8 => 0
      case _ if d > 0 => 1
      case _ => -1
    }

    ordering match {
      case Maximize => double2sign(xVal - yVal)
      case Minimize => double2sign(yVal - xVal)
    }
  }
}

object Parameter {

  private def mapUndefinedTo(x: Any, value: Any): Any = if (ScriptObjectMirror.isUndefined(x)) value else x

  def apply(name: Any, title: Any, ordering: Any, view: Any, normalize: Any): Parameter = {
    val newName = mapUndefinedTo(name, "").toString
    val newTitle = mapUndefinedTo(title, "").toString

    val newView: Any => String = mapUndefinedTo(view, "") match {
      case v: ScriptObjectMirror if v.isFunction => x => v.call(null, x.asInstanceOf[AnyRef]).toString
      case s: String => x => x + s
    }

    val newNormalize: Any => Double = mapUndefinedTo(normalize, "") match {
      case "" => x => x.asInstanceOf[Number].doubleValue()
      case v: ScriptObjectMirror => x => v.call(null, x.asInstanceOf[AnyRef]).asInstanceOf[Number].doubleValue()
    }

    val newOrdering = if (mapUndefinedTo(ordering, "maximize") == "maximize") Maximize else Minimize

    new Parameter(newName, newTitle, newOrdering, newView, newNormalize)
  }
}

class Result(map: Map[String, Any]) {
  def apply(name: String): Any = map(name)

  override def toString: String = map.toString
}