package org.digitalpanda.flink.common

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable.ArrayBuffer

class JobConfTest extends AnyFlatSpec with Matchers {

  "JobConf" should "iterate over array config" in {
    // Given
    val actual = new ArrayBuffer[Long]()
    val jobConf = JobConf("application-test.conf")

    // when
    jobConf.forEach("somewhere.an-array"){
      conf => actual.append(conf.getLong("a-key"))
    }

    // Then
    actual.toList should equal (List(1, 2, 3))
  }
}