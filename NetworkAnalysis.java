import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

public class NetworkAnalysis {

    private static EdgeWeightedGraph network, copperNetwork;
    private static LinkedList<FlowEdge> flowEdges;
    private static LinkedList<Edge>[] edges;

    public static void main(String[] args) {

        try{
            Scanner fileScanner = new Scanner(new File(args[0]));
            network = new EdgeWeightedGraph(fileScanner.nextInt());
            flowEdges = new LinkedList();

            while(fileScanner.hasNextLine()){
                Edge temp = new Edge(fileScanner.nextInt(), fileScanner.nextInt(), fileScanner.next(), fileScanner.nextInt(), fileScanner.nextInt());
                FlowEdge tempFlow = new FlowEdge(temp.either(), temp.other(temp.either()), temp.speed());
                flowEdges.add(tempFlow);
                tempFlow = new FlowEdge(temp.other(temp.either()), temp.either(), temp.speed());
                flowEdges.add(tempFlow);
                fileScanner.nextLine();
                network.addEdge(temp);
            }
        }catch(IOException e){
            System.out.println(args[1] + " not found");
        }

        edges = new LinkedList[network.V()];

        for(int i = 0; i < edges.length; i++){
            edges[i] = new LinkedList<Edge>();
            Iterator<Edge> adj = network.adj(i).iterator();
            while(adj.hasNext()){
                edges[i].add(adj.next());
            }
        }

        copperNetwork = new EdgeWeightedGraph(network.V());
        for(int i = 0; i < edges.length; i++){
            for(int j = 0; j < edges[i].size(); j++){
                if(edges[i].get(j).type().equals("copper")){
                    copperNetwork.addEdge(edges[i].get(j));
                }
            }
        }

        int selection = 0;

        while(selection != 6){
            selection = prompt();
            switch (selection){
                //case 0:
                    //System.out.println(network);
                    //break;
                case 1:
                    minLatency(getVerticies("Lowest Latency"));
                    break;
                case 2:
                    copperOnlyConnected();
                    break;
                case 3:
                    maxBandwidth(getVerticies("Maximum Bandwith"));
                    break;
                case 4:
                    minLatencyTree();
                    break;
                case 5:
                    twoFailed();
                    break;
                default:
                    break;
            }
        }
    }

    private static int prompt(){
        Scanner scan = new Scanner(System.in);
        int selection;
        System.out.println("\nNetwork Analysis Options\n1 - Get Lowest Latency Path Between Two Points\n" +
                "2 - Determine Whether Network is Copper-Only Connected\n" +
                "3 - Get Max Data Transfer From One Vertex to Another\n4 - Get Lowest Average Latency Spanning Tree\n" +
                "5 - Check Network Status on Failure of Any Two Vertices\n6 - Quit Network Analysis");
        System.out.println("\nSelect one of the options above (1-6)\n");
        selection = scan.nextInt();
        if(selection < 7 && selection > 0){
            return selection;
        }else{
            System.out.print("\nERROR: Please enter a valid option (1-6)\n");
            return prompt();
        }
    }

    private static int[] getVerticies(String prompt){
        prompt += " Test At: ";
        Scanner scan = new Scanner(System.in);
        int input[] = new int [2];
        do {
            System.out.print("\nVertex To Begin " + prompt);
            input[0] = scan.nextInt();
            if(input[0] >= network.size() || input[0] < 0){
                System.out.println("\nERROR: Please enter a valid vertex (0-" + (network.size()-1) + ")");
            }
        }while(input[0] >= network.size() || input[0] < 0);
        do {
            System.out.print("\nVertex To Finish " + prompt);
            input[1] = scan.nextInt();
            if(input[1] >= network.size() || input[1] < 0){
                System.out.println("\nERROR: Please enter a valid vertex (0-" + (network.size()-1) + ")");
            }
            if(input[0] == input[1]){
                System.out.println("\nERROR: Please enter an end vertex different from start vertex");
            }
        }while(input[1] >= network.size() || input[1] < 0 || input[0] == input[1]);
        return input;
    }

    private static void minLatency(int[] verticies){
        LatencyDijkstra path = new LatencyDijkstra(network, verticies[0]);
        LinkedList<Edge> process = path.pathTo(verticies[1]);
        String out = "" + verticies[0];
        int minBandwidth = Integer.MAX_VALUE;
        for(int i = process.size()-1; i >= 0; i--){
            out += "->" + process.get(i).other(verticies[0]);
            verticies[0] = process.get(i).other(verticies[0]);
            minBandwidth = Integer.min(process.get(i).speed(), minBandwidth);
        }
        System.out.print("\nPath:\n" + out + "\n");
        System.out.println("\nBandwidth: " + minBandwidth + " mb/s");
    }

    private static void copperOnlyConnected(){
        CC copperConnected = new CC(copperNetwork);
        boolean out = true;
        for(int i = 1; i < network.V(); i++){
            out &= copperConnected.connected(0, i);
        }
        if(out){
            System.out.println("Network is Copper-Only Connected");
        }else{
            System.out.println("Network is NOT Copper-Only Connected");
        }
    }

    private static void maxBandwidth(int[] verticies){
        FlowNetwork flowNetwork = new FlowNetwork(network.V());
        for(int i = 0; i < flowEdges.size(); i++){
            flowNetwork.addEdge(flowEdges.get(i));
        }
        FordFulkerson maxFlow;
        if(verticies[1] > verticies[0]){
            maxFlow = new FordFulkerson(flowNetwork, verticies[0], verticies[1]);
        }else{
            maxFlow = new FordFulkerson(flowNetwork, verticies[1], verticies[0]);
        }
        System.out.println("\n" + maxFlow.value() + " mb/s");
        for(int i = 0; i < flowEdges.size(); i++){
            flowEdges.get(i).resetFlow();
        }
    }

    private static void minLatencyTree(){
        PrimMST tree = new PrimMST(network);
        LinkedList<Edge> mst = tree.edges();
        System.out.println("Paths:");
        for(int i = 0; i < mst.size(); i++){
            System.out.println(mst.get(i).either() + "-" + mst.get(i).other(mst.get(i).either()));
        }
    }

    private static void twoFailed(){
        EdgeWeightedGraph temp;
        CC checkConnected;
        for(int i = 0; i < network.V(); i++){
            for(int j = i+1; j < network.V(); j++){
                temp = new EdgeWeightedGraph(network.V());
                for(int k = 0; k < edges.length; k++){
                    for(int l = 0; l < edges[k].size(); l++){
                        if(edges[k].get(l).either() != i && edges[k].get(l).other(edges[k].get(l).either()) != j && edges[k].get(l).either() != j && edges[k].get(l).other(edges[k].get(l).either()) != j)
                            temp.addEdge(edges[k].get(l));
                    }
                }
                checkConnected = new CC(temp);
                for(int k = 0; k < network.V(); k++){
                    for(int l = 0; l < network.V(); l++){
                        if(k != i && k != j && l != i && l != j && k != l){
                            if(!checkConnected.connected(k, l)){
                                System.out.println("Network Disconnected When " + i + " And " + j + " Go Offline");
                                return;
                            }
                        }
                    }

                }
            }
        }
        System.out.println("Network Remains Connected When Any Two Vertices Go Offline");
    }
}
