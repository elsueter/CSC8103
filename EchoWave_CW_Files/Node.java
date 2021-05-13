package com.EchoWave;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class Node extends Thread{
    /**
     * @activated is a lock for the node dependant on the current step this node is on
     * @initiator dictates weather this node is the initiator
     * @ID is the ID of the node in the network it is within
     */
    private boolean activated;
    private boolean initiator;
    private int ID;

    /**
     * @parent Is a variable reference to the node that is the parent of this node
     * @neigh is an array of references to the nodes that are this nodes neighbors
     * @children is an array of references to the nodes that are this nodes chidren
     * @msgQue is a queue of incoming messages to be processed
     */
    private Node parent;
    private ArrayList<Node> neigh;
    private ArrayList<Integer> children =  new ArrayList<>();
    private ArrayList<Message> msgQue = new ArrayList<>();

    /**
     * @running is a boolean to dictate if this nodes thread should exit at the next opportunity
     * @ready is a flag to tell if the algorithm is currently being run or has run its course
     * @lock is a lock used to ensure that race conditions are not caused when accessing data from multiple threads
     */
    private boolean running = true;
    private boolean ready = true;
    private ReentrantLock lock = new ReentrantLock();

    /**
     * private class to define what a message is and make parsing messages easier
     */
    private class Message{
        String type;
        Node ID;
        Message(String in, Node inID){
            this.type = in;
            this.ID = inID;
        }
    }

    /**
     * Node constructor with a passed ID
     * @param ID the ID of the node according to the network it is a part of
     */
    Node(int ID){
        this.ID = ID;
    }

    /**
     * Function to set the neighbours of this node in the network
     * @param in An array of Nodes that this node is neighbors to
     */
    public void setNeigh(ArrayList<Node> in){
        this.neigh = in;
    }

    /**
     * @return if this node is ready
     */
    public boolean isReady(){
        return this.ready;
    }

    /**
     * @return an array of this Nodes children
     */
    public ArrayList<Integer> getChildren(){
        return this.children;
    }

    /**
     * Public function to set the node up again for another round of executions
     */
    public void reset(){
        lock.lock();
        this.initiator = false;
        this.children.clear();
        this.activated = false;
        this.parent = null;
        this.msgQue.clear();
        lock.unlock();
    }

    /**
     * The start of the nodes thread and is called on startup of the network for every node
     */
    public void run(){
        //Sets the message count to 0
        int msgCount = 0;
        //If the node is supposed to be running it will continue until otherwise with the use of the @running flag
        while(running) {
            try {
                //Checks the queue to see if any messages have come in since the last loop
                if (msgQue.size() > 0) {
                    //Locks the variables and assigns them, the lock is paramount to preserve data integrity
                    this.lock.lock();
                    //retrieves the message data and assigns them to accessible variables
                    String type = msgQue.get(0).type;
                    Node ID = msgQue.get(0).ID;
                    //and takes the message out of the queue
                    msgQue.remove(0);
                    //iterates the message count to tell the node how many mesaages have been recieved
                    msgCount++;
                    this.lock.unlock();
                    //if this node has not been messaged yet then it will read the message
                    if (!activated) {
                        //Locks the object and sets variables again
                        this.lock.lock();
                        this.parent = ID;
                        //this is paramount for the algorithm to stop this node replying to new messages
                        this.activated = true;
                        this.lock.unlock();
                        //Sends messages to each of its neighbors
                        for (int i = 0; i < this.neigh.size(); i++) {
                            if(this.neigh.get(i).ID != this.parent.ID){
                                neigh.get(i).sendMessage("EXPMsg", this);
                            }
                        }
                    }
                    //If the node is not activated but has recieved messaged from every one of its neighbors then
                    //it will continue with the next step of the algorithm.
                    else{
                        //Checks the msg type and if its an echo it will add the sending node to its children
                        if(type == "EchoMsg"){
                            this.lock.lock();
                            this.children.add(ID.ID);
                            this.lock.unlock();
                        }
                        //Checks the msgcount against its number of neighbors
                        if (msgCount == this.neigh.size()) {
                            //locks the object
                            this.lock.lock();
                            //resets the node for future executions
                            this.activated = false;
                            msgCount = 0;
                            //if the node is not the initiator it will need to echo to its parent,
                            // if not the algorithm execution has ended
                            if (!this.initiator) {
                                this.parent.sendMessage("EchoMsg", this);
                            }else{
                                this.ready = true;
                            }
                            this.lock.unlock();
                        }
                    }
                }
                //This sleep is necessary to allow multiple threads to not interfere with one another
                sleep(0, 10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This is the initiator message
     * <p>
     *     Does not require the message type or the sending node as this will be the initiator node
     * </p>
     */
    synchronized public void sendMessage(){
        this.lock.lock();
        this.ready = false;
        this.initiator = true;
        this.activated = true;
        for (int i = 0; i < this.neigh.size(); i++) {
            this.neigh.get(i).sendMessage("ExpMsg", this);
        }
        this.lock.unlock();
    }

    /**
     * This will send a message to a given node and add it to the queue of that node
     * <p>
     *     also contains the type of the message and the sender
     * </p>
     */
    synchronized public void sendMessage(String type, Node ID){
        //Locks the object
        this.lock.lock();
        //Adds to the queue and the message count
        Network.sentMessageCount++;
        msgQue.add(new Message(type, ID));
        this.lock.unlock();
    }
}
