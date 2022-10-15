import java.util.concurrent.*;

class Manager {
    Semaphore semaphore = new Semaphore(4, true);
    String notes;
    boolean work = true;

    public void read() {
        int numOfPermission = semaphore.availablePermits();
        if (numOfPermission < 0) {
            semaphore.release(3);
        }
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
        }
        Thread t = Thread.currentThread();
        System.out.println(t.getName() + " try...");
        System.out.println(t.getName() + " reading...");
        System.out.println("Priority is --" + t.getPriority());

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
        }
        System.out.println(t.getName() + ":" + Minimum(notes));
        semaphore.release();
    }

    void write(String str) {
        int numerOfPerm = semaphore.availablePermits();
        if (numerOfPerm >= 4) {
            try {
                semaphore.acquire();
                semaphore.acquire();
                semaphore.acquire();
                semaphore.acquire();
            } catch (InterruptedException e) {
            }
            Thread t = Thread.currentThread();
            System.out.println(t.getName() + " try...");
            System.out.println(t.getName() + " writing...");
            System.out.println("Priority is --" + t.getPriority());
            System.out.println();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
            notes = str;
            System.out.println();
            System.out.println(t.getName() + ":" + notes);
            System.out.println();
            semaphore.release();
            semaphore.release();
            semaphore.release();
            semaphore.release();
        }
    }

    static int Minimum(String str) {
        int[] n = null;
        try {
            String[] nn = str.split(" ");
            n = new int[nn.length];
            for (int i = 0; i < nn.length; i++)
                n[i] = Integer.parseInt(nn[i] + "");
        } catch (Exception r) {
        }
        if (n == null) {
            return 0;
        }
        int min = n[0];
        for (int i = 1; i < n.length; i++) {
            if (min > n[i]) min = n[i];
        }
        return min;
    }
}

class Writer implements Runnable {
    Manager manager;

    Writer(Manager manager, int num) {
        this.manager = manager;
        new Thread(this, "Writer " + num).start();
    }

    public void run() {
        while (manager.work) {
            manager.write(NumberToString(GenerateNatural(10)));
        }
    }

    int[] GenerateNatural(int n) {
        int[] k = new int[n];
        for (int i = 0; i < k.length; i++) {
            k[i] = (int) (Math.random() * 100);
        }
        return k;
    }

    String NumberToString(int[] n) {
        String str = "";
        for (int i = 0; i < n.length; i++) {
            str += n[i] + " ";
        }
        return str;
    }
}

class Reader implements Runnable {
    Manager manager;

    Reader(Manager manager, int num) {
        this.manager = manager;
        new Thread(this, "Reader " + num).start();
    }

    public void run() {
        while (manager.work) {
            manager.read();
        }
    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Manager manager = new Manager();
        new Writer(manager, 1);
        new Writer(manager, 2);
        new Reader(manager, 3);
        new Reader(manager, 4);
        new Writer(manager, 1);
        new Writer(manager, 2);
        new Reader(manager, 3);
        new Reader(manager, 4);
        Thread.sleep(2000);
        manager.work = false;
    }
}