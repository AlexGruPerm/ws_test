
trait Human
  class Male extends Human
    class AdultMale extends Male
      class age20to40Male extends AdultMale
      class after40Male   extends AdultMale
        class btw40and60  extends after40Male
        class after60     extends after40Male

type IntFoo = Int => after40Male

def x[A <: IntFoo](v : A) = v(3)

val variance1: Int => after40Male = _ => new after40Male
x(variance1)

val variance2: Int => btw40and60 = _ => new btw40and60
x(variance2)

def f(v: AdultMale): Int = 1

f(new AdultMale)
f(new after40Male)
f(new after60)