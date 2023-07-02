## AlmostLox

This is an implementation of the `JLox` language from [Crafting Interpreters](https://craftinginterpreters.com/) (part 1).

To run an `AlmostLox` script, either edit and run tests in `crafting/interpreters/LoxTest.java`
or edit `app.lox` file and run the app.

## Differences from the book implementation

The known differences are:
- parser fail fast (no synchronization) 
- no inheritance
- no resolver

Closures behave differently because of the latter.

The resolver binds variables in the original JLox.

So you'll find the following behavior:
```
{
x = 5;
fun a() {
  print x;
}
 a(); // this will print 5
 x = 3;
 a(); // this will also print 5
}
```

AlmostLox implementation won't bind the variable and will actually search for it in the enclosing
scope:

```
{
x = 5;
fun a() {
  print x;
}
 a(); // this will print 5
 x = 3;
 a(); // this will also print 3;
}
```
