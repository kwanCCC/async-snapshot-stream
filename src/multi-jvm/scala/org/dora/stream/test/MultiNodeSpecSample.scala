package org.dora.stream.test

import akka.actor.{Actor, Props}
import akka.remote.testkit.{MultiNodeSpec, MultiNodeSpecCallbacks}
import akka.testkit.ImplicitSender
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

//hook on scala test
trait STMultiNodeSpec extends MultiNodeSpecCallbacks with WordSpecLike with Matchers with BeforeAndAfterAll {
  self: MultiNodeSpecSample =>

  override def beforeAll() = multiNodeSpecBeforeAll()

  override def afterAll() = multiNodeSpecAfterAll()

  // Might not be needed anymore if we find a nice way to tag all logging from a node
  override implicit def convertToWordSpecStringWrapper(s: String): WordSpecStringWrapper =
    new WordSpecStringWrapper(s"$s (on node '${self.myself.name}', $getClass)")
}

//Multi JVM
class MultiNodeSampleSpecMultiJvmNode1 extends MultiNodeSpecSample

class MultiNodeSampleSpecMultiJvmNode2 extends MultiNodeSpecSample

class MultiNodeSampleSpecMultiJvmNode3 extends MultiNodeSpecSample

object MultiNodeSpecSample {

  class Ponger extends Actor {
    def receive: PartialFunction[Any, Unit] = {
      case "ping" =>
        println("receive msg from %s".format(sender().path.toSerializationFormat))
        sender() ! "pong"
    }
  }

}

//Node Sample
class MultiNodeSpecSample extends MultiNodeSpec(MultiNodeConfiguration) with STMultiNodeSpec with ImplicitSender {

  import org.dora.stream.test.MultiNodeConfiguration._
  import org.dora.stream.test.MultiNodeSpecSample._

  override def initialParticipants: Int = roles.size

  "A MultiNodeSample" must {

    "wait for all nodes to enter a barrier" in {
      enterBarrier("startup")
    }

    "send to and receive from a remote node" in {
      runOn(source1) {
        system.actorOf(Props[Ponger], "ponger")
        enterBarrier("deployed")
        val ponger = system.actorSelection(node(processor1) / "user" / "ponger")
        ponger ! "ping"
        import scala.concurrent.duration._
        expectMsg(10.seconds, "pong")
      }

      runOn(processor1) {
        system.actorOf(Props[Ponger], "ponger")
        enterBarrier("deployed")
        val ponger = system.actorSelection(node(sink1) / "user" / "ponger")
        ponger ! "ping"
        import scala.concurrent.duration._
        expectMsg(10.seconds, "pong")
      }

      runOn(sink1) {
        system.actorOf(Props[Ponger], "ponger")
        enterBarrier("deployed")
        val ponger = system.actorSelection(node(source1) / "user" / "ponger")
        ponger ! "ping"
        import scala.concurrent.duration._
        expectMsg(10.seconds, "pong")
      }

      enterBarrier("finished")
    }
  }
}