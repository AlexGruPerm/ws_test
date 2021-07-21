trait Animal
class Predator extends Animal
class Herbivorous extends Animal
class PredSmall extends Predator
class Dog(name: String) extends PredSmall
class SmallDog(name: String) extends Dog(name)
trait HerbiBig extends Herbivorous
class Cow extends HerbiBig

//**********************************************

val pred = new Predator

val dog = new Dog("Buddy")

trait Vet[-T] {
  def heal(animal: T): Boolean
}

/**
 * ������� heal �������������� �� ���������,
 * ���� �� ������������� � ����� Animal, �� ��� ������� ���������� �������� Vat[Dog],
 * � ������ ����� ��������� ��� �������� �� Animal �� Dog.
*/
val myVet: Vet[Animal] = new Vet[Animal]{
  override def heal(animal: Animal): Boolean = true
}

myVet.heal(dog)

/**
 * � ���� ������������� ����� Dog, �� ��� ���������� �������� Vat[SmallDog],
 * � ������ ����� ��������� ��� �������� �� Dog �� SmallDog.
*/
val myVet2: Vet[Dog] = new Vet[Dog]{
  override def heal(animal: Dog): Boolean = true
}

myVet2.heal(dog)

val smDog: SmallDog = new SmallDog("small")

myVet2.heal(smDog)


