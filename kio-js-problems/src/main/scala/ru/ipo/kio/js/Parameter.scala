package ru.ipo.kio.js

import org.mozilla.javascript.{Context, ContextAction, ContextFactory, NativeFunction, Scriptable}

import scala.collection.JavaConverters._

abstract sealed class ResultsOrdering

case object Maximize extends ResultsOrdering

case object Minimize extends ResultsOrdering

class Parameter(
                 val name: String,
                 val title: String,
                 ordering: ResultsOrdering,
                 val view: Any => String,
                 val normalize: Any => Double
               ) extends Ordering[Result] {
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

  def v(a: Any): String = view(a)
  def n(a: Any): Double = normalize(a)

  def normalizeWithOrdering(a: Any): Double = ordering match {
    case Minimize => -normalize(a)
    case Maximize => normalize(a)
  }
}

object Parameter {

  private def mapUndefinedTo(x: Any, value: Any): Any = if (x == Scriptable.NOT_FOUND) value else x

  private def call(f: NativeFunction, thiz: JsKioProblem, args: Array[AnyRef]): AnyRef = {
    ContextFactory.getGlobal.call(new ContextAction[AnyRef] {
      override def run(cx: Context): AnyRef = {
        val problem = thiz.problem
        f.call(cx, thiz.problemScope, thiz.problem, args)
      }
    })
  }

  def apply(task: JsKioProblem, name: Any, title: Any, ordering: Any, view: Any, normalize: Any): Parameter = {
    val newName = mapUndefinedTo(name, "").toString
    val newTitle = mapUndefinedTo(title, "").toString

    val newView: Any => String = mapUndefinedTo(view, "") match {
      case v: NativeFunction => x => call(v, task, Array(x.asInstanceOf[AnyRef])).toString
      case s: String => x => x + s
    }

    val newNormalize: Any => Double = mapUndefinedTo(normalize, "") match {
      case "" => x => x.asInstanceOf[Number].doubleValue()
      case v: NativeFunction => x => call(v, task, Array(x.asInstanceOf[AnyRef])).asInstanceOf[Number].doubleValue()
    }

    val newOrdering = if (mapUndefinedTo(ordering, "maximize") == "maximize") Maximize else Minimize

    new Parameter(newName, newTitle, newOrdering, newView, newNormalize)
  }
}

class Result(map: Map[String, Any]) {
  def apply(name: String): Any = map(name)

  def this(javaMap: java.util.Map[String, AnyVal]) {
     this(javaMap.asScala.toMap)
  }

  override def toString: String = map.toString
}
