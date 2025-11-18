package main;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import annot.*;

public class Main
{
  public static void main(String[] args) throws Exception
  {
    TestClass obj = new TestClass();
    Class<?> clazz = obj.getClass();
    System.out.println("=== ВЫЗОВ АННОТИРОВАННЫХ МЕТОДОВ ===\n");
    Method[] methods = clazz.getDeclaredMethods();
    for (Method method : methods)
    {
      if (method.isAnnotationPresent(RepeatCall.class))
      {
        RepeatCall annotation = method.getAnnotation(RepeatCall.class);
        int calls = annotation.value();
        method.setAccessible(true);
        Class<?>[] paramTypes = method.getParameterTypes();
        System.out.println(method.getName() + " - " + calls);
        for (int i = 0; i < calls; i++)
        {
          Object[] arguments = createArguments(paramTypes, i);
          Object result = method.invoke(obj, arguments);
            if (result != null)
            {
              System.out.println("  -> Результат: " + result);
            }
        }
        System.out.println();
      }
    }
  }

  private static Object[] createArguments(Class<?>[] paramTypes, int callIndex)
  {
    Object[] args = new Object[paramTypes.length];
    for (int i = 0; i < paramTypes.length; i++)
    {
      if (paramTypes[i] == String.class)
      {
        args[i] = "текст_" + callIndex + "_" + i;
      }
      else if (paramTypes[i] == int.class)
      {
        args[i] = callIndex * 10 + i;
      }
      else if (paramTypes[i] == boolean.class)
      {
        args[i] = (callIndex + i) % 2 == 0;
      }
      else if (paramTypes[i] == double.class)
      {
        args[i] = callIndex * 1.5 + i * 0.1;
      }
      else if (paramTypes[i] == long.class)
      {
        args[i] = callIndex * 100L + i;
      }
      else if (paramTypes[i] == List.class)
      {
        args[i] = Arrays.asList("элемент1", "элемент2", "элемент3");
      }
      else
      {
        args[i] = paramTypes[i].isPrimitive() ? 0 : null;
      }
    }
    return args;
  }
}
