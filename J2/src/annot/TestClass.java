import java.util.List;

public class TestClass
{
  @RepeatCall(2)
  public void publicMethod1(String text)
  {
    System.out.println("Public 1: " + text);
  }

  @RepeatCall(3)
  public void publicMethod2(int a, int b)
  {
    System.out.println("Public 2: " + a + " + " + b + " = " + (a + b));
  }

  @RepeatCall(1)
  public String publicMethod3(boolean flag)
  {
    String result = "Public 3: flag = " + flag;
    System.out.println(result);
    return result;
  }

  @RepeatCall(4)
  protected void protectedMethod1(double value)
  {
    System.out.println("Protected 1: " + value);
  }

  @RepeatCall(2)
  protected int protectedMethod2(String text, int multiplier)
  {
    int result = text.length() * multiplier;
    System.out.println("Protected 2: '" + text + "' * " + multiplier + " = " + result);
    return result;
  }

  @RepeatCall(1)
  protected void protectedMethod3(List< String > list)
  {
    System.out.println("Protected 3: list size = " + list.size());
  }

  @RepeatCall(3)
  private void privateMethod1(long number)
  {
    System.out.println("Private 1: " + number);
  }

  @RepeatCall(2)
  private boolean privateMethod2(String a, String b)
  {
    boolean result = a.equals(b);
    System.out.println("Private 2: '" + a + "' == '" + b + "' = " + result);
    return result;
  }

  @RepeatCall(1)
  private double privateMethod3(int x, int y, double z)
  {
    double result = (x + y) * z;
    System.out.println("Private 3: (" + x + " + " + y + ") * " + z + " = " + result);
    return result;
  }

  public void normalPublicMethod()
  {
    System.out.println("Normal public method");
  }

  protected void normalProtectedMethod()
  {
    System.out.println("Normal protected method");
  }

  private void normalPrivateMethod()
  {
    System.out.println("Normal private method");
  }
}
