package ru.ipo.kio.js

import org.mozilla.javascript.{Context, ContextAction, ContextFactory, NativeArray, NativeFunction, NativeObject, Script, Scriptable, ScriptableObject}
import org.scalatest.{FlatSpec, Matchers}
import ru.ipo.Resource
/**
  * Created by ilya on 20.03.21.
  */
class RhinoTester extends FlatSpec with Matchers {

  "Rhino" should "work" in {
    val context = Context.enter()
    val scope = context.initStandardObjects

    val code1 = Resource("/ru/ipo/kio/js/brilliant.js").asString()
    val code2 = Resource("/ru/ipo/kio/js/heesch.js").asString()
    val code3 = Resource("/ru/ipo/kio/js/epidemic.js").asString()

//    val r = context.evaluateString(scope, "let f = (x) => x + 1; console.log(f(10));", "<scala>", 1, null)
    val r1 = context.evaluateString(scope, code1, "brilliant.js", 1, null)
    val r2 = context.evaluateString(scope, code2, "heesch.js", 1, null)
    val r3 = context.evaluateString(scope, code3, "epidemic.js", 1, null)
    System.out.println("r = " + r1)
    System.out.println("r = " + r2)
    System.out.println("r = " + r3)
    Context.exit();
  }

  it should "test values to be undefined" in {
    val cf = new ContextFactory
    val js = Resource("/ru/ipo/kio/js/heesch.js").asString()
    cf.call(new ContextAction[Unit] {
      override def run(cx: Context): Unit = {
        val globalScope = cx.initSafeStandardObjects()
        val scope = cx.newObject(globalScope)

        val className = "heesch.Heesch"
        val settings = "{level: 2}"
        cx.evaluateString(scope, js, "<>", 1, null).asInstanceOf[NativeObject]
        val task = cx.evaluateString(scope, s"new $className($settings)", "<>", 1, null).asInstanceOf[NativeObject]
        val params = ScriptableObject.callMethod(cx, task, "parameters", Array()).asInstanceOf[NativeArray]
        val param1 = params.get(0).asInstanceOf[NativeObject]
        val param1view = ScriptableObject.getProperty(param1, "view").asInstanceOf[NativeFunction]
        val result = param1view.call(cx, scope, task, Array(0.asInstanceOf[AnyRef]))
        System.out.println("result " + result)
        val param1undef = ScriptableObject.getProperty(param1, "undeff")
        System.out.println("undef " + param1undef + " " + (param1undef == Scriptable.NOT_FOUND))



//        val r = cx.evaluateString(scope, s"new $className({level: 2})", "<>", 1, null)
//        System.out.println(js)
//        System.out.println(r)
//        System.out.println(r.asInstanceOf[NativeObject].entrySet())
//        System.out.println(r.asInstanceOf[NativeObject].)
//        System.out.println(r.asInstanceOf[NativeObject].getAllIds.mkString("Array(", ", ", ")"))
//        System.out.println(r.asInstanceOf[NativeObject].getPrototype.getIds.mkString("Array(", ", ", ")"))
      }
    })
  }

  it should "be callable from different contexts" in {
    val cf = new ContextFactory

    val fCode = "f = function(x) {return x + 1;}"

    val globalScope = cf.call(new ContextAction[Scriptable] {
      override def run(cx: Context): Scriptable = cx.initSafeStandardObjects()
    })

    val newScope = cf.call(new ContextAction[Scriptable] {
      override def run(cx: Context): Scriptable = cx.newObject(globalScope)
    })

    val f = cf.call(new ContextAction[NativeFunction] {
      override def run(cx: Context): NativeFunction = {
        cx.evaluateString(newScope, fCode, "f.code", 1, null).asInstanceOf[NativeFunction]
      }
    })

    val x = cf.call(new ContextAction[Number] {
      override def run(cx: Context): Number = {
        f.call(cx, newScope, null, Array(10.asInstanceOf[AnyRef])).asInstanceOf[Number]
      }
    })

    System.out.println(x)
  }

}
