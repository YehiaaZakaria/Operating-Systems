import java.util.Random;

public class Process implements Comparable<Process> {
    String processName;
    String processColor;
    int processArrivalTime;
    int processBurstTime;
    int processRemainingBurstTime;
    int processRemainingQuantum;
    int processPriorityNumber;
    int processQuantum;
    double AG_Factor;

    public Process(String processName, int processBurstTime,int processArrivalTime , int processPriorityNumber, int processQuantum) {
        this.processName = processName;
        this.processArrivalTime = processArrivalTime;
        this.processBurstTime = processBurstTime;
        this.processPriorityNumber = processPriorityNumber;
        this.processQuantum = processQuantum;
        this.processRemainingBurstTime = processBurstTime;
        this.processRemainingQuantum = processQuantum;
    }
    void printProcess(int CLK) {
        System.out.println(CLK + ": " +processName + " " + processArrivalTime+" "+ processBurstTime + " " + processRemainingBurstTime);
    }

    void run() {
        this.processRemainingQuantum--;
        this.processRemainingBurstTime--;

    }

    @Override
    public int compareTo(Process otherProcess) {
        return Integer.compare(this.processArrivalTime, otherProcess.processArrivalTime);
    }

    public double RF()
    {
        Random random = new Random();
        double randomNumber = random.nextDouble() * 20.0;
        return randomNumber;
    }

    public void calculateAGFactor() {
        double rf = RF();
        if (rf < 10) {
            AG_Factor = RF() + processArrivalTime + processBurstTime;
        } else if (rf > 10) {
            AG_Factor = 10 + processArrivalTime + processBurstTime;
        } else {
            AG_Factor = processPriorityNumber + processArrivalTime + processBurstTime;
        }
    }
}
