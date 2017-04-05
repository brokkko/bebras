package ru.ipo

import java.io.ByteArrayOutputStream

import scala.annotation.tailrec

/**
  * Created by ilya on 01.04.17.
  */
object Std {
  def using[T <: AutoCloseable, V](resource: T)(block: T => V): V = {
    try {
      block(resource)
    } finally {
      if (resource != null) resource.close()
    }
  }
}

class Resource(name: String) {
  import Std._
  import Resource._

  def asByteArray(): Array[Byte] = {
    using(cl.getResourceAsStream(name)) {is =>
      val baos: ByteArrayOutputStream = new ByteArrayOutputStream()
      val buffer: Array[Byte] = new Array[Byte](1024)

      @tailrec
      def copy(): Unit = {
        val read = is.read(buffer)
        if (read != -1) {
          baos.write(buffer, 0, read)
          copy()
        }
      }

      copy()

      baos.toByteArray
    }
  }

  def asString(): String = new String(asByteArray(), "UTF-8")
}

object Resource {

  private val cl = Resource.getClass.getClassLoader

  def apply(name: String) = new Resource(name)

}