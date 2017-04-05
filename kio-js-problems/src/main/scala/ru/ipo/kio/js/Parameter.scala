package ru.ipo.kio.js

import jdk.nashorn.api.scripting.ScriptObjectMirror

abstract sealed class ResultsOrdering

case object Maximize extends ResultsOrdering

case object Minimize extends ResultsOrdering

class Parameter(
                 name: String,
                 title: String,
                 ordering: ResultsOrdering,
                 view: Any => String,
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
   def apply(name: String, title: String, ordering: String, view: ScriptObjectMirror, normalize: ScriptObjectMirror): Parameter = {
     val newView: Any => String = if (ScriptObjectMirror.isUndefined(view))
       x => x.toString
     else if (view.isFunction)
       x => view.call(null, x).toString
     else
       x => x.toString + view.toString

     val newNormalize: Any => Double = if (normalize.isFunction)
       x => normalize.call(x).asInstanceOf[Number].doubleValue()
     else
       x => x.asInstanceOf[Number].doubleValue()

     new Parameter(name, title, if (ordering == "maximize") Maximize else Minimize, newView, newNormalize)
   }
}

class Result(map: Map[String, Any]) {
  def apply(name: String): Any = map(name)
}