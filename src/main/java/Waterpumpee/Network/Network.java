package Waterpumpee.Network;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import Waterpumpee.RandomIdService;

public class Network implements Serializable {
  private static int INIT_HIDDEN_SIZE = 0;
  private static int INPUT_SENSOR_SIZE = 64;
  private static int OUTPUT_SIZE = 64;
  private static double MUTATION_POWER = 0.2;

  private Random rand;// only use one for seeding
  private double fitness;
  private List<Node> hidden;
  private List<Edge> edges;
  private List<Node> input;
  private List<Node> output;
  private int edgeNodeIdCounter = 0;
  private String netId;

  public Network(Random rand) {
    this.rand = rand;
    this.fitness = 0;
    netId = RandomIdService.generateRandomString(rand, 16);
    hidden = new ArrayList<>();
    input = new ArrayList<>();
    output = new ArrayList<>();

    initialize();
  }

  public Network(Network toClone) {
    this.rand = toClone.rand;
    this.fitness = 0;
    this.edgeNodeIdCounter = 0;
    // todo: check whether net id already exists
    this.netId = RandomIdService.generateRandomString(rand, 16);
    this.input = deepCopyNodeList(input);
    this.output = deepCopyNodeList(output);
    this.hidden = deepCopyNodeList(hidden);
    this.edges = deepcopyAndApplyEdges(edges);
  }

  private List<Node> deepCopyNodeList(List<Node> toCopy) {
    return toCopy.stream()
        .map(n -> new Node(n, n.getId()))
        .collect(Collectors.toList());
  }

  private List<Edge> deepcopyAndApplyEdges(List<Edge> toCopy) {
    List<Edge> copiedEdges = new ArrayList<>();

    for (Edge e : toCopy) {
      Optional<Node> copyContextStartNode = findById(e.getStart().getId());

      if (copyContextStartNode.isPresent()) {
        Optional<Node> copyContextEndNode = findById(e.getEnd().getId());

        if (copyContextEndNode.isPresent()) {
          // todo: apply to nodes aswell
          Node start = copyContextStartNode.get();
          Node end = copyContextEndNode.get();
          Edge copy = new Edge(copyContextStartNode.get(),
              copyContextEndNode.get(),
              e.getWeight(),
              e.getId());
          copiedEdges.add(copy);

          start.addToOutputConnections(copy);
          end.addToInputConnections(copy);
        }
      }
    }

    return copiedEdges;
  }

  public void setFitness(double fitness) {
    this.fitness = fitness;
  }

  private void initialize() {
    input = new ArrayList<>();
    for (int i = 0; i < INPUT_SENSOR_SIZE; i++) {
      Node node = new Node(NodeType.INPUT, 0, getNextNetElementId());
      node.setActivation(0);
      input.add(node);
    }

    output = new ArrayList<>();
    for (int i = 0; i < OUTPUT_SIZE; i++) {
      Node node = new Node(NodeType.OUTPUT, 0, getNextNetElementId());
      node.setActivation(0);
      output.add(node);
    }

    hidden = new ArrayList<>();
    for (int i = 0; i < INIT_HIDDEN_SIZE; i++) {
      Node node = new Node(NodeType.HIDDEN, 0, getNextNetElementId());
      node.setActivation(0);
      hidden.add(node);
    }

    edges = new ArrayList<>();
    for (Node inputNode : input) {
      for (Node hiddenNode : hidden) {
        Edge edge = new Edge(inputNode, hiddenNode, generateWeight(), getNextNetElementId());
        edges.add(edge);
      }

      for (Node outputNode : output) {
        Edge edge = new Edge(inputNode, outputNode, generateWeight(), getNextNetElementId());
        edges.add(edge);
      }
    }

    for (Node hiddenNode : hidden) {
      for (Node outputNode : output) {
        Edge edge = new Edge(hiddenNode, outputNode, generateWeight(), getNextNetElementId());
        edges.add(edge);
      }
    }
  }

  private double generateWeight() {
    return rand.nextDouble() * 2 - 1;
  }

  private String getNextNetElementId() {
    return netId + "-" + ++edgeNodeIdCounter;
  }

  public Optional<Node> findById(String id) {
    return findById(id, hidden)
        .or(() -> findById(id, input)
            .or(() -> findById(id, output)));
  }

  private Optional<Node> findById(String id, List<Node> nodes) {
    return nodes.stream()
        .filter(e -> e.getId().equals(id))
        .findFirst();
  }

  public void setInput(List<Double> inputValues) {
    for (int i = 0; i < input.size() && i < inputValues.size(); i++) {
      input.get(i).setActivation(inputValues.get(i));
    }
  }

  public List<Double> getOutput() {
    return output.stream()
        .map(n -> n.getActivation())
        .collect(Collectors.toList());
  }

  public void propagate() {
    List<Node> unprocessed = new ArrayList<>();
    List<Node> processed = new ArrayList<>();

    unprocessed.addAll(input);

    while (!unprocessed.isEmpty()) {
      Node toProcess = unprocessed.get(0);
      unprocessed.remove(0);
      processed.add(toProcess);
      if (NodeType.HIDDEN.equals(toProcess.getNodeType())) {
        // add node input field so BP is possible todo: later
        toProcess.process();
        List<Node> toAdd = toProcess.getSuccessorNodes().stream()
            .filter(n -> !processed.contains(n))
            .filter(n -> !n.getNodeType().equals(NodeType.INPUT))
            .filter(n -> !n.getNodeType().equals(NodeType.OUTPUT))
            .collect(Collectors.toList());

        unprocessed.addAll(toAdd);
      }
    }

    for (Node n : output) {
      n.process();
    }
  }

  public void reset() {
    output.stream()
    .forEach(o -> o.resetActivation());
    this.fitness = 0;
  }

  public void saveToFile(String relativeFilePath) {
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(relativeFilePath))) {
      oos.writeObject(this);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static Network loadNetwork(String relativeFilePath) {
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(relativeFilePath))) {
      return (Network) ois.readObject();
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void mutate() {
    //todo: cache xxx*mutation power calcs
    //todo: unittest
    for (Edge e : edges) {
      if (rand.nextDouble() < 0.1 * MUTATION_POWER) {
        e.perturbWeight(rand.nextGaussian() * MUTATION_POWER);
      }
    }

    edges.stream()
        .filter(e -> e.isEnabled())
        .filter(k -> rand.nextDouble() < 0.001 * MUTATION_POWER)
        .forEach(e -> {
          Node addedNode = new Node(NodeType.HIDDEN, 0, getNextNetElementId());
          Edge addedEdge = new Edge(addedNode, e.getEnd(), 1, getNextNetElementId());
          addedNode.addToOutputConnections(addedEdge);
          addedNode.addToInputConnections(e);
          e.getEnd().addToInputConnections(addedEdge);
          hidden.add(addedNode);
          edges.add(addedEdge);
          e.setNewEnd(addedNode);
        });

    hidden.stream()
        .filter(k -> rand.nextDouble() < 0.01 * MUTATION_POWER)
        .forEach(start -> {
          edges.add(new Edge(start,
              hidden.get(rand.nextInt(hidden.size())),
              generateWeight(), getNextNetElementId()));
        });
  }

}
