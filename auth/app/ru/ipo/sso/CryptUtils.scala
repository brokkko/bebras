package ru.ipo.sso

import java.security.Signature
import javax.xml.bind.DatatypeConverter

object CryptUtils {

  def base64decode(base64Code: String): Array[Byte] = DatatypeConverter.parseBase64Binary(base64Code)

  def base64encode(data: Array[Byte]): String = DatatypeConverter.printBase64Binary(data)

  def sign(lines: String*) = {
    val text = lines mkString "\n"

    val signer = Signature.getInstance("SHA512withRSA")
    signer.initSign(SSOConfiguration.get.privateKey)
    signer.update(text.getBytes("UTF-8"))

    base64encode(signer.sign())
  }

}
