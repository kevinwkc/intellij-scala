package org.jetbrains.plugins.scala.codeInsight.implicits

import com.intellij.openapi.editor.{Inlay, InlayModel}
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement

private class Hint(val parts: Seq[Text],
                   val element: PsiElement,
                   val suffix: Boolean,
                   val menu: Option[String]) {

  def addTo(model: InlayModel): Inlay = {
    val inlay = {
      val offset = if (suffix) element.getTextRange.getEndOffset else element.getTextRange.getStartOffset
      val renderer = new TextRenderer(parts, menu)
      if (ImplicitHints.expanded) {
        renderer.expand()
      }
      model.addInlineElement(offset, suffix, renderer)
    }
    inlay.putUserData(Hint.ElementKey, element)
    inlay
  }
}

private object Hint {
  private val ElementKey: Key[PsiElement] = Key.create("SCALA_IMPLICIT_HINT_ELEMENT")

  def apply(parts: Seq[Text],
            element: PsiElement,
            suffix: Boolean,
            menu: Option[String] = None): Hint = new Hint(parts, element, suffix, menu)

  def elementOf(inlay: Inlay): PsiElement = ElementKey.get(inlay)
}