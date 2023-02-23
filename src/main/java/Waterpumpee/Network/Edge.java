package Waterpumpee.Network;

import java.io.Serializable;
import java.util.Optional;

public class Edge implements Serializable {
    private Node start;
    private Node end;
    private double weight;
    private String id;

    public Edge(Node start, Node end, double weight, String id) {
        this.start = start;
        this.end = end;
        this.weight = weight;
        this.id = id;

        updateEdgeConnectionInNodes();
    }

    public Optional<Edge> fromEdge(Edge edge, String id, Network context) {
        Optional<Node> startNodeOptional = context.findById(edge.start.getId());

        if(startNodeOptional.isEmpty()) {
            return Optional.empty();
        }

        Optional<Node> endNodeOptional = context.findById(edge.start.getId());

        if(endNodeOptional.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new Edge(startNodeOptional.get(), endNodeOptional.get(),
        edge.weight, id));
    }

    private void updateEdgeConnectionInNodes(){
      start.addToOutputConnections(this);
      end.addToInputConnections(this);
    }

    public double evaluate() {
        return start.getActivation() * weight;
    }

    public void perturbWeight(double by) {
        this.weight+=by;
    }

    public String getId() {
        return id;
    }

    public double getWeight() {
        return weight;
    }

    public boolean isEnabled() {
        return true;
    }

    public Node getStart() {
        return start;
    }

    public Node getEnd() {
        return end;
    }

    public void setNewEnd(Node end) {
      this.end.removeFromInputConnections(this);
      this.end = end;
    }

    public double consumeActivation(){
        double activation = getStart().getActivation();
        //keep input/activation for backprop? maybe put in array/stack? 
        //resetting is needed so i dont have to reset everyhting after 1 timestep
        //yet can keep activations from loops for next timestep (are not consumed)
        getStart().resetActivation();
        //todo: this may be utterly wrong though: one node having multiple outs
        //the 2. out would miss the activation entirely
        //maybe add a fire-threshold - if the activation is high enough fire
        //this should only be done after a basic model is working

        /*
         * OR reduce/increase (towars 0?) nodeinput only by output 
         * current. that way sigmoids are only resistors
         * how to battle inflating inputs?
         */
        return activation * weight;
    }

}
