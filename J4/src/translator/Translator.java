package translator;

import translator.exceptions.*;

import java.util.TreeMap;
import java.util.Map;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Translator
{
  public static void translate(String fileName, TreeMap<String, String> dictionary)
          throws FileReadException
  {
    Scanner scanner;
    try
    {
      scanner = new Scanner(new File(fileName));
    }
    catch (FileNotFoundException e)
    {
      throw new FileReadException("Unable to open file: " + e.getMessage());
    }

    while (scanner.hasNextLine())
    {
      String str = scanner.nextLine();

      for (Map.Entry<String, String> entry : dictionary.entrySet())
      {
        String word = entry.getKey().toLowerCase();
        if (str.toLowerCase().contains(word))
        {
          str = str.replaceAll("(?i)" + word + "\\b", entry.getValue());
        }
      }
      System.out.println(str);
    }
    scanner.close();
  }
}
