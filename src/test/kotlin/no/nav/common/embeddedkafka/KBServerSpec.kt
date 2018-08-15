package no.nav.common.embeddedkafka

import no.nav.common.KafkaEnvironment
import org.amshove.kluent.shouldEqualTo
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object KBServerSpec : Spek({

    val kEnv = KafkaEnvironment(2)

    describe("kafka broker tests") {

        context("active embeddedkafka cluster of two broker") {

            val b = 2

            beforeGroup {
                kEnv.start()
            }

            it("should have $b broker(s)") {

                kEnv.adminClient.describeCluster().nodes().get().toList().size shouldEqualTo b
            }

            it("should not be any topics available") {

                kEnv.adminClient.listTopics().names().get().toList().size shouldEqualTo 0
            }

            afterGroup {
                // nothing here
            }
        }

        context("active embeddedkafka cluster of 1 stopped broker") {

            val b = 1

            beforeGroup {
                kEnv.serverPark.brokers[0].stop()
            }

            it("should have $b broker(s)") {

                kEnv.adminClient.describeCluster().nodes().get().toList().size shouldEqualTo b
            }

            it("should not be any topics available") {

                kEnv.adminClient.listTopics().names().get().toList().size shouldEqualTo 0
            }

            afterGroup {
                // nothing here
            }
        }

        context("active embeddedkafka cluster of 1 restarted broker") {

            val b = 2

            beforeGroup {
                kEnv.serverPark.brokers[0].start()
            }

            it("should have $b broker(s)") {

                kEnv.adminClient.describeCluster().nodes().get().toList().size shouldEqualTo b
            }

            it("should not be any topics available") {

                kEnv.adminClient.listTopics().names().get().toList().size shouldEqualTo 0
            }

            afterGroup {
                // nothing here
            }
        }

        afterGroup {
            kEnv.tearDown()
        }
    }
})