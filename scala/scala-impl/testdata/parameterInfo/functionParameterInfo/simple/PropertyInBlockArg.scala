class Property {
  def foo(x: Int) = 1
}

def bar(l: Long): Unit = ()

val y = new Property
bar {
  y.fo<caret>o
}
//l: Long