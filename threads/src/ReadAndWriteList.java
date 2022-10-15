import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.*;

public class ReadAndWriteList<T> {
    private ArrayList<T> lockedList;

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    public ReadAndWriteList(int size, T initialValue) {
        lockedList = new ArrayList<T>(
                Collections.nCopies(size, initialValue));
    }

    public T set(int index, T element) {
        Lock writerLock = lock.readLock();
        writerLock.lock();
        try {
            System.out.println("\n>> Set index: " + index + " element: " + element + "\n");
            return lockedList.set(index, element);
        } finally {
            writerLock.unlock();
        }
    }

    public T get(int index) {
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            // Покажу, что блокировка для чтения может быть
            // получена несколькими задачами чтения:
            if (lock.getReadLockCount() > 1)
                System.out.println(">>> getReadLockCount: " + lock.getReadLockCount());
            return lockedList.get(index);
        } finally {
            readLock.unlock();
        }
    }

    public static void main(String[] args) throws Exception {
        int readers = 6;
        int writers = 1;
        new ReadAndWriteListTest(readers, writers);
    }
}
class ReadAndWriteListTest {
    ExecutorService exec = Executors.newCachedThreadPool();
    private final static int SIZE = 20;
    private static Random rand = new Random();
    private ReadAndWriteList<Integer> list = new ReadAndWriteList<Integer>(SIZE, 0);


    private class Writer implements Runnable {
        @Override
        public void run() {
            try {
                for (int i = 0; i < 20; i++) {
                    list.set(i, rand.nextInt(100));
                    TimeUnit.MILLISECONDS.sleep(100);
                }
            } catch (InterruptedException e) {
            }
            System.out.println(">> Writer finished. Shutting Down\n");
            exec.shutdown();
        }
    }

    private class Reader implements Runnable {
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    for (int i = 0; i < SIZE; i++) {
                        list.get(i);
                        TimeUnit.MILLISECONDS.sleep(1);
                    }
                }
            } catch (InterruptedException e) {
            }
        }
    }

    public ReadAndWriteListTest(int reader, int writers) {
        for (int i = 0; i < reader; i++)
            exec.execute(new Reader());
        for (int i = 0; i < writers; i++)
            exec.execute(new Writer());
    }
}