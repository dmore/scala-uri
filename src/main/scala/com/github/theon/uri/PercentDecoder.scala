package com.github.theon.uri


/**
 * Date: 23/06/2013
 * Time: 20:38
 */
object PercentDecoder extends UriDecoder {

  def decode(u: Uri) =
    try {
      u.copy (
        pathParts = u.pathParts.map(decodePathPart),
        query = u.query.copy (
          u.query.parameters.map(decodeTuple)
        )
      )
    } catch {
      case e: NumberFormatException => throw new UriDecodeException("Encountered '%' followed by a non hex number. It looks like this URL isn't Percent Encoded. If so look at using the NoopDecoder")
    }

  def decodePathPart(pp: PathPart) = pp match {
    case StringPathPart(s) => StringPathPart(decodeString(s))
    case MatrixParams(p, params) => MatrixParams(decodeString(p), params.map(decodeTuple))
  }

  def decodeTuple(kv: (String, String)) =
    decodeString(kv._1) -> decodeString(kv._2)

  protected def decodeString(s: String) = {
    val segments = s.split('%')
    val decodedSegments = segments.tail.map(seg => {
      val percentByte = Integer.parseInt(seg.substring(0, 2), 16).toByte
      new String(Array(percentByte), "UTF-8") + seg.substring(2)
    })
    segments.head + decodedSegments.mkString
  }
}

object PermissiveDecoder extends Permissive(PercentDecoder)

object NoopDecoder extends UriDecoder {
  def decode(u: Uri) = u
}

class Permissive(child: UriDecoder) extends UriDecoder {
  def decode(u: Uri) = {
    try {
      child.decode(u)
    } catch {
      case e: UriDecodeException => u
    }
  }
}

trait UriDecoder {
  def decode(u: Uri): Uri
}

class UriDecodeException(message: String) extends Exception(message)