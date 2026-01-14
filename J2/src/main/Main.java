package main;

import java.util.Arrays;
import java.util.List;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

import annot.*;

public class Main
{
  public static void main(String[] args)
  {
    try
    {
      TestClass obj = new TestClass();
      Class<?> clazz = obj.getClass();
      System.out.println("=== ВЫЗОВ АННОТИРОВАННЫХ МЕТОДОВ ===\n");
      Method[] methods = clazz.getDeclaredMethods();
      for (Method method : methods)
      {
        int mod = method.getModifiers();
        if (!Modifier.isProtected(mod) && !Modifier.isPrivate(mod))
        {
          continue;
        }
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
    catch (IllegalAccessException | InvocationTargetException e)
    {
      System.out.println("Something went wrong");
      System.out.println(e.getMessage());
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
    else if (type.isArray())
    {
      return createArrayInstance(type, callIndex, paramIndex);
    }
    else if (!type.isPrimitive() && !type.isInterface())
    {
      try
      {
        return createObjectInstance(type, callIndex, paramIndex);
      }
      catch (Exception e)
      {
        System.err.println("Не удалось создать объект типа " + type.getName() + ": " + e.getMessage());
        return null;
      }
    }
    else if (type.isEnum())
    {
      Object[] enumConstants = type.getEnumConstants();
      if (enumConstants != null && enumConstants.length > 0)
      {
        return enumConstants[(callIndex + paramIndex) % enumConstants.length];
      }
    }
    return null;
  }

  private static Object createObjectInstance(Class<?> type, int callIndex, int paramIndex) throws Exception
  {
    Constructor<?>[] constructors = type.getDeclaredConstructors();
    Arrays.sort(constructors, (c1, c2) ->
        Integer.compare(c1.getParameterCount(), c2.getParameterCount()));
    for (Constructor<?> constructor : constructors)
    {
      try
      {
        constructor.setAccessible(true);
        Class<?>[] paramTypes = constructor.getParameterTypes();
        Object[] params = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++)
        {
          params[i] = createInstance(paramTypes[i], callIndex, paramIndex + i);
        }
        Object instance = constructor.newInstance(params);
        populateFields(instance, callIndex, paramIndex);
        return instance;
      }
      catch (Exception e)
      {
        continue;
      }
    }
    throw new InstantiationException("Не найден подходящий конструктор для класса " + type.getName());
  }

  private static Object createArrayInstance(Class<?> arrayType, int callIndex, int paramIndex)
  {
    Class<?> componentType = arrayType.getComponentType();
    int length = Math.max(1, (callIndex + paramIndex) % 5 + 1);
    Object array = Array.newInstance(componentType, length);
    for (int i = 0; i < length; i++)
    {
      Object element = createInstance(componentType, callIndex, paramIndex + i);
      Array.set(array, i, element);
    }
    return array;
  }

  private static void populateFields(Object instance, int callIndex, int paramIndex) throws Exception
  {
    Field[] fields = instance.getClass().getDeclaredFields();
    for (Field field : fields)
    {
      if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()))
      {
        continue;
      }
      field.setAccessible(true);
      Object value = createInstance(field.getType(), callIndex, paramIndex);
      if (value != null)
      {
        try
        {
          field.set(instance, value);
        }
        catch (Exception e)
        {
          System.err.println("Не удалось установить поле " + field.getName() + ": " + e.getMessage());
        }
      }
    }
  }
}
