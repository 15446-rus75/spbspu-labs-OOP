package main;

import animals.*;
import java.util.*;

public class Main
{
  public static void segregate(Collection<? extends Animal> srcCollection,
                               Collection<? super Hedgehog> collection1,
                               Collection<? super Manul> collection2,
                               Collection<? super Lynx> collection3)
  {
    for (Animal animal : srcCollection)
    {
      if (animal instanceof Hedgehog)
      {
        collection1.add((Hedgehog) animal);
      }
      else if (animal instanceof Manul)
      {
        collection2.add((Manul) animal);
      }
      else if (animal instanceof Lynx)
      {
        collection3.add((Lynx) animal);
      }
    }
  }

  public static void main(String[] args)
  {
    Hedgehog hedgehog1 = new Hedgehog("Ёжик-1");
    Hedgehog hedgehog2 = new Hedgehog("Ёжик-2");
    Manul manul1 = new Manul("Манул-1");
    Manul manul2 = new Manul("Манул-2");
    Lynx lynx1 = new Lynx("Рысь-1");
    Lynx lynx2 = new Lynx("Рысь-2");
    Insectivore insectivore = new Insectivore("Насекомоядное");
    Felidae felidae = new Felidae("Кошачье");
    Mammal mammal = new Mammal("Млекопитающее");
    Chordate chordate = new Chordate("Хордовое");

    System.out.println("=== Демонстрация 1 ===");
    List<Mammal> mammals = Arrays.asList(hedgehog1, manul1, lynx1, mammal);
    List<Hedgehog> hedgehogs1 = new ArrayList<>();
    List<Felidae> felidaes = new ArrayList<>();
    List<Mammal> predators1 = new ArrayList<>();

    segregate(mammals, hedgehogs1, felidaes, predators1);

    System.out.println("Исходная коллекция (Млекопитающие): " + mammals);
    System.out.println("Ежи: " + hedgehogs1);
    System.out.println("Кошачьи (манулы): " + felidaes);
    System.out.println("Хищные (рыси): " + predators1);

    System.out.println("\n=== Демонстрация 2 ===");
    List<Mammal> predators2 = Arrays.asList(hedgehog2, manul2, lynx2);
    List<Chordate> chordates = new ArrayList<>();
    List<Manul> manuls = new ArrayList<>();
    List<Felidae> cats = new ArrayList<>();

    segregate(predators2, chordates, manuls, cats);

    System.out.println("Исходная коллекция (Хищные): " + predators2);
    System.out.println("Хордовые (ежи): " + chordates);
    System.out.println("Манулы: " + manuls);
    System.out.println("Кошачьи (рыси): " + cats);

    System.out.println("\n=== Демонстрация 3 ===");
    List<Hedgehog> hedgehogFamily = Arrays.asList(hedgehog1, hedgehog2);
    List<Insectivore> insectivores = new ArrayList<>();
    List<Mammal> predators3a = new ArrayList<>();
    List<Mammal> predators3b = new ArrayList<>();

    segregate(hedgehogFamily, insectivores, predators3a, predators3b);

    System.out.println("Исходная коллекция (Ежовые): " + hedgehogFamily);
    System.out.println("Насекомоядные (ежи): " + insectivores);
    System.out.println("Хищные (манулы): " + predators3a);
    System.out.println("Хищные (рыси): " + predators3b);
  }
}
