package de.nachtfische.sampledata



case class Gender(det:String)


case class Noun(gender:Gender, noun:String, definition:String, rank:Int)
case class OtherWord(verb:String, definition:String, rank:Int, speciality:Option[String])

object SpanishNounReader {

    def parseGender(genderString: String): Gender = genderString match {
        case "el" => Gender("el")
        case "la" => Gender("la")
        case "los" => Gender("los")
        case "las" => Gender("las")
        case _ => throw new IllegalArgumentException("couldn't parse gender " + genderString)
    }

    // encender (e-ie) (2133)$$$to turn on
    def parseOther(stringToParse:String):Either[String,OtherWord] = {
        val wordFieldInfo = "^(.*)(\\(.*\\))?\\((\\d+)\\)$".r
        val split: List[String] = stringToParse.split("\\$\\$\\$").toList
        val fieldInfo = wordFieldInfo.findFirstMatchIn(split(0)).map( t => (t.group(1), Option(t.group(2)), t.group(3)))

        fieldInfo.map(f => OtherWord(f._1.trim, split(1),f._3.toInt, f._2.map(_.trim))).toRight(stringToParse)
    }

    // el tiempo (68)$$$time . . . as a concept; weather
    def parseNoun(stringToParse:String):Either[String,Noun] = {
        val wordFieldInfo = "^(el|la|los|las) (.*) \\((\\d+)\\)$".r
        val split: List[String] = stringToParse.split("\\$\\$\\$").toList
        val fieldInfo = wordFieldInfo.findFirstMatchIn(split(0)).map( t => (t.group(1), t.group(2), t.group(3)))
        
        fieldInfo.map(f => Noun(parseGender(f._1), f._2, split(1), f._3.toInt)).toRight(stringToParse)
    }

    def parseNouns() :Iterator[Either[String,Noun]] = {
        scala.io.Source.fromInputStream(getClass.getResourceAsStream("data/spanish-nouns-top-2514.csv")).getLines().map(SpanishNounReader.parseNoun)
    }



}
