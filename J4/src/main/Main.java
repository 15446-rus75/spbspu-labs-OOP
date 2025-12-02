package main;

import translator.exceptions.*;

import java.util.TreeMap;

public class Main
{
  public static void main(String[] args)
  {
    try
    {
      TreeMap<String, String> dictionary = Dictionary.loadFromFile("src/translator/dict");
      Translator.translate("src/translator/book", dictionary);
    }
    catch (Exception e)
    {
      System.err.println("ERROR: " + e.getMessage());
    }
  }
}
