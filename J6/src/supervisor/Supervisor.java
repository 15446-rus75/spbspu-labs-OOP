package supervisor;

import program.AbstractProgram;
// import program.State;  // импорт удалён, чтобы избежать путаницы

/**
 * Супервизор – поток, наблюдающий за состояниями абстрактной программы.
 * При получении состояния STOPPING перезапускает программу.
 * При FATAL_ERROR завершает программу и останавливается сам.
 * Использует wait/notify через очередь состояний программы.
 */
public class Supervisor extends Thread {
    private final AbstractProgram program;
    private volatile boolean supervising = true;

    public Supervisor(AbstractProgram program) {
        this.program = program;
    }

    @Override
    public void run() {
        System.out.println("Supervisor started.");
        while (supervising) {
            try {
                program.State state = program.takeState(); // явно указываем program.State
                if (state == null) {
                    System.out.println("Program terminated, supervisor exiting.");
                    break;
                }
                handleState(state);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Supervisor interrupted.");
                break;
            }
        }
        System.out.println("Supervisor stopped.");
    }

    /**
     * Обрабатывает полученное состояние.
     */
    private void handleState(program.State state) {
        System.out.println("Supervisor detected state: " + state);
        switch (state) {
            case STOPPING:
                System.out.println("Supervisor: restarting program...");
                program.restartProgram(); // вызывает добавление RUNNING
                break;
            case FATAL_ERROR:
                System.out.println("Supervisor: fatal error, shutting down program...");
                program.shutdown();
                supervising = false;
                break;
            default:
                // UNKNOWN и RUNNING просто логируются
                break;
        }
    }

    /**
     * Команда супервизору запустить программу.
     */
    public void startProgram() {
        program.startProgram();
    }

    /**
     * Команда супервизору остановить программу.
     */
    public void stopProgram() {
        program.stopProgram();
    }

    /**
     * Останавливает работу супервизора.
     */
    public void shutdownSupervisor() {
        supervising = false;
        this.interrupt();
    }
}
