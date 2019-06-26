import java.io.File
import java.nio.file.{Files, Paths}

object Gzip {
  def main(args: Array[String]): Unit = {
    if (args.length != 2){
      println("usage: $0 [-c|-d] filename")
      System.exit(1)
    }
    if (args(0) == "-d"){
      val fileName = args(1)


      println(decompress(fileName))
    }
    if (args(0) == "-c"){
      val fileName = args(1)
      compress(fileName)
    }
  }

  def compress(fileName: String): Unit ={
    val file = new File(fileName)
    val byteSize = file.length()
    // msはいらないので切り捨てる
    val lastModified = file.lastModified() / 1000
    val bytes = Files.readAllBytes(Paths.get(fileName))
    val gzipObject = GzipObject.createGzipObject(bytes, fileName, lastModified, byteSize)
    gzipObject.writeCompressData(fileName + ".gz")
  }

  def decompress(fileName: String): String ={
    val bytes = Files.readAllBytes(Paths.get(fileName))
    val gzipObject = scanGzipFile(bytes)
    gzipObject.decompressData()
    val isCorrectCRC = gzipObject.checkCRC()
    if (!isCorrectCRC){
      throw new Exception("CRC is mismatch.")
    }

    gzipObject.decompressedString
  }

  def scanGzipFile(bytes: Array[Byte]): GzipObject={
    val len = bytes.length
    val id1 = bytes(0)
    val id2 = bytes(1)

    if (id1 != 0x1f && id2 != 0x8b){
      throw new Exception("file format is not gzip.")
    }

    val compressionMethod = bytes(2)
    val flag = bytes(3)
    // timeは4bit反転されている
    val time = Array(bytes(7), bytes(6), bytes(5), bytes(4))
    val xfl = bytes(8)
    val os = bytes(9)
    var fileName: Array[Byte] = Array()
    var i = 10
    // gzipはfilenameオプションしかしていできないのでこれもそれだけに追従する
    // flagが0x08だとファイルネームoptionが有効
    if(flag == 0x08) {
      var endOfFilename = false
      // ファイル名部は最終bitが0bitとなるのでそこまでループする
      while(!endOfFilename){
        val b = bytes(i)
        i = i + 1
        if(b == 0x00){
          endOfFilename = true
        }
        fileName = fileName :+ b
      }
    }
    var data: Array[Byte] = Array()
    //下部8bitはチェックサムとfile sizeになるので8bitを除く
    for{j <- i to len - 9}{
      data = data :+ bytes(j)
    }
    // crc32, filesizeは反転されている
    val crc32 = Array(bytes(len - 5), bytes(len - 6), bytes(len - 7), bytes(len - 8))
    val fileSize = Array(bytes(len - 1), bytes(len - 2), bytes(len - 3), bytes(len - 4))

    new GzipObject(
      compressionMethod,
      flag,
      time,
      xfl,
      os,
      Array(),
      Array(),
      Array(),
      Array(),
      Array(),
      data,
      crc32,
      fileSize
    )
  }
}
