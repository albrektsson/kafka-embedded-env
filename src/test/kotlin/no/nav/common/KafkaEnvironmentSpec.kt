package no.nav.common

// import com.github.kittinunf.fuel.httpGet
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldEqualTo
import org.amshove.kluent.shouldContainAll
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.zookeeper.client.FourLetterWordMain
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.util.Properties

object KafkaEnvironmentSpec : Spek({

    fun KafkaEnvironment.acInit(): AdminClient =
            AdminClient.create(
                    Properties().apply {
                        set(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokersURL)
                        set(ConsumerConfig.CLIENT_ID_CONFIG, "embkafka-adminclient")
                    }
            )

    var adminClient: AdminClient? = null

    describe("kafka environment tests") {

        context("default kafka environment") {

            val keDefault = KafkaEnvironment()
            val nBroker = 1
            val nTopics = 0

            beforeGroup {
                keDefault.start()
                adminClient = keDefault.acInit()
            }

            it("should have 1 zookeeper") {

                FourLetterWordMain.send4LetterWord(
                        keDefault.serverPark.zookeeper.host,
                        keDefault.serverPark.zookeeper.port,
                        "ruok") shouldBeEqualTo "imok\n"
            }

            it("should have $nBroker broker") {

                adminClient?.describeCluster()?.nodes()?.get()?.toList()?.size ?: -1 shouldEqualTo nBroker
            }

            it("should have $nTopics topics available") {

                adminClient?.listTopics()?.names()?.get()?.toList()?.size ?: -1 shouldEqualTo nTopics
            }

            afterGroup {
                keDefault.tearDown()
                adminClient = null
            }
        }

        context("basic kafka environment") {

            val basicTopics = listOf("basic01", "basic02")
            val keBasic = KafkaEnvironment(topics = basicTopics)
            val nBroker = 1

            beforeGroup {
                keBasic.start()
                adminClient = keBasic.acInit()
            }

            it("should have 1 zookeeper") {

                FourLetterWordMain.send4LetterWord(
                        keBasic.serverPark.zookeeper.host,
                        keBasic.serverPark.zookeeper.port,
                        "ruok") shouldBeEqualTo "imok\n"
            }

            it("should have $nBroker broker") {

                adminClient?.describeCluster()?.nodes()?.get()?.toList()?.size ?: -1 shouldEqualTo nBroker
            }

            it("should have ${basicTopics.size} topics available") {

                adminClient?.listTopics()?.names()?.get()?.toList()?.size ?: -1 shouldEqualTo basicTopics.size
            }

            it("should have topics as requested available") {

                adminClient?.listTopics()?.names()?.get()?.toList() ?: emptyList<String>() shouldContainAll basicTopics
            }

            afterGroup {
                keBasic.tearDown()
                adminClient = null
            }
        }

        context("strange_1 kafka environment") {

            val keStrange1 = KafkaEnvironment(-2)
            val nBroker = 0

            beforeGroup {
                keStrange1.start()
            }

            it("should have 1 zookeeper") {

                FourLetterWordMain.send4LetterWord(
                        keStrange1.serverPark.zookeeper.host,
                        keStrange1.serverPark.zookeeper.port,
                        "ruok") shouldBeEqualTo "imok\n"
            }

            it("should have $nBroker broker") {

                // Cannot initialize AdminClient, verify empty brokersURL instead
                keStrange1.brokersURL shouldBeEqualTo ""
            }

            afterGroup {
                keStrange1.tearDown()
            }
        }

        /*context("strange_2 kafka environment") {

            val strange2Topics = listOf("strange201", "strange202", "strange203")
            val keStrange2 = KafkaEnvironment(
                    0,
                    topics = strange2Topics,
                    withRest = true)

            val zku = keStrange2.serverPark.zookeeper.url
            val nBroker = 1

            beforeGroup {
                keStrange2.start()
            }

            it("should have 1 zookeeper") {

                FourLetterWordMain.send4LetterWord(
                        keStrange2.serverPark.zookeeper.host,
                        keStrange2.serverPark.zookeeper.port,
                        "ruok") shouldBeEqualTo "imok\n"
            }

            it("should have $nBroker broker") {

                ZkUtils.apply(zku, sessTimeout, connTimeout, false).run {
                    val n = allBrokersInCluster.size()
                    close()
                    n
                } shouldEqualTo nBroker
            }

            // +1 is due to schema registry topic for schemas
            it("should have ${strange2Topics.size + 1} topics available") {

                ZkUtils.apply(zku, sessTimeout, connTimeout, false).run {
                    val n = allTopics.size()
                    close()
                    n
                } shouldEqualTo (strange2Topics.size + 1)
            }

            it("should have topics as requested available") {

                ZkUtils.apply(zku, sessTimeout, connTimeout, false).run {
                    val topics = allTopics
                    val lTopics = mutableListOf<String>()

                    topics.foreach { lTopics.add(it) }
                    close()
                    lTopics
                } shouldContainAll strange2Topics
            }

            it("should have a schema registry") {

                (keStrange2.serverPark.schemaregistry.url + "/config")
                        .httpGet()
                        .responseString().third.component1() shouldEqual """{"compatibilityLevel":"BACKWARD"}"""
            }

            it("should have a rest server") {

                // quick and raw http&json
                (keStrange2.serverPark.rest.url + "/brokers")
                        .httpGet()
                        .responseString().third.component1() shouldEqual """{"brokers":[0]}"""
            }

            afterGroup {
                keStrange2.tearDown()
            }
        }*/
    }
})