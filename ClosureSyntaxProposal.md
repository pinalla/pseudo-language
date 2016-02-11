# Introduction #

This is the proposal to add closure to pseudo.


# Details #

Two kinds of closures
  * block closure that allows return/break/continue
  * lambda that are just expression

# Block Closure #

Declaration of a function that takes a block closure

```
def foreach(int[] array):void (int,int:void block) {
  for(int index = 0; index<array.length; index++) {
    block(index, array[index])
  }
}
```

Call of such function:
```
{
  array = ...
  foreach(index,value: array) {
    if (index == 3) {
      continue
    }
    if (index == 7) {
      break;
    }
    if (value == 0) {
      return;
    }
  }
}
```

# Lambda #

Declaration of a lambda:
```
{
  l = lambda(x, y) x + y
}
```

Use of a lambda:
```
  print l(2, 3)   // print 5
```