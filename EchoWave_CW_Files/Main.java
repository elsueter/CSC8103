package com.EchoWave;

public class Main {
    /**
     * The three network 2D arrays are simply objects to hold the relational information of the
     * different network structures
     *
     * @network1 is the first network and reprisents the most connected of the three
     * @network2 is more reprisentative of a circular network
     * @network3 is more tree like in structure
     */
    static int network1[][] = {
            {8, 9},
            {7, 8},
            {3, 5, 6},
            {2, 4, 14},
            {3, 5},
            {2, 4, 12},
            {2, 7},
            {1, 6, 11, 12},
            {0, 1, 10},
            {0, 10},
            {8, 9, 11},
            {7, 10, 12},
            {5, 7, 11, 13},
            {12, 14},
            {3, 13}

    };

    static int network2[][] = {
            {1, 10},
            {0, 2},
            {1, 3},
            {2, 4},
            {3, 5},
            {4, 6},
            {5, 7, 11},
            {6, 8, 13, 14},
            {7, 10, 13, 9},
            {10, 8},
            {0, 8, 9},
            {6, 12},
            {11, 14},
            {7, 8},
            {7, 12}
    };

    static int network3[][] = {
            {1, 2, 3},
            {0, 4},
            {0, 5, 6},
            {0, 7},
            {1, 8, 9},
            {2, 9},
            {2, 10, 11},
            {3, 11},
            {4, 14},
            {4, 5, 13, 14},
            {6, 12, 13},
            {6, 7, 12},
            {10, 11},
            {9, 10},
            {8, 9},

    };

    /**
     * This main functio simply constructs 3 new network structures, assigns them each one of the above objects
     * and then runs the getTree() function to run the echoWave algorithm
     * @param args
     */
    public static void main(String[] args) {
        Network net = new Network(network1, 0);
        net.getTree();

        Network net2 = new Network(network2, 1);
        net2.getTree();

        Network net3 = new Network(network3, 2);
        net3.getTree();

        System.exit(0);
    }
}
