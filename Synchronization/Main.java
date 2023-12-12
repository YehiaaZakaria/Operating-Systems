import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.Scanner;

public class Main {
    public static class Router {
        private Semaphore semaphore;
        private List<Device> connectedDevices;
        private List<Device> waitingDevices;

        public Router(int connectionCount) {
            semaphore = new Semaphore(connectionCount);
            connectedDevices = new ArrayList<>(connectionCount);
            waitingDevices = new ArrayList<>();
        }

        // tries to aquire the semaphore and if it succeeds, it adds the device to the connected devices list
        // otherwise it adds the device to the waiting devices list.
        public boolean connectDevice(Device device) throws InterruptedException {
            int connectionNumber = semaphore.tryAcquire();
            device.connectionNumber = connectionNumber;
            if (connectionNumber != -1) {
                connectedDevices.add(device);
                System.out.println("- Connection " + connectionNumber + ": " + device.getDeviceName() + " occupied");
                device.performFunction(connectionNumber);
                return true;
            } else {
                waitingDevices.add(device);
                System.out.println("- "+ device.getDeviceName() + " ("+ device.getType()+ ") "+  "arrived and waiting");
                return false;
            }
        }

        // disconnects a device by removing it from the connected devices list and releases the occupied semaphore
        // if there are waiting devices, it acquires the semaphore and adds the waiting device to the connected devices list
        public void disconnectDevice(Device device) throws InterruptedException {
            connectedDevices.remove(device);
            semaphore.release(device.connectionNumber);
            checkWaitingDevices();
        }

        // checks if there are waiting devices and acquires the semaphore and adds the waiting device to the connected devices list
        private void checkWaitingDevices() throws InterruptedException {
            if (!waitingDevices.isEmpty()) {
                Device waitingDevice = waitingDevices.remove(0);
                this.connectDevice(waitingDevice);
            }
        }
    }

    public static class Semaphore {
        private int permits;
        private int totalPermits;
        private Object lock = new Object();
        boolean[] b;

        public Semaphore(int permits) {
            this.permits = permits;
            this.totalPermits = permits;
            b = new boolean[totalPermits];
            for(int i = 0; i < permits; i++){
                b[i] = false;
            }
        }

        public int tryAcquire() {
            synchronized (lock) {
                if (permits > 0) {
                    permits--;
                    for(int i = 0; i < totalPermits; i++){
                        if(b[i] == false){
                            b[i] = true;
                            return i+1;
                        }
                    }
                }
                return -1;
            }
        }

        public void release(int permitNumber) {
            synchronized (lock) {
                permits++;
                b[permitNumber-1] = false;
                lock.notify();

            }
        }
    }

    public static class Device extends Thread {
        private String name;
        private String type;
        private Router router;
        private int connectionNumber;

        public Device(String name, String type, Router router, int connectionNumber) {
            this.name = name;
            this.type = type;
            this.router = router;
            this.connectionNumber = connectionNumber;
        }

        public String getDeviceName() {
            return name;
        }

        public String getType() {
            return type;
        }

        @Override
        public void run() {

                System.out.println("- (" + name + ")(" + type + ") arrived");
            try {
                boolean output = router.connectDevice(this);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public void performFunction(int cn) throws InterruptedException {
            // Simulate online activity
            System.out.println("- Connection " + cn + ": " + name + " login");
            System.out.println("- Connection " + cn + ": " + name + " performs online activity");
            Thread.sleep((long) (100)); // Simulate online activity time

            System.out.println("- Connection " + cn + ": " + name + " Logged out");
            router.disconnectDevice(this);
        }
    }

    public static class Network {
        public static void main(String[] args) {
            Scanner scanner = new Scanner(System.in);

            // Taking inputs
            System.out.println("What is the number of WI-FI Connections?");
            int maxConnections = scanner.nextInt();

            System.out.println("What is the number of devices Clients want to connect?");
            int totalDevices = scanner.nextInt();

            Router router = new Router(maxConnections);

            // Taking device details input
            Device[] devices = new Device[totalDevices];
            for (int i = 0; i < totalDevices; i++) {
                System.out.println("Enter details for Device " + (i + 1));
                System.out.print("Name: ");
                String name = scanner.next();

                System.out.print("Type: ");
                String type = scanner.next();

                devices[i] = new Device(name, type, router, i + 1);
                //devices[i].start();
            }
            for (Device device : devices) {
                Thread t = new Thread(device);
                t.start();
            }

            scanner.close();
        }
    }

    public static void main(String[] args) {
        Network.main(args);
    }
}
