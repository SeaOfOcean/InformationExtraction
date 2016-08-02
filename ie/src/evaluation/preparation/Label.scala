package evaluation.preparation

import java.io.{BufferedWriter, FileWriter}

import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.util.CoreMap

import scala.collection.mutable.ListBuffer
import scala.io.{Source, StdIn}

/**
  * Created by xianyan on 16-7-27.
  */
object Label {


  //  val titleList = List("president", "ceo", "officer", "chairman")
  val titleList = List {}
  val person = "PERSON"
  val title = "TITLE"
  val organization = "ORGANIZATION"
  val startPerson = "<PERSON>"
  val endPerson = "</PERSON>"
  val startTitle = "<TITLE>"
  val endTitle = "</TITLE>"
  val startOrganization = "<ORGANIZATION>"
  val endOrganization = "</ORGANIZATION>"


  def main(args: Array[String]): Unit = {
    val urlMap = CrawlerHelper.getUrlMap()
    val company = "Apple"
    val result = labelCompany2(company, 0)
    val bw = new BufferedWriter(new FileWriter(CrawlerHelper.getLabeledFile(company)))
    bw.write(result)
    bw.close()
    Extraction.extract(company)
  }

  class FixedList[A](max: Int) extends Traversable[A] {

    val list: ListBuffer[A] = ListBuffer()

    def append(elem: A) {
      if (list.size == max) {
        list.trimStart(1)
      }
      list.append(elem)
    }

    def removeLast(): A = {
      list.remove(list.length - 1)
    }

    def foreach[U](f: A => U) = list.foreach(f)

  }

  def labelCompany2(company: String, index: Int): String = {
    val lines = Source.fromFile(CrawlerHelper.getWebContentPath(company, index)).getLines().toList
    var labeledLines = ListBuffer[String]()
    var historyBuffer = new FixedList[String](10)
    var redos = ListBuffer[String]()
    for (line <- lines) {
      println(line)
      val document: Annotation = new Annotation(line)
      NerHelper.pipeline.annotate(document)
      val sentences: java.util.List[CoreMap] = document.get(classOf[CoreAnnotations.SentencesAnnotation])
      var result = List[String]()
      import scala.collection.JavaConversions._
      for (sentence <- sentences) {
        // traversing the words in the current sentence
        // a CoreLabel is a CoreMap with additional token-specific methods
        import scala.collection.JavaConversions._

        def processOneWord(word: String): Any = {
          println(word)

          var lab = StdIn.readLine()
          var label = "O"
          lab match {
            case "1" => label = person
            case "2" => label = title
            case "3" => label = organization
            case "c" => {
              redos :+= word
              redos :+= (historyBuffer.removeLast())
            }
            case _ => label
          }
          if (lab == "c") result = result.dropRight(1)
          else result :+= (word + "/" + label)
          if(lab != "c") historyBuffer.append(word)

        }


        for (token <- sentence.get(classOf[CoreAnnotations.TokensAnnotation])) {
          while (!redos.isEmpty) {
            val word = redos.remove(redos.length-1)
            processOneWord(word)
          }
          // this is the text of the token
          val word = token.get(classOf[TextAnnotation])

          processOneWord(word)
        }
      }
      labeledLines :+= result.mkString("\t")
    }
    labeledLines.mkString("\n")
  }

  def labelCompany(company: String, index: Int): String = {
    val lines = Source.fromFile(CrawlerHelper.getWebContentPath(company, index)).getLines().toList
    var content = ""
    for (line <- lines) {
      val items = line.split(",|and")
      if (!items.isEmpty) {
        content += (startPerson + items(0) + endPerson + " , ")
        content += (startTitle + items(1) + endTitle + " , ")
        for (item <- items.slice(2, items.length)) {
          if (isTitle(item)) {
            content += startTitle + item + endTitle + " , "
          } else {
            println(item)
            var lab = StdIn.readLine()
            if (lab == "2") content += startTitle + item + endTitle + " , "
            else if (lab == "3") content += startOrganization + item + endOrganization + " , "
            else content += lab + " , "
          }
        }
        content = content.substring(0, content.length - 3)
        content += "\n"
      }
    }
    content
  }

  def isTitle(text: String): Boolean = {
    for (title <- titleList) {
      if (text.toLowerCase().contains(title)) {
        return true
      }
    }
    return false
  }


}
