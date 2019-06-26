import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.file.{Files, Paths}
import java.util.zip.{CRC32, Deflater, Inflater}

import org.apache.commons.codec.binary.Hex

class GzipObject(
                  val compressionMethod: Byte,
                  val flag: Byte,
                  val time: Array[Byte],
                  val xfl: Byte,
                  val os: Byte,
                  val FEXTRAs: Array[Byte],
                  val FNAME: Array[Byte],
                  val FTEXT: Array[Byte],
                  val FCOMMENT: Array[Byte],
                  val FHCRC: Array[Byte],
                  val compressBlocks: Array[Byte],
                  val crc32: Array[Byte],
                  val ISIZE: Array[Byte]
                ) {
  var decompressedString: String = _

  def decompressData(): Unit = {
    // gzipの圧縮データにはzlibのheaderがつかないからnowrap
    val inflater = new Inflater(true)

    inflater.setInput(compressBlocks)
    val result: Array[Byte] = new Array(compressBlocks.length * 10)
    val len = inflater.inflate(result)

    decompressedString = new String(result, 0, len, "UTF-8")
  }

  def writeCompressData(fileName: String): Unit = {
    val outputBytes: Array[Byte] = Array(0x1f.toByte, 0x8b.toByte) ++ Array(compressionMethod) ++ Array(flag) ++ time ++ Array(xfl) ++ Array(os) ++
      FEXTRAs ++ FNAME ++ FTEXT ++ FTEXT ++ FCOMMENT ++ FHCRC ++ compressBlocks ++ crc32 ++ ISIZE
    Files.write(Paths.get(fileName), outputBytes)
  }

  def checkCRC(): Boolean = {
    val calculator = new CRC32
    val decompressedBytes = decompressedString.getBytes
    calculator.update(decompressedBytes)
    val objectCRC32 = Hex.encodeHexString(crc32).mkString("")
    val newCRC32 = calculator.getValue.toHexString

    objectCRC32 == newCRC32
  }

  override def toString: String = {
    "compressionMethod: " + compressionMethod + "\n" +
    "flag: " + flag + "\n" +
    "time: " + time.mkString(" ") + "\n" +
    "xfl: " + xfl + "\n" +
    "os: " + os + "\n" +
    "FEXTRA: " + FEXTRAs.mkString(" ") + "\n" +
    "FNAME: " + FNAME.mkString(" ") + "\n" +
    "FTEXT: " + FTEXT.mkString(" ") + "\n" +
    "FCOMMENT: " + FCOMMENT.mkString(" ") + "\n" +
    "FHCRC: " + FHCRC.mkString(" ") + "\n" +
    "compressedBlocks: " + compressBlocks.mkString(" ") + "\n" +
    "crc32: " + crc32.mkString(" ") + "\n" +
    "ISIZE: " + ISIZE.mkString(" ")
  }
}

object GzipObject{
  def createGzipObject(contents: Array[Byte], fileName: String, lastModified: Long, byteSize: Long): GzipObject ={
    // file名 + 末尾に0x00を付ける
    val fileNameBytes = fileName.getBytes(Charset.forName("UTF-8")) :+ 0x00.toByte
    val lastModifiedBytes = longToByteArray(lastModified)
    val byteSizeBytes = longToByteArray(byteSize)
    val crc32 = calculateCRC32(contents)
    val compressedContent = compressContents(contents)

    new GzipObject(
      0x08,
      0x08,
      // 下部4byteを逆順で配置する
      Array(lastModifiedBytes(7), lastModifiedBytes(6), lastModifiedBytes(5), lastModifiedBytes(4)),
      0x00,
      // 255はunknown
      255.toByte,
      Array(),
      fileNameBytes,
      Array(),
      Array(),
      Array(),
      compressedContent,
      Array(crc32(7), crc32(6),crc32(5), crc32(4)),
      Array(byteSizeBytes(7), byteSizeBytes(6), byteSizeBytes(5), byteSizeBytes(4))
    )
  }

  def longToByteArray(l: Long): Array[Byte] = {
    ByteBuffer.allocate(8).putLong(l).array()
  }

  def calculateCRC32(contents: Array[Byte]): Array[Byte]={
    val calculator = new CRC32
    calculator.update(contents)
    val l = calculator.getValue
    longToByteArray(l)
  }

  def compressContents(contents: Array[Byte]): Array[Byte] ={
    // zlibのheaderなんかを入れてほしくないのでnowrap
    val deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true)
    val output = new Array[Byte](contents.length * 2)
    deflater.setInput(contents)
    deflater.finish()

    val length = deflater.deflate(output)
    output.take(length)
  }
}
