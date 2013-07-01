package com.typesafe.plugin

import org.specs2.mutable.Specification

import play.api.cache._
import play.api.test._
import play.api.test.Helpers._

class RedisCachePluginSpec extends Specification {

  abstract class WithRedisPlugin extends WithApplication(FakeApplication(
    additionalPlugins = List("com.typesafe.plugin.RedisCachePlugin")))

  "String value" should {

    "be retrieved" in new WithRedisPlugin {
      Cache.set("earth", "wind")
      Cache.get("earth") should beSome("wind")
      Cache.getAs[String]("earth") should beSome("wind")
    }

    "be retrieved before expiration" in new WithRedisPlugin {
      Cache.set("earth", "wind", 1)
      Cache.get("earth") should beSome("wind")
    }

    "be removed after expiration" in new WithRedisPlugin {
      Cache.set("earth", "wind", 1)
      Thread.sleep(500)
      Cache.get("earth") should beSome("wind")
      Thread.sleep(500)
      Cache.get("earth") should beNone
    }

    "be removed manually" in new WithRedisPlugin {
      Cache.set("earth", "wind")
      Cache.get("earth") should beSome("wind")
      Cache.remove("earth")
      Cache.get("earth") should beNone
    }

  }

  "Primitive value other than String" should {

    "be retrived as Int" in new WithRedisPlugin {
      Cache.set("earth", 42)
      Cache.getAs[Int]("earth") should beSome(42)
    }

    "be retrived as Long" in new WithRedisPlugin {
      Cache.set("earth", 42L)
      Cache.getAs[Long]("earth") should beSome(42L)
    }

    "be retrived as Double" in new WithRedisPlugin {
      Cache.set("earth", 42.0)
      Cache.getAs[Double]("earth") should beSome(42.0)
    }

    "be retrived as Boolean" in new WithRedisPlugin {
      Cache.set("earth", true)
      Cache.getAs[Boolean]("earth") should beSome(true)
    }

  }

  "Case class value" should {

    "be retrived" in new WithRedisPlugin {
      val dog = Dog("John", 8)
      Cache.set("earth", dog)
      Cache.get("earth") should beSome(dog)
    }

  }

}
