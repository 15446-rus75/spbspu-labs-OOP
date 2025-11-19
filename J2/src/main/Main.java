package main;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
      args[i] = createInstance(paramTypes[i], callIndex, i);
    }
    return args;
  }

  private static Object createInstance(Class<?> type, int callIndex, int paramIndex)
  {
    if (type == String.class)
    {
      return "текст_" + callIndex + "_" + paramIndex;
    }
    else if (type == int.class || type == Integer.class)
    {
      return callIndex * 10 + paramIndex;
    }
    else if (type == boolean.class || type == Boolean.class)
    {
      return (callIndex + paramIndex) % 2 == 0;
    }
    else if (type == double.class || type == Double.class)
    {
      return callIndex * 1.5 + paramIndex * 0.1;
    }
    else if (type == long.class || type == Long.class)
    {
      return callIndex * 100L + paramIndex;
    }
    else if (type == float.class || type == Float.class)
    {
      return callIndex * 1.5f + paramIndex * 0.1f;
    }
    else if (type == byte.class || type == Byte.class)
    {
      return (byte) ((callIndex + paramIndex) % 128);
    }
    else if (type == short.class || type == Short.class)
    {
      return (short) (callIndex * 10 + paramIndex);
    }
    else if (type == char.class || type == Character.class)
    {
      return (char) ('A' + (callIndex + paramIndex) % 26);
    }
    else if (List.class.isAssignableFrom(type))
    {
      return Arrays.asList("элемент1_" + callIndex, "элемент2_" + paramIndex, "элемент3");
    }
    else if (type.getName().contains("Optional"))
    {
      try
      {
        Object value = createInstance(String.class, callIndex, paramIndex);
        return type.getMethod("of", Object.class).invoke(null, value);
      }
      catch (Exception e)
      {
        return null;
      }
    }
    else if (!type.isPrimitive() && !type.isInterface())
    {
      try
      {
        Object instance = type.getDeclaredConstructor().newInstance();
        populateFields(instance, callIndex, paramIndex);
        return instance;
      }
      catch (Exception e)
      {
        return null;
      }
    }
    else
    {
      return null;
    }
  }

  private static void populateFields(Object instance, int callIndex, int paramIndex)
  {
    try
    {
      Field[] fields = instance.getClass().getDeclaredFields();
      for (Field field : fields)
      {
        if (java.lang.reflect.Modifier.isStatic(field.getModifiers()))
        {
          continue;
        }
        field.setAccessible(true);
        Object value = createInstance(field.getType(), callIndex, paramIndex);
        if (value != null)
        {
          field.set(instance, value);
        }
      }
    }
    catch (Exception e)
    {}
  }

}
