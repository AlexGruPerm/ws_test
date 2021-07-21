

object Family {

  sealed abstract class FamilyMember[+A]{
    def isParent: Boolean
    def isChild: Boolean
    def isPet: Boolean
    def get: A
    final def map[B](f: A => B): FamilyMember[B] =
      this.get match {
        case _: Parent[_] => Parent(f(this.get))
        case _: Child[_] => Child(f(this.get))
        case _: Pet[_] => Pet(f(this.get))
      }
    final def flatMap[B](f: A => FamilyMember[B]): FamilyMember[B] =      
      f(this.get)
  }

  final case class Parent[+A](v: A) extends FamilyMember[A] {
    def isParent = true
    def isChild = false
    def isPet = false
    def get: A = v
  }

  final case class Child[+A](v: A) extends FamilyMember[A] {
    def isParent = false
    def isChild = true
    def isPet = false
    def get: A = v
  }

  final case class Pet[+A](v: A) extends FamilyMember[A] {
    def isParent = false
    def isChild = false
    def isPet = true
    def get: A = v
  }


}

object HumanObject{
  sealed abstract class Member{
    def name: String
  }
  case class Human(humanName: String) extends Member{
    def name: String = humanName 
  }
  
  case class Animal(animalName: String) extends Member{
    def name: String = animalName
  }
}


import Family._
import HumanObject._

val father :FamilyMember[Human] = Parent(Human("John"))
val mather :FamilyMember[Human] = Parent(Human("Mary"))
val son :FamilyMember[Human] = Child(Human("Karl"))
val cat :FamilyMember[Animal] = Pet(Animal("Alisa"))
val dog :FamilyMember[Animal] = Pet(Animal("Kuzya"))

val fml :List[FamilyMember[Member]] = List(father,mather,son,cat,dog)

