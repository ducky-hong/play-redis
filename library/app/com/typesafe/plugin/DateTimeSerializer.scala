package com.typesafe.plugin

import com.esotericsoftware.kryo.{Kryo, Serializer}
import org.joda.time.{DateTimeZone, DateTime}
import com.esotericsoftware.kryo.io.{Input, Output}
import org.joda.time.chrono.ISOChronology

class DateTimeSerializer extends Serializer[DateTime] {

  override def write(kryo: Kryo, output: Output, obj: DateTime): Unit = {
    output.writeLong(obj.getMillis, true)
    val tz = Option(obj.getZone).filter(_ != DateTimeZone.getDefault).map(_.getID).getOrElse("")
    output.writeString(tz)
  }

  override def read(kryo: Kryo, input: Input, tpe: Class[DateTime]): DateTime = {
    val millis = input.readLong(true)
    val tz = Option(input.readString).filter(!_.isEmpty).map(DateTimeZone.forID(_)).getOrElse(DateTimeZone.getDefault)
    new DateTime(millis, ISOChronology.getInstance.withZone(tz))
  }

}
