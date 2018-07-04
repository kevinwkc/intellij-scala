package org.jetbrains.plugins.scala.failed.annotator

import org.jetbrains.plugins.scala.annotator.LiteralTypesHighlightingTestBase
import org.jetbrains.plugins.scala.util.TestUtils

class LiteralTypesHighlightingTest extends LiteralTypesHighlightingTestBase {
  override protected def shouldPass = false
  override def folderPath = TestUtils.getTestDataPath + "/annotator/literalTypes/failed/"

  //TODO doesn't work without literal types either
  def testSip23Bounds(): Unit = doTest()

  //TODO highlights properly, but lacks dependencies, add later
  def testSip23Macros1(): Unit = doTest()

  //TODO 'Macros' does not highlight properly at all, fix this later
  def testSip23Test2(): Unit = doTest()
}
