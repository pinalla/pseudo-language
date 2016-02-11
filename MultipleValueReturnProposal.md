# Introduction #

Pseudo currently has no way to return multiple values hence
users use some workarounds like creating a record for that.
This proposal define a syntax to allow a function to return
multiple values.

# Details #

## Function with multiple return types ##

```
  def f(int i):int, double {
    ...
  }
```

## Multiple return values ##

```
  def f(int i):int, double {
    return 2, 4.0
  }  
```

## Multiple assignations ##

```
  a, b = 2, 3
```

### Multiple assignations with some variables omitted ###

```
   _,b = f(3)
```