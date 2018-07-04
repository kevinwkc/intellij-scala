package org.jetbrains.plugins.scala
package lang
package completion3

import com.intellij.testFramework.EditorTestUtil
import org.jetbrains.plugins.scala.debugger.{ScalaVersion, Scala_2_12}
import org.jetbrains.plugins.scala.lang.completion.clauses.ExhaustiveMatchCompletionContributor

class ScalaClausesCompletionTest extends ScalaCodeInsightTestBase {

  import EditorTestUtil.{CARET_TAG => CARET}
  import ScalaCodeInsightTestBase._

  override implicit val version: ScalaVersion = Scala_2_12

  def testSyntheticUnapply(): Unit = doCompletionTest(
    fileText =
      s"""case class Foo(foo: Int = 42)(bar: Int = 42)
         |
         |Foo()() match {
         |  case $CARET
         |}
       """.stripMargin,
    resultText =
      s"""case class Foo(foo: Int = 42)(bar: Int = 42)
         |
         |Foo()() match {
         |  case Foo(foo)$CARET
         |}
       """.stripMargin,
    item = "Foo(foo)"
  )

  def testSyntheticUnapplyVararg(): Unit = doCompletionTest(
    fileText =
      s"""case class Foo(foos: Int*)
         |
         |Foo() match {
         |  case $CARET
         |}
       """.stripMargin,
    resultText =
      s"""case class Foo(foos: Int*)
         |
         |Foo() match {
         |  case Foo(foos@_*)$CARET
         |}
       """.stripMargin,
    item = "Foo(foos@_*)"
  )

  def testUnapply(): Unit = doCompletionTest(
    fileText =
      s"""class Foo(val foo: Int = 42, val bar: Int = 42)
         |
         |object Foo {
         |  def unapply(foo: Foo): Option[(Int, Int)] = Some(foo.foo, foo.bar)
         |}
         |
         |new Foo() match {
         |  case $CARET
         |}
       """.stripMargin,
    resultText =
      s"""class Foo(val foo: Int = 42, val bar: Int = 42)
         |
         |object Foo {
         |  def unapply(foo: Foo): Option[(Int, Int)] = Some(foo.foo, foo.bar)
         |}
         |
         |new Foo() match {
         |  case Foo(i, i1)$CARET
         |}
       """.stripMargin,
    item = "Foo(i, i1)"
  )

  def testNoBeforeCaseCompletion(): Unit = checkNoCompletion(
    fileText =
      s"""case class Foo()
         |
         |Foo() match {
         |  $CARET
         |}
      """.stripMargin,
    item = "Foo()"
  )

  def testNoAfterArrowCompletion(): Unit = checkNoCompletion(
    fileText =
      s"""case class Foo()
         |
         |Foo() match {
         |  case _ => $CARET
         |}
      """.stripMargin,
    item = "Foo()"
  )

  def testNestedPatternCompletion(): Unit = doMultipleCompletionTest(
    fileText =
      s"""sealed trait Foo
         |
         |case class FooImpl(foo: Int = 42) extends Foo
         |
         |case object Bar extends Foo
         |
         |case class Baz(foo: Foo = FooImpl())
         |
         |Baz() match {
         |  case Baz(null | $CARET)
         |}
       """.stripMargin,
    items = "FooImpl(foo)", "Bar"
  )

  def testDefaultPatternCompletion(): Unit = doCompletionTest(
    fileText =
      s"""class Foo
         |
         |(_: Foo) match {
         |  case $CARET
         |}
       """.stripMargin,
    resultText =
      s"""class Foo
         |
         |(_: Foo) match {
         |  case foo: Foo$CARET
         |}
       """.stripMargin,
    item = "foo: Foo"
  )

  def testSealedTraitInheritors(): Unit = doMultipleCompletionTest(
    fileText =
      s"""sealed trait Foo {
         |  def foo: Int = 42
         |}
         |
         |class FooImpl() extends Foo
         |
         |object FooImpl {
         |  def unapply(foo: FooImpl): Option[Int] = Some(foo.foo)
         |}
         |
         |case class Bar(override val foo: Int) extends Foo
         |
         |trait Baz extends Foo
         |
         |(_: Foo) match {
         |  case $CARET
         |}
       """.stripMargin,
    items = "FooImpl(i)", "Bar(foo)", "baz: Baz"
  )

  def testCollectPatternCompletion(): Unit = doCompletionTest(
    fileText =
      s"""case class Foo(foo: Int = 42)(bar: Int = 42)
         |
         |Some(Foo()()).collect {
         |  case $CARET
         |}
       """.stripMargin,
    resultText =
      s"""case class Foo(foo: Int = 42)(bar: Int = 42)
         |
         |Some(Foo()()).collect {
         |  case Foo(foo)$CARET
         |}
       """.stripMargin,
    item = "Foo(foo)"
  )

  def testNamedPatternCompletion(): Unit = doCompletionTest(
    fileText =
      s"""case class Foo()
         |
         |Foo() match {
         |  case foo@$CARET
         |}
       """.stripMargin,
    resultText =
      s"""case class Foo()
         |
         |Foo() match {
         |  case foo@Foo()$CARET
         |}
       """.stripMargin,
    item = "Foo()"
  )

  def testSealedTrait(): Unit = doMatchCompletionTest(
    fileText =
      s"""sealed trait X
         |
         |class A(s: String) extends X
         |
         |case class B(s: String) extends X
         |
         |(_: X) $CARET
         """.stripMargin,
    resultText =
      s"""sealed trait X
         |
         |class A(s: String) extends X
         |
         |case class B(s: String) extends X
         |
         |(_: X) match {
         |  case a: A => $CARET
         |  case B(s) =>
         |}
         """.stripMargin
  )

  def testJavaEnum(): Unit = doMatchCompletionTest(
    fileText =
      s"""import java.nio.file.FileVisitResult
         |
         |(_: FileVisitResult) m$CARET
         """.stripMargin,
    resultText =
      s"""import java.nio.file.FileVisitResult
         |
         |(_: FileVisitResult) match {
         |  case FileVisitResult.CONTINUE => $CARET
         |  case FileVisitResult.TERMINATE =>
         |  case FileVisitResult.SKIP_SUBTREE =>
         |  case FileVisitResult.SKIP_SIBLINGS =>
         |}
         """.stripMargin
  )

  def testFromScalaPackage(): Unit = doMatchCompletionTest(
    fileText =
      s"""(_: List[String]) m$CARET
         """.stripMargin,
    resultText =
      s"""(_: List[String]) match {
         |  case Nil => $CARET
         |  case ::(head, tl) =>
         |}
           """.stripMargin
  )

  def testVarargs(): Unit = doMatchCompletionTest(
    fileText =
      s"""sealed trait Foo
         |
         |case class Bar(foos: Foo*) extends Foo
         |
         |(_: Foo) m$CARET
         """.stripMargin,
    resultText =
      s"""sealed trait Foo
         |
         |case class Bar(foos: Foo*) extends Foo
         |
         |(_: Foo) match {
         |  case Bar(foos@_*) => $CARET
         |}
         """.stripMargin
  )

  private def doMultipleCompletionTest(fileText: String,
                                       items: String*): Unit =
    super.doMultipleCompletionTest(fileText, items.size, DEFAULT_CHAR, DEFAULT_TIME, DEFAULT_COMPLETION_TYPE) { lookup =>
      items.contains(lookup.getLookupString)
    }

  private def doMatchCompletionTest(fileText: String, resultText: String): Unit = {
    import ExhaustiveMatchCompletionContributor.{ItemText, RendererTailText}
    super.doCompletionTest(fileText, resultText, DEFAULT_CHAR, DEFAULT_TIME, DEFAULT_COMPLETION_TYPE) {
      hasItemText(_, ItemText, ItemText, RendererTailText)
    }
  }
}
