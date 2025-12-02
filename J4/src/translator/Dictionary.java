package translator;

import translator.exceptions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Comparator;
import java.util.Scanner;
import java.util.TreeMap;

public class Dictionary
{
  public static TreeMap<String, String> loadFromFile(String filePath)
          throws FileReadException, InvalidFileFormatException
  {

    TreeMap<String, String> dictionary = new TreeMap<>(
            Comparator.comparing(String::length).reversed().thenComparing(Comparator.naturalOrder())
    );

    Scanner scan;
    try
    {
      scan = new Scanner(new File(filePath));
    }
    catch (FileNotFoundException e)
    {
      throw new FileReadException("Unable to open file: " + e.getMessage());
    }

    while (scan.hasNextLine())
    {
      String line = scan.nextLine();
      String[] words = line.split("|");

      if (words.length != 2)
      {
        scan.close();
        throw new InvalidFileFormatException("Invalid format: " + line);
      }
      dictionary.put(words[0], words[1]);
    }
    scan.close();
    return dictionary;
  }
}
