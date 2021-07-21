trait Foo
class Bar extends Foo

type IntFoo = Int => Foo

//type Function[-A, +B] — ковариантный по второму, контравариантный по первому
def x[A <: IntFoo](v : A) = v(3)

val varianceMore: Any => Bar = _ => new Bar

x(varianceMore)
