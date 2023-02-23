package Waterpumpee.Network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Node implements Serializable {
    
    private NodeType nodeType;
    private double input;
    private double activation;
    private int bias;
    private String id;
    private List<Edge> inputConnections;
    private List<Edge> outputConnections;

    Node(NodeType type, int bias, String id) {
        this.nodeType = type;
        this.activation = 0;
        this.input = 0;
        this.bias = bias;
        this.id = id;
        inputConnections = new ArrayList<>();
        outputConnections = new ArrayList<>();
    }

    Node(Node toClone, String id){
        this.nodeType = toClone.nodeType;
        this.activation = 0;
        this.input = 0;
        this.bias = toClone.bias;
        //clones node without network context
        inputConnections = new ArrayList<>();
        outputConnections = new ArrayList<>();
        this.id = id;
    }

    public double getActivation() {
        return this.activation;
    }

    public void setActivation(double activation) {
        this.activation = activation;
    }

    public void perturbBias(double by) {
        this.bias+=by;
    }

    public void removeFromInputConnections(Edge e) {
        inputConnections.remove(e);
    }

    public void addToInputConnections(Edge e) {
        inputConnections.add(e);
    }

    public void removeFromOutputConnections(Edge e) {
        outputConnections.remove(e);
    }

    public void addToOutputConnections(Edge e) {
        outputConnections.add(e);
    }

    public String getId() {
        return id;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public double process() {
        input += inputConnections.stream()
        .map(connection -> connection.consumeActivation())
        .reduce(0.0, Double::sum);

        activation += sigmoid(input);
        return activation;
    }

    public void shiftInputToActivation(){
        activation += input;
        input = 0;
    }

    public static double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
      }
      
      public static double sigmoidDerivative(double x) {
        double s = sigmoid(x);
        return s * (1 - s);
      }

    public void resetActivation() {
        activation = 0;
        input = 0;
    }

    public List<Node> getSuccessorNodes(){
        return outputConnections.stream()
        .map(e -> e.getEnd())
        .collect(Collectors.toList());
    }

}
