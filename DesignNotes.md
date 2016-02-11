# Design FAQ #

## Why a new language ?, I am really happy with C, Java, Python, <name you own>. ##

The aim of pseudo is to ease to teach, write and test algorithms and
their data structures. No more, no less than that.
C, Java or Python are not suitable to teach algorithmic because:
  * Teaching algorithmic in C means
> > teaching pointers and allocations.
> > Allocation in C is complex and is like a vortex compared to algorithmic.
> > A lot of hours of your time budget will be devoted to allocation in C
> > instead of algorithms and data structures.
  * Teaching algorithmic in Java means
> > using class but without constructors, without inheritance but with
> > a bunch of static methods.
> > This is not great because you don't teach idiomatic Java but ill formed Java.
> > Bad habits are hard to fight.
  * Teaching algorithmic in Python means
> > you have to restrict yourself to no use any list, range, dictionary,
> > comprehension, etc.
> > Again, you will teach twisted Python and not Pythonic Python.

## Why types aren't mandatory ? ##

Pseudo use a slightly relaxed type system because I want to be able
to explain the type system of pseudo in 5 minutes.
If you want to close the type system of pseudo and keep the same expressiveness
you have to introduce parametrized types, structural types, etc.
Concepts that you can't explain in 5 minutes.

## Why types can be specified ? ##

Because the type system of pseudo is relaxed, some operations need
to be done at runtime. This have a cost. By introducing types where
you can, you reduce that cost.

## Why overloading of function is not allowed ? ##

You don't really need it. Remember that
Moreover, I don't know an efficient way to implement multi-dispatch call at runtime.
The solution is perhaps to use the Attila Szegedi's POJO Linker.

## Why is there no semicolon after each instruction ? ##

Because you don't need semicolons to disambiguate a grammar.
Semis are only useful when you do error recovery, but even
in this case, you can do error recovery using the end of lines.

## Why you don't use if...fi like in ALGOL ? ##

The four first languages of the TIOBE index (Java, C, PHP, C++)
uses curly braces.

## Why not using spaces like in Python ? ##

I expect that pseudo will be used to teach, create and test algorithmics
so when you write an algorithm you move blocks around and I've always found
more easy to do that if block are defined by curly braces.
But I guess it depends if you IDE is able or not to help you.

## Why is there no class/inheritance/mixin/interface ? ##

Because pseudo is not a language that allow to do OOP.
Use Java or Scala instead.

## Why is there no call by reference like in C ? ##

Because you don't needed it.
There are in my opinions two use cases:
  * You allocate something on the stack and want to send it by its address.
> > Pseudo is a managed language, the runtime provide a GC
> > so this kind of problems don't exist.
  * You need to write swap.
> > Knowing that you will use swap in a sort algorithm, you can
> > rewrite it like that
```
   def swap(any[] array, int i, int j):void {
     any tmp = array[i]
     array[i] = array[j]
     array[j] = tmp
   }
```

## Why is there no bits twiddling operators ? ##

The aim is to reduce the weight of the grammar of the language
but the library named **bits** provide functions for doing shifts, mask,
rotation, etc.