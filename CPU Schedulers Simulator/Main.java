import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;


public class Main {

    public static ArrayList<Process> readProcesses(){
        ArrayList<Process> processes = new ArrayList<Process>();
        try {
            File myObj = new File("processes.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();

                String[] parts = data.split(" ");
                Process p = new Process(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));

                processes.add(p);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        // Sort the list of processes based on arrival time
        Collections.sort(processes);
        return processes;
    }

    public static void main(String[] args) {
        ArrayList<Process> p = readProcesses();

        Scheduler s = new Scheduler(p);
        s.mainLoop();
    }

}