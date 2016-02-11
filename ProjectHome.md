## Pseudo Manifesto ##

Pseudo is:
  * a language that allow to easily teach, write and test algorithms
  * a language that you can learn in less than hour
  * a language that you don't have to learn if you already know C or Python (less than 5 minutes)
  * a language that provide gradual typing
  * a language that provides a managed environment, with a GC, etc.
  * a language that should be used to explain any algorithms on wikipedia.

There is a small language [design FAQ](DesignNotes.md)

Pseudo Language is:
  * a compiler
  * a runtime
The pseudo language is targeted to run on Java Platform 7 but you can
already use it.

Example of code: the Fibonacci's function:
```
def fib(n) {
  if (n < 2) {
    return n
  }
  return fib(n - 1) + fib(n - 2)
}
```

Because pseudo allows gradual typing, this code is also legal:
```
def fib(int n):int {
  if (n < 2) {
    return n
  }
  return fib(n - 1) + fib(n - 2)
}
```