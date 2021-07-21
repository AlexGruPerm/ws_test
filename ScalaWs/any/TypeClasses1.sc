//https://lex-kravetski.livejournal.com/583971.html
//https://lex-kravetski.livejournal.com/540744.html


val list = List( 1, 2, 3 )
val newList1 = list.map( x => x * 2 )



def fnc(p1: Int, p2:Int): Option[Int] =
  Some(p1+p2)

val newList3 = list.map( x =>  List(x,x)  )
val newList4 = list.flatMap( x => List( x, x ) )
val newList4 = list.flatMap( x => Vector( x, x ) )
val newList5 = list.flatMap( x => fnc( x, x ) )

val newList = list.flatMap( x => Option( x, x ) )