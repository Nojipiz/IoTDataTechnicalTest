//> using scala 2.13.14
//> using dep "org.apache.kafka::kafka:3.8.0"
//> using dep "io.circe::circe-core:0.14.10"
//> using dep "io.circe::circe-generic:0.14.10"
//> using dep "io.circe::circe-parser:0.14.10"
//> using dep "net.debasishg::redisclient:3.42"
//

import com.redis._
import scala.concurrent.Future
import io.circe._
import io.circe.parser._
import io.circe.generic.auto._
import scala.collection.JavaConverters.asJavaCollectionConverter
import org.apache.kafka.clients.consumer.ConsumerConfig.{
  BOOTSTRAP_SERVERS_CONFIG,
  _
}
import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.serialization.StringDeserializer
import scala.util.{Try, Success, Failure, Properties}
import java.time.Duration
import java.{util => ju}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.annotation.tailrec
import java.util.Date

case class ErrorRecord(
    timestamp: String,
    device_ip: String,
    error_code: Int
)

object Consumer {
  private lazy val persistence = new RedisClient(
    Properties.envOrNone("PERSISTENCE_SERVER").getOrElse("localhost"),
    6379
  )
  private lazy val BOOTSTRAP_SERVERS_VALUE =
    Properties.envOrNone("BOOTSTRAP_SERVERS").getOrElse("localhost:9094")
  private lazy val TOPIC_NAME =
    Properties.envOrNone("TOPIC_NAME").getOrElse("ip_data")
  private lazy val GROUP_ID_VALUE = "consumer-original"
  private lazy val TIMEOUT_MILLIS = 100

  def main(args: Array[String]): Unit = {
    Future(printCurrentAmountOfRecords())
    consume(consumer, TOPIC_NAME, TIMEOUT_MILLIS)
  }

  lazy val consumer: KafkaConsumer[String, String] = {
    val props = new ju.Properties()
    props.put(BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS_VALUE)
    props.put(GROUP_ID_CONFIG, GROUP_ID_VALUE)
    props.put(ENABLE_AUTO_COMMIT_CONFIG, "true")
    props.put(AUTO_COMMIT_INTERVAL_MS_CONFIG, "100")
    props.put(
      KEY_DESERIALIZER_CLASS_CONFIG,
      classOf[StringDeserializer].getName
    )
    props.put(
      VALUE_DESERIALIZER_CLASS_CONFIG,
      classOf[StringDeserializer].getName
    )
    new KafkaConsumer(props)
  }

  def consume(
      consumer: KafkaConsumer[String, String],
      topic: String,
      timeoutMillis: Long
  ): Unit = {
    @tailrec
    def operateRecords(): Unit = {
      consumer
        .poll(Duration.ofMillis(timeoutMillis))
        .iterator()
        .forEachRemaining { record =>
          println(
            s"Processing partition: ${record.partition}, offset: ${record.offset}"
          )
          persistRecord(record.value)
        }
      operateRecords()
    }

    Try {
      consumer.subscribe(ju.List.of(topic))
      operateRecords()
      consumer.close()
    } match {
      case Success(v) => println("Consumer finished gratefully")
      case Failure(e) => println("Error at executing consumer")
    }
  }

  @tailrec
  def printCurrentAmountOfRecords(): Unit = {
    Thread.sleep(30000)
    val ipsMessage = persistence.dbsize
      .fold("Error, can't print the amount of unique IP's")(amount =>
        s"Amount of unique IP's ${amount}"
      )
    val date = new Date()

    println(s""" 
      | ------------- Unique IPs -------------
      |       ${ipsMessage}
      |    at: ${date}
      | --------------------------------------
      """.stripMargin)
    printCurrentAmountOfRecords()
  }

  private def persistRecord(jsonValue: String) = {
    decode[ErrorRecord](jsonValue)
      .fold(
        error => println("There is an error deserializing the entity"),
        errorRecord => persistence.set(errorRecord.device_ip, "Found")
      )
  }
}
