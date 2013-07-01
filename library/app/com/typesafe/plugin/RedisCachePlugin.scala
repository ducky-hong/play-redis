package com.typesafe.plugin

import play.api.{Configuration, Application}
import play.api.cache._
import com.redis._

class RedisCachePlugin(app: Application) extends CachePlugin {

  lazy val host: String = app.configuration.getString("play.redis.plugin.host").getOrElse("localhost")
  lazy val port: Int = app.configuration.getInt("play.redis.plugin.port").getOrElse(6379)
  lazy val secret: Option[String] = app.configuration.getString("play.redis.plugin.secret")
  lazy val database: Int = app.configuration.getInt("play.redis.plugin.pool.database").getOrElse(0)
  lazy val maxIdle: Int = app.configuration.getInt("play.redis.plugin.pool.maxIdle").getOrElse(8)

  lazy val pool: RedisClientPool = new RedisClientPool(host, port, maxIdle, database, secret)

  override def enabled: Boolean = {
    !app.configuration.getString("redisplugin").filter(_ == "disabled").isDefined
  }

  override def onStart(): Unit = {
    pool
  }

  override def onStop(): Unit = {
    pool.close
  }

  lazy val api: CacheAPI = new RedisCacheImpl(pool)

}

class RedisCacheImpl(pool: RedisClientPool) extends CacheAPI {

  private object CacheDataType {
    val Boolean: Byte = 0x00;
    val Int: Byte = 0x01;
    val Long: Byte = 0x02;
    val Double: Byte = 0x03;
    val String: Byte = 0x04;
    val AnyRef: Byte = 0x05;
  }

  import serialization._
  import Parse.Implicits._
  import com.twitter.chill.KryoInjection

  def set(key: String, value: Any, expiration: Int): Unit = {
    pool.withClient { client =>
      val cacheData = value match {
        case v: Boolean => Array[Byte](CacheDataType.Boolean, if (v) 0x01 else 0x00)
        case v: Int => CacheDataType.Int +: Format.default(value)
        case v: Long => CacheDataType.Long +: Format.default(value)
        case v: Double => CacheDataType.Double +: Format.default(value)
        case v: String => CacheDataType.String +: Format.default(value)
        case v: AnyRef => CacheDataType.AnyRef +: KryoInjection(v)
        case _ => throw new MatchError(value.getClass + " does not support.")
      }
      client.set(key, cacheData)
      if (expiration != 0) {
        client.expire(key, expiration)
      }
    }
  }

  def get(key: String): Option[Any] = {
    pool.withClient { client =>
      client.get[Array[Byte]](key) flatMap { cachedData =>
        val flag = cachedData.head
        val data =cachedData.tail
        flag match {
          case CacheDataType.Boolean => Some(data.head == 1)
          case CacheDataType.Int => Some(parseInt(data))
          case CacheDataType.Long => Some(parseLong(data))
          case CacheDataType.Double => Some(parseDouble(data))
          case CacheDataType.String => Some(parseString(data))
          case CacheDataType.AnyRef => KryoInjection.invert(data)
        }
      }
    }
  }

  def remove(key: String): Unit = {
    pool.withClient { client =>
      client.del(key)
    }
  }

}