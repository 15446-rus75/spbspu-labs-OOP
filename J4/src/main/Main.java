package main;

import translator.exceptions.*;

import translator.*;

import java.util.TreeMap;

public class Main
{
  public static void main(String[] args)
  {
    try
    {
      TreeMap<String, String> dictionary = Dictionary.loadFromFile("src/translator/dict.txt");
      Translator.translate("src/translator/text.txt", dictionary);
    }
    catch (Exception e)
    {
      System.err.println("ERROR: " + e.getMessage());
    }
  }
}
