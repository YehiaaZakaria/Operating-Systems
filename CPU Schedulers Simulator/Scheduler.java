import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


public class Scheduler {
    ArrayList<Process> processes;
    Queue<Process> waiting = new LinkedList<Process>();
    Process running;
    int CLK = 0;
    int turn = 0;
    Scheduler(ArrayList<Process> p)
    {
        running = null;
        processes = p;
    }

    void updateWaitingQueue()
    {
        while(turn < processes.size())
        {
            if(processes.get(turn).processArrivalTime <= CLK)
            {
                waiting.add(processes.get(turn));
                turn++;
            }
            else
            {
                break;
            }
        }
    }

    void mainLoop()
    {
        while(true)
        {
            updateWaitingQueue();
            if(running != null)
            {
                running.run();
                running.printProcess(CLK);
                if(running.processRemainingBurstTime == 0)
                {
                    System.out.println(running.processName+ " OUT");
                    running = null;
                }
                else if(running.processRemainingQuantum == 0)
                {
                    running.processRemainingQuantum = running.processQuantum;
                    waiting.add(running);
                    System.out.println(running.processName+ " OUT");
                    running = null;
                }
                CLK++;
            }
            else if(waiting.isEmpty())
            {
                CLK++;
            }
            else if(!waiting.isEmpty())
            {
                running = waiting.remove();

                running.run();
                running.printProcess(CLK);
                if(running.processRemainingBurstTime == 0)
                {
                    System.out.println(running.processName+ " OUT");
                    running = null;
                }
                else if(running.processRemainingBurstTime != 0 && running.processRemainingQuantum == 0)
                {
                    running.processRemainingQuantum = running.processQuantum;
                    waiting.add(running);
                    System.out.println(running.processName+ " OUT");
                    running = null;
                }
                CLK++;
            }


            if(turn == processes.size() && waiting.isEmpty())
            {
                System.out.println("we're done");
                break;
            }

        }
    }
}
