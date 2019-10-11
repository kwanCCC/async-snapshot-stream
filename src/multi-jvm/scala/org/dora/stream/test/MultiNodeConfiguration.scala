package org.dora.stream.test

import akka.remote.testconductor.RoleName
import akka.remote.testkit.MultiNodeConfig
import com.typesafe.config.ConfigFactory

object MultiNodeConfiguration extends MultiNodeConfig {

  val source1: RoleName = role("source1")
  val processor1: RoleName = role("processor1")
  val sink1: RoleName = role("sink1")

  commonConfig(ConfigFactory.parseString(
    """
      |akka {
      |  loglevel = "INFO"
      |
      |  cluster {
      |    seed-nodes = []
      |    seed-nodes = ${?SEED_NODES}
      |  }
      |
      |  coordinated-shutdown.run-by-jvm-shutdown-hook = on //this is the default value
      |
      |  cluster.downing-provider-class = "tanukki.akka.cluster.autodown.MajorityLeaderAutoDowning"
      |
      |  custom-downing {
      |    stable-after = 10s
      |
      |    majority-leader-auto-downing {
      |      majority-member-role = ""
      |      down-if-in-minority = true
      |      shutdown-actor-system-on-resolution = true
      |    }
      |  }
      |
      |  actor {
      |    provider = "akka.cluster.ClusterActorRefProvider"
      |  }
      |
      |  management {
      |    cluster.bootstrap {
      |      contact-point-discovery {
      |        required-contact-point-nr = 3
      |        required-contact-point-nr = ${?REQUIRED_CONTACT_POINTS}
      |      }
      |    }
      |  }
      |
      |}
    """.stripMargin))

  nodeConfig(source1)(ConfigFactory.parseString(
    """
      |akka {
      |  remote {
      |    log-remote-lifecycle-events = on
      |    netty.tcp {
      |      hostname = "127.0.0.1"
      |      hostname = ${?HOSTNAME}
      |      bind-hostname = 0.0.0.0
      |      port = 2551
      |      port = ${?PORT}
      |    }
      |  }
      |  discovery {
      |    method = config
      |    config.services = {
      |      service1 = {
      |        endpoints = [
      |          {
      |            host = "127.0.0.1"
      |            port = 2552
      |          }
      |        ]
      |      },
      |      service2 = {
      |        endpoints = [
      |          {
      |            host = "127.0.0.1"
      |            port = 2553
      |          }
      |        ]
      |      }
      |    }
      |  }
      |  management {
      |    http {
      |      hostname = "127.0.0.1"
      |      hostname = ${?HOSTNAME}
      |      bind-hostname = "0.0.0.0"
      |      port = 8558
      |      bind-port = 8558
      |    }
      |  }
      |}
      |""".stripMargin))

  nodeConfig(processor1)(ConfigFactory.parseString(
    """
      |akka {
      |  remote {
      |    log-remote-lifecycle-events = on
      |    netty.tcp {
      |      hostname = "127.0.0.1"
      |      hostname = ${?HOSTNAME}
      |      bind-hostname = 0.0.0.0
      |      port = 2552
      |      port = ${?PORT}
      |    }
      |  }
      |  discovery {
      |    method = config
      |    config.services = {
      |      service1 = {
      |        endpoints = [
      |          {
      |            host = "127.0.0.1"
      |            port = 2551
      |          }
      |        ]
      |      },
      |      service2 = {
      |        endpoints = [
      |          {
      |            host = "127.0.0.1"
      |            port = 2553
      |          }
      |        ]
      |      }
      |    }
      |  }
      |  management {
      |    http {
      |      hostname = "127.0.0.1"
      |      hostname = ${?HOSTNAME}
      |      bind-hostname = "0.0.0.0"
      |      port = 8559
      |      bind-port = 8559
      |    }
      |  }
      |}
      |""".stripMargin))

  nodeConfig(sink1)(ConfigFactory.parseString(
    """
      |akka {
      |  remote {
      |    log-remote-lifecycle-events = on
      |    netty.tcp {
      |      hostname = "127.0.0.1"
      |      hostname = ${?HOSTNAME}
      |      bind-hostname = 0.0.0.0
      |      port = 2553
      |      port = ${?PORT}
      |    }
      |  }
      |  discovery {
      |    method = config
      |    config.services = {
      |      service1 = {
      |        endpoints = [
      |          {
      |            host = "127.0.0.1"
      |            port = 2551
      |          }
      |        ]
      |      },
      |      service2 = {
      |        endpoints = [
      |          {
      |            host = "127.0.0.1"
      |            port = 2552
      |          }
      |        ]
      |      }
      |    }
      |  }
      |  management {
      |    http {
      |      hostname = "127.0.0.1"
      |      hostname = ${?HOSTNAME}
      |      bind-hostname = "0.0.0.0"
      |      port = 8560
      |      bind-port = 8560
      |    }
      |  }
      |}
      |""".stripMargin))

}
