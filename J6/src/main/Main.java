package main;

import program.AbstractProgram;
import supervisor.Supervisor;

/**
 * Главный класс, демонстрирующий работу супервизора и абстрактной программы.
 */
public class Main {
    public static void main(String[] args) {
        AbstractProgram program = new AbstractProgram();
        Supervisor supervisor = new Supervisor(program);
        supervisor.start();

        // Демонстрация управления
        try {
            Thread.sleep(2000);                      // даём программе поработать самостоятельно
            System.out.println("Main: starting program...");
            supervisor.startProgram();

            Thread.sleep(5000);
            System.out.println("Main: stopping program...");
            supervisor.stopProgram();

            Thread.sleep(5000);
            System.out.println("Main: exiting, will shutdown supervisor.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            supervisor.shutdownSupervisor();
            program.shutdown();                       // корректное завершение программы
        }
    }
}
