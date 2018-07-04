package org.jetbrains.plugins.scala
package lang
package completion
package clauses

import com.intellij.codeInsight.completion._
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.jetbrains.plugins.scala.extensions._
import org.jetbrains.plugins.scala.lang.completion.lookups.ScalaLookupItem
import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.{ScCaseClause, ScStableReferenceElementPattern}
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ScObject, ScTypeDefinition}

class CaseClauseCompletionContributor extends ScalaCompletionContributor {

  extend(CompletionType.BASIC,
    PlatformPatterns.psiElement.inside(classOf[ScCaseClause]),
    new ScalaCompletionProvider {

      override protected def completionsFor(position: PsiElement)
                                           (implicit parameters: CompletionParameters, context: ProcessingContext): Iterable[ScalaLookupItem] = {
        val maybeClass = position.findContextOfType(classOf[ScStableReferenceElementPattern])
          .flatMap(_.expectedType)
          .flatMap(_.extractClass)

        val targetClasses = maybeClass match {
          case Some(scalaClass: ScTypeDefinition) if scalaClass.isSealed => findInheritors(scalaClass)
          case Some(scalaClass: ScTypeDefinition) => Seq(scalaClass)
          case _ => Iterable.empty
        }

        // TODO find conflicting CompletionContributor
        targetClasses.filterNot(_.isInstanceOf[ScObject]).map { clazz =>
          val result = new ScalaLookupItem(clazz, patternText(clazz)(position))
          result.isLocalVariable = true
          result
        }
      }
    }
  )
}