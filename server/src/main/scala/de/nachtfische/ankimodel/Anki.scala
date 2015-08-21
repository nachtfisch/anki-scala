package de.nachtfische.ankimodel

import java.nio.file._
import java.util.UUID

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.json4s.JValue
import slick.driver.SQLiteDriver.api._
import slick.jdbc.JdbcBackend

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class ApkgFile(pathToFile:Path) {
  val zipFileSystem = FileSystems.newFileSystem(pathToFile, getClass.getClassLoader)
  val mapper: ObjectMapper = new ObjectMapper()

  val tempDir = Files.createTempDirectory("ankiDeck");
  val dbFile = tempDir.resolve("collection.sqlite")

  Files.copy(zipFileSystem.getPath("/collection.anki2"), Files.newOutputStream(dbFile))


  def mediaDirectory(): Map[String,String] = {
    val tree = mapper.readTree(Files.newInputStream(zipFileSystem.getPath("/media")))
    toSimpleMap(tree)
  }

  def fetchModels(): Map[String, CardModel] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val modelFuture = db.run(sql"select models from col".as[String].head) map parseModelString
    await(modelFuture)
  }

  def await[T](modelFuture: Future[T]): T = {
    Await.result(modelFuture, Duration("300 s"))
  }

  def notes:Seq[Fact] = {
    val noteFuture = db.run(TableQuery[Notes].result)
    val models = fetchModels()
    await(noteFuture) map {case (id, fields, modelId) => Fact(models(modelId.toString()), DbUtils.splitFields(fields))}
  }

  private def parseModelString(s: String): Map[String, CardModel] = {
    import org.json4s.jackson.JsonMethods._
    implicit val defaultModel = org.json4s.DefaultFormats
    val model: JValue = parse(s)

    model.extract[Map[String, CardModel]]
  }

  def db: JdbcBackend.DatabaseDef = {
    JdbcBackend.Database.forURL(
      "jdbc:sqlite:%s" format dbFile.toAbsolutePath.toString,
      driver = "org.sqlite.JDBC")
  }

  private def toSimpleMap(jsonNode: JsonNode): Map[String,String] = {
    jsonNode.fields().foldLeft(Map[String, String]()) { (acc, current) =>
      acc + (current.getKey -> current.getValue.asText())
    }
  }


}

/**
 * CREATE TABLE notes (
    id              integer primary key,   /* 0 note id, ref from card*/
    guid            text not null,         /* 1 */
    mid             integer not null,      /* 2 model id*/
    mod             integer not null,      /* 3 last modified*/
    usn             integer not null,      /* 4 */
    tags            text not null,         /* 5 seperated list of tags */
    flds            text not null,         /* 6 seperated list of fields */
    sfld            integer not null,      /* 7 */
    csum            integer not null,      /* 8 */
    flags           integer not null,      /* 9 */
    data            text not null          /* 10 */
);
 */
class Notes(tag: Tag) extends Table[(Int, String, Long)](tag, "notes") {
  def name = column[Int]("id", O.PrimaryKey)
  def modelId = column[Long]("mid")
  def modified = column[Long]("mod")
  def fields = column[String]("flds") // split with DbUtils.splitFields(fields)
  def * = (name, fields, modelId)
}

/*CREATE TABLE col (
id              integer primary key,
crt             integer not null, // created time
mod             integer not null, // last modified
scm             integer not null,
ver             integer not null,
dty             integer not null,   /* dirty flag */
usn             integer not null,
ls              integer not null,
conf            text not null,
models          text not null, /* json encrypted models with id mapping */
decks           text not null,
dconf           text not null,
tags            text not null
);*/

class CardCollection(tag:Tag) extends Table[String](tag, "col") {
  override def * = column[String]("models")
}


/*CREATE TABLE cards (
id              integer primary key,   /* 0 */
nid             integer not null,      /* 1 note id */
did             integer not null,      /* 2 deck id*/
ord             integer not null,      /* 3 ordinal if multiple cards are created from single note*/
mod             integer not null,      /* 4 modified */
usn             integer not null,      /* 5 */
type            integer not null,      /* 6 */
queue           integer not null,      /* 7 */
due             integer not null,      /* 8 */
ivl             integer not null,      /* 9 */
factor          integer not null,      /* 10 */
reps            integer not null,      /* 11 */
lapses          integer not null,      /* 12 */
left            integer not null,      /* 13 */
odue            integer not null,      /* 14 */
odid            integer not null,      /* 15 */
flags           integer not null,      /* 16 */
data            text not null          /* 17 */
);*/

object DbUtils {
  def splitFields(fieldsOrig: String): List[String] = {
    var fields = fieldsOrig;
    fields = fields.replaceAll("\\x1f\\x1f", "\u001f\u001e\u001f")
    fields = fields.replaceAll("\\x1f$", "\u001f\u001e")
    val split: Array[String] = fields.split("\\x1f")
    for (i <- split.indices) {
      if (split(i).matches("\\x1e")) {
        split(i) = ""
      }
    }
    return split.toList
  }

}

class Anki {

}

object Anki {

  val temporaryFolder = Files.createTempDirectory("anki")

  val apkgMap: mutable.Map[String, ApkgFile] = collection.mutable.Map[String,ApkgFile]()

  apkgMap.put("sample", sampleApkg)
  apkgMap.put("russian", new ApkgFile(Paths.get("/Users/christian/dev/deck-reader/src/main/resources/Top_5000_Russian_Words_-_1396_verbs_English.apkg")))

  def fetchApkg(url:String): String = {
    val downloadedFile: (Path, String) = downloadAnkiFile(url)
    val file: ApkgFile = new ApkgFile(downloadedFile._1)
    apkgMap.put(downloadedFile._2, file)

    downloadedFile._2
  }

  def getById(id:String): Option[ApkgFile] = {
    apkgMap.get(id)
  }

  def downloadAnkiFile(url: String, folder: Path = temporaryFolder):(Path,String) = {
    import java.net.URL
    import sys.process._

    val id: UUID = UUID.randomUUID()
    val filePath: Path = folder.resolve(id.toString + ".apkg")

    new URL(url) #> filePath.toFile !!

    return (filePath,id.toString)
  }

  def sampleApkg:ApkgFile = {
    new ApkgFile(Paths.get("/Users/christian/dev/deck-reader/src/main/resources/French_vocab_appearance.apkg"))
  }

}
