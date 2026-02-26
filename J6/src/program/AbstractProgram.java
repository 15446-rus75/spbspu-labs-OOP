package program;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Абстрактная программа, работающая в отдельном потоке-демоне.
 * Изменяет своё состояние на случайное с заданным интервалом.
 * Каждое изменение сохраняется в очереди и сопровождается уведомлением (notify).
 */
public class AbstractProgram {
    private final List<State> stateQueue = new LinkedList<>(); // очередь непрочитанных состояний
    private final Thread randomChanger;                         // поток-демон, изменяющий состояние
    private volatile boolean running = true;                    // флаг работы программы
    private final Random random = new Random();

    public AbstractProgram() {
        // начальное состояние
        addState(State.UNKNOWN);

        randomChanger = new Thread(new RandomStateChanger());
        randomChanger.setDaemon(true);
        randomChanger.start();
    }

    /**
     * Добавляет новое состояние в очередь и уведомляет ожидающих (супервизора).
     * @param state новое состояние
     */
    private void addState(State state) {
        synchronized (this) {
            stateQueue.add(state);
            this.notifyAll();
        }
        System.out.println(Thread.currentThread().getName() + " [Program] State changed to: " + state);
    }

    /**
     * Извлекает следующее состояние из очереди.
     * Если очередь пуста, блокируется до появления нового состояния или завершения программы.
     * @return очередное состояние или null, если программа завершена и очередь пуста
     * @throws InterruptedException если поток прерван во время ожидания
     */
    public State takeState() throws InterruptedException {
        synchronized (this) {
            while (stateQueue.isEmpty() && running) {
                this.wait();
            }
            if (!stateQueue.isEmpty()) {
                return stateQueue.remove(0);
            } else {
                return null; // программа завершена
            }
        }
    }

    /**
     * Запуск программы (перевод в состояние RUNNING).
     */
    public void startProgram() {
        addState(State.RUNNING);
    }

    /**
     * Остановка программы (перевод в состояние STOPPING).
     */
    public void stopProgram() {
        addState(State.STOPPING);
    }

    /**
     * Перезапуск программы (перевод в состояние RUNNING).
     * Используется супервизором при обнаружении STOPPING.
     */
    public void restartProgram() {
        addState(State.RUNNING);
    }

    /**
     * Завершение работы программы: останавливает поток-демон и оповещает супервизор.
     */
    public void shutdown() {
        running = false;
        randomChanger.interrupt(); // прерываем сон потока-демона
        synchronized (this) {
            this.notifyAll(); // чтобы супервизор вышел из ожидания, если очередь пуста
        }
    }

    // Внутренний класс – поток, случайным образом меняющий состояние
    private class RandomStateChanger implements Runnable {
        @Override
        public void run() {
            while (running) {
                try {
                    // случайная задержка от 2 до 6 секунд
                    int delay = 2000 + random.nextInt(4000);
                    Thread.sleep(delay);
                    if (!running) break;
                    State newState = getRandomState();
                    addState(newState);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // Выбор случайного состояния из трёх возможных (исключая UNKNOWN)
        private State getRandomState() {
            State[] states = {State.RUNNING, State.STOPPING, State.FATAL_ERROR};
            return states[random.nextInt(states.length)];
        }
    }
}
