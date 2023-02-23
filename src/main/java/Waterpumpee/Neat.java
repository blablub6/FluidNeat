package Waterpumpee;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import Waterpumpee.Network.Network;

public class Neat {

    // Parameters for the NEAT algorithm
    int populationSize;
    double mutationRate;

    // The current population of networks
    ArrayList<Network> population;

    // Random number generator
    Random rand;

    public Neat(int populationSize, double mutationRate) {
        this.populationSize = populationSize;
        this.mutationRate = mutationRate;
        population = new ArrayList<Network>();
        rand = new Random();
        initializePopulation();
    }

    // Initialize the population with randomly generated networks
    public void initializePopulation() {
        for (int i = 0; i < populationSize; i++) {
            population.add(new Network(rand));
        }
    }

    // Evaluate the fitness of all networks in the population
    public void evaluateFitness() {
        for (Network n : population) {
          //todo: set inputs
          double summand = rand.nextInt(100);
          double summand2 = rand.nextInt(100);
          double sum = summand + summand2;
          n.setInput(List.of(summand, summand2));
          n.propagate();//how often? rly depends on the task, no?
          List<Double> result = n.getOutput();
          n.setFitness(Math.pow(sum - result.get(0),2));//e.g. % of task solved or score
        }
    }

    // Select parents for breeding using the NEAT selection method
    public Network[] selectParents() {
        // Code to implement the NEAT selection method
    }

    // Perform one generation of the NEAT algorithm
    public void nextGeneration() {
        // Code to implement the NEAT algorithm
        //breed pop from selected parents
    }
}
