/*
 * Simple test class
 */
public class Test
{
    int j = 0; // Not counted
    public static void main(String[] args)
    {
        Foo f = new Foo();
        f.bar();

    }
}


/**
 * A class.
 */
class Foo
{
    public void bar()
    {
      // Output
      System.out.println("FooBar"); //Not counted

      System.out.println("""
                         final value = 10;  // Not code
                         """);
    }
}

// issues/915
public class BackSlash {
    public void run()
    {
      "\\"; // 1 code + 2 blanks


      "\\"; // 1 code + 3 blanks


      /* A line comment */
    }
}
