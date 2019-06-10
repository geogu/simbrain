package org.simbrain.util.geneticalgorithm;

import org.simbrain.util.math.SimbrainRandomizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The top level for genetic algorithms in Simbrain. A set of agents, each of which expresses
 * a genotype and produces a fitness value at each generation.  Contains classes to initialize
 * a population and run an evolutionary simulation.
 * <p>
 * Environments are currently handled at this level.
 *
 * @param <G> The type of genes the population evolves, "genotypes"
 * @param <A> The type of agent the population evolves, "phenotypes"
 */
public class Population<G extends Genome, A extends Agent<G, A>> {

    /**
     * Number of agents in this population at a given generation.
     */
    private int size;

    /**
     * The agents in this population.
     */
    private List<A> agentList;

    /**
     * Randomizer for this simnulation
     */
    private SimbrainRandomizer randomizer;

    /**
     * Construct a population with a specified size. A seed can be set so that
     * simulations can be replicated.
     *
     * @param size size of population
     * @param seed random seed
     */
    public Population(int size, long seed) {
        this.size = size;
        this.randomizer = new SimbrainRandomizer(seed);
    }

    /**
     * Initialize the population with a set of agents, using a prototype agent.
     *
     * @param prototype the prototype agent, which spawns all agents in the population.
     */
    public void populate(A prototype) {
        agentList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            agentList.add(prototype.copy());
        }
    }

    /**
     * Computes fitness of every agent in the population
     *
     * @return the fitness of the most fit agent.
     */
    public Double computeNewFitness() {
        agentList.forEach(Agent::computeFitness);
        eliminateLeastFit(); // Must happen in this order
        return agentList.get(0).getFitness();
    }

    /**
     * Eliminate the least fit agents from the population.
     */
    private void eliminateLeastFit() {
        Collections.sort(agentList);
        Collections.reverse(agentList);
        // TODO: Make it possible to set the elimination ratio
        agentList = agentList.stream().limit(agentList.size() / 2).collect(Collectors.toList());
    }

    /**
     * Replenish the part of the population that was eliminated in {@link #eliminateLeastFit()})
     * by crossing over the genes of the remaining part of the population.
     */
    public void replenish() {
        int remainingPopulation = agentList.size();
        int reproduceSize = size - remainingPopulation;
        for (int i = 0; i < reproduceSize; i++) {
            int index1 = randomizer.nextInt(remainingPopulation);
            int index2 = randomizer.nextInt(remainingPopulation);
            while (index2 == index1) {
                index2 = randomizer.nextInt(remainingPopulation);
            }
            A agent1 = agentList.get(index1);
            A agent2 = agentList.get(index2);
            A newAgent = agent1.crossover(agent2);
            newAgent.mutate();
            agentList.add(newAgent);
        }
    }

    public List<A> getAgentList() {
        return agentList;
    }
}