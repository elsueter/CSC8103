package com.EchoWave;

import java.io.*;
import java.util.*;

import static java.lang.Thread.sleep;

public class Network {
    /**
     * @rand is an object for the network that is used to generate the random numbers in functions
     */
    Random rand = new Random(System.currentTimeMillis());

    /**
     * @ID stores the number that this network has been assigned
     * @sentMessageCount is a static that simply iterates on every node sending a message
     * @nodes[] contains all of the Node objects that make up this network
     * @pickedNodes[] is a queue of nodes that have been picked out as the initiators for an iteration cycle
     * @times stores the time taken to compute the echoWave per itereration
     */
    private int ID;
    public static int sentMessageCount = 0;
    private static final int iterations = 20;
    private Node nodes[];
    private int pickedNodes[];
    private ArrayList<Long> times = new ArrayList<>();
    private ArrayList<Integer> layers = new ArrayList<>();
    private ArrayList<Integer> messages = new ArrayList<>();

    /**
     * Network constructor
     * <p>
     *     Constructs the network and assigns all important valiues to each of the nodes to allow them to function
     *     and then starts the nodes running on their own thread to emulate a real world network
     * </p>
     * @param in this is the input network structure to be emulated
     * @param ID is an int to allow the network to write to a unique file after computation
     */
    Network(int[][] in, int ID){
        this.ID = ID;
        int len = in.length;
        //initiates the Node array and then assigns each value to a Node object
        nodes = new Node[len];
        for(int i = 0; i < len; i++) {
            nodes[i] = new Node(i);
        }
        //for each Node stored in the array it assigns that Node each of its neighbors and starts running a new
        // thread from their run function
        for(int i = 0; i < len; i++){
            ArrayList<Node> temp = new ArrayList<>();
            for(int j = 0; j < in[i].length; j++){
                temp.add(this.nodes[in[i][j]]);
            }
            nodes[i].setNeigh(temp);
            nodes[i].start();
        }
    }

    /**
     * function that will pick nodes and store them to the pickedNodes[] variable for later picking
     */
    private void pickNodes(){
        pickedNodes = new int[iterations];
        for(int i = 0; i < iterations; i++){
            int r = rand.nextInt(nodes.length - 1);
            pickedNodes[i] = r;
        }
    }

    /**
     * Main function called when network is asked to perform echo wave algorithm
     * <p>
     *     Naming convention was for the further development if other algorithms to generate a tree were to be
     *     implemented but not useful in this case
     * </p>
     */
    public void getTree(){
        //Deletes the current file that this network will write to if it already exists
        File file = new File("Network Solution_" + this.ID + ".txt");
        file.delete();
        //Calls pickNodes() in order to set the pickedNodes array up for picking nodes out of the network
        pickNodes();
        //Runs through a loop for every iteration that has been set for this computation which can be changed
        for(int j = 0; j < iterations; j++){
            //Terminal output to show user computation is occuring
            System.out.println("Iteration: " + j);
            //Main call function to initiate the echoWave algorithm, in cases where more funtions were avaliable
            //here would be a switch statement based on another passed variable
            echoWave(pickedNodes[j]);
        }
        //Total and avg are temperary values that are calculated before being saved to file to give useful information
        //from the times array and the number of iterations. The reason timings are not taken around the execution of
        //the full algorithm is to allow them to just time the algorithms execution not other program executions
        int total = 0;
        double avg = 0;
        for(int i = 0; i < this.times.size(); i++){
            total += this.times.get(i);
        }
        avg = total/this.times.size();
        int temp = 0;
        for(int i = 0; i < this.layers.size(); i++){
            temp += this.layers.get(i);
        }
        double avgLayers = temp/this.layers.size();
        temp = 0;
        for(int i = 0; i < this.messages.size(); i++){
            temp += this.messages.get(i);
        }
        double avgMessages = temp/this.messages.size();
        try {
            //Appends the total and avg to the end of the file this network will write to
            BufferedWriter outputStream = new BufferedWriter(new FileWriter("Network Solution_" + this.ID + ".txt", true));
            outputStream.write("Total Time: " + total + " Average time per iteration: " + avg + " Average Messages: " + avgMessages + " Average Layers: " + avgLayers);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Performs the echo wave algorithm
     * <p>
     *     Takes an int which will refer to an increment in this networks nodes array to
     *     dictate which node is the initiater in this run of the algorithm
     * </p>
     * @param root int to dictate the initiator of this echo wave which is an increment of the nodes array
     */
    private void echoWave(int root){
        //Gets the current time for future timing references
        long time = System.currentTimeMillis();
        //Sends the initiator message to the current root node which initiates the algorithm
        this.nodes[root].sendMessage();
        //While loop here is implemented to allow the system to attempt to run the algorithm again from the root node
        //in the case of an error. While loop is not the best solution here but allows the system to get over a hiccup
        //if something does go wrong
        while (!this.nodes[root].isReady()) {
            try {
                sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //Saves the solution to the file assigned to this network
        saveSolution(root, System.currentTimeMillis()-time);
        //Resets all of the nodes to allow them to be ready for another set of executions
        for (int i = 0; i < this.nodes.length; i++) {
            this.nodes[i].reset();
            //This resets so that the message count can be saved per execution
            sentMessageCount = 0;
        }
    }

    /**
     * Appends the current solution to file for the network for more perminent storage
     * <p>
     *     This method allows the program to save the solution and then overwrite its current variables without
     *     needing to keep previous solutions in memory
     * </p>
     * @param root The ID of the root node
     * @param time The time taken for the execution of this iteration
     */
    private void saveSolution(int root, long time){
        //A lot of this code is not optimised or very nice, but it was to allow for a nicer output file, that being
        //said the output is still not perfect, i hope it is still good enough however.

        //This stores the current nodes to be printed
        ArrayList<Integer> queue = new ArrayList<>();
        //sets the first node to be processed as being the root of the entire computation
        int next = root;
        //this is the initiation of the output string
        String output = "";
        //indentation levels to try and differentiate between layers in the output tree
        int lvl = 0;
        int buffer = 0;
        int layer = 0;
        //While loop here used to allow the program to run here due to the indeterminate length of the tree produced
        while(true){
            //Indentation
            for(int i = 0; i < lvl; i++){
                output+="   ";
            }
            //Formatting to show children of parent
            output += next + "[";
            //Logic for indentation
            if(this.nodes[next].getChildren().size() > 0){
                buffer = this.nodes[next].getChildren().size();
                layer++;
                lvl++;
            }else{
                buffer--;
                if(buffer == 0) {
                    lvl--;
                }
            }
            //Iterates through the nodes of the 'next' node and gets the children of that node
            for (int i = 0; i < this.nodes[next].getChildren().size(); i++) {
                //adds a string to the output based on that nodes children
                output += this.nodes[next].getChildren().get(this.nodes[next].getChildren().size()-i-1);
                //adds the children to the queue
                queue.add(0, this.nodes[next].getChildren().get(i));
                //formatting logic
                if(i < this.nodes[next].getChildren().size()-1){
                    output+=", ";
                }
            }
            //formatting
            output += "]\n";
            //checks if the queue has more nodes to process, if so sets the top of the
            //queue to the next one to be processed
            if(queue.size()>0) {
                next = queue.get(0);
                queue.remove(0);
            }else{
                break;
            }
        }
        //End of each execution time taken and total messages
        output+="\n Time taken (ms): " + time + ", No messages sent: " + sentMessageCount + " Taking " + layer + " layers to compute\n\n";
        //Adds the time taken tot he times array
        this.times.add(time);
        this.layers.add(layer);
        this.messages.add(sentMessageCount);
        try {
            //Writes the output to the file
            BufferedWriter outputStream = new BufferedWriter(new FileWriter("Network Solution_" + this.ID + ".txt", true));
            outputStream.write(output);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
