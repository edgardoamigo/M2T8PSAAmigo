import java.util.*;

class Process {
    String name;
    int arrivalTime;
    List<Integer> cpuBursts;
    List<Integer> ioBursts;

    Process(String name, int arrivalTime, List<Integer> cpuBursts, List<Integer> ioBursts) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.cpuBursts = new ArrayList<>(cpuBursts);
        this.ioBursts = new ArrayList<>(ioBursts);
    }
}

class ScheduleResult {
    List<String> ganttChart = new ArrayList<>();
    int turnaroundTime;
    int waitingTime;
    int responseTime;
}

public class TraditionalProcessorScheduling {

    public static List<Process> generateProcesses(int processCount, int cpuCycles, int ioCycles) {
        List<Process> processes = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < processCount; i++) {
            String name = String.valueOf((char) ('A' + i));
            int arrivalTime = random.nextInt(10);
            List<Integer> cpuBursts = new ArrayList<>();
            List<Integer> ioBursts = new ArrayList<>();

            for (int j = 0; j < cpuCycles; j++) {
                cpuBursts.add(1 + random.nextInt(10)); // Random CPU burst
            }
            for (int j = 0; j < ioCycles; j++) {
                ioBursts.add(1 + random.nextInt(5)); // Random I/O burst
            }
            processes.add(new Process(name, arrivalTime, cpuBursts, ioBursts));
        }
        return processes;
    }

    public static ScheduleResult fcfsScheduling(List<Process> processes) {
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        ScheduleResult result = new ScheduleResult();
        int currentTime = 0;
        for (Process process : processes) {
            result.ganttChart.add("|----" + process.name + "----|");
            int arrivalTime = process.arrivalTime;
            if (arrivalTime > currentTime) {
                currentTime = arrivalTime;
            }
            int cpuTime = process.cpuBursts.stream().mapToInt(Integer::intValue).sum();
            currentTime += cpuTime;
            result.turnaroundTime += currentTime - arrivalTime;
            result.waitingTime += currentTime - arrivalTime - cpuTime;
            result.responseTime += currentTime - arrivalTime - cpuTime;
        }
        return result;
    }

    public static ScheduleResult roundRobinScheduling(List<Process> processes, int quantum) {
        Queue<Process> readyQueue = new LinkedList<>(processes);
        ScheduleResult result = new ScheduleResult();
        int currentTime = 0;
        Map<Process, Integer> responseTimes = new HashMap<>();

        while (!readyQueue.isEmpty()) {
            Process process = readyQueue.poll();
            result.ganttChart.add("|---" + process.name + "---|");

            int cpuTime = Math.min(process.cpuBursts.remove(0), quantum);
            currentTime += cpuTime;
            if (!responseTimes.containsKey(process)) {
                responseTimes.put(process, currentTime - process.arrivalTime);
            }
            if (!process.cpuBursts.isEmpty()) {
                readyQueue.add(process);
            }

            result.turnaroundTime += currentTime - process.arrivalTime;
            result.waitingTime += currentTime - process.arrivalTime - cpuTime;
        }
        result.responseTime = responseTimes.values().stream().mapToInt(Integer::intValue).sum() / processes.size();
        return result;
    }

    public static void displayGanttChart(ScheduleResult result, String algorithmName) {
        System.out.println(algorithmName + " Gantt Chart:");
        System.out.println(String.join("", result.ganttChart));
    }

    public static void displayResults(List<Process> processes, ScheduleResult fcfsResult, ScheduleResult rrResult) {
        System.out.println("------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Summary and Comparison");
        System.out.println("Process Name    Turnaround Time      Waiting Time      Response Time");
        System.out.println("                FCFS  |  RR          FCFS  |  RR       FCFS  |  RR");

        double avgTurnaroundFCFS = 0, avgTurnaroundRR = 0;
        double avgWaitingFCFS = 0, avgWaitingRR = 0;
        double avgResponseFCFS = 0, avgResponseRR = 0;

        for (Process process : processes) {
            avgTurnaroundFCFS += fcfsResult.turnaroundTime;
            avgTurnaroundRR += rrResult.turnaroundTime;
            avgWaitingFCFS += fcfsResult.waitingTime;
            avgWaitingRR += rrResult.waitingTime;
            avgResponseFCFS += fcfsResult.responseTime;
            avgResponseRR += rrResult.responseTime;

            System.out.printf("%-15s %-6d | %-6d     %-6d | %-6d   %-6d | %-6d%n",
                    process.name,
                    fcfsResult.turnaroundTime, rrResult.turnaroundTime,
                    fcfsResult.waitingTime, rrResult.waitingTime,
                    fcfsResult.responseTime, rrResult.responseTime);
        }

        int processCount = processes.size();
        System.out.printf("Average         %.2f | %.2f     %.2f | %.2f     %.2f | %.2f%n",
                avgTurnaroundFCFS / processCount, avgTurnaroundRR / processCount,
                avgWaitingFCFS / processCount, avgWaitingRR / processCount,
                avgResponseFCFS / processCount, avgResponseRR / processCount);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter # of processes: ");
        int processCount = scanner.nextInt();
        System.out.print("Enter # of CPU Cycle/Burst: ");
        int cpuCycles = scanner.nextInt();
        System.out.print("Enter # of I/O Operations: ");
        int ioCycles = scanner.nextInt();
        System.out.print("Enter Time Quantum for RR: ");
        int quantum = scanner.nextInt();

        List<Process> processes = generateProcesses(processCount, cpuCycles, ioCycles);

        System.out.println("Processes:");
        System.out.println("Process Name     Arrival Time        CPU Bursts        I/O Bursts");
        for (Process process : processes) {
            System.out.printf("%-15s %-18d %-15s %-15s%n", process.name, process.arrivalTime, process.cpuBursts, process.ioBursts);
        }

        ScheduleResult fcfsResult = fcfsScheduling(processes);
        ScheduleResult rrResult = roundRobinScheduling(processes, quantum);

        displayGanttChart(fcfsResult, "FCFS");
        displayGanttChart(rrResult, "RR");

        displayResults(processes, fcfsResult, rrResult);
    }
}
