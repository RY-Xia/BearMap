package bearmaps.hw4;

import bearmaps.proj2ab.ArrayHeapMinPQ;
import edu.princeton.cs.algs4.Stopwatch;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * To summarize, our three differences from the lecture version are:
 *
 * 1.The algorithm starts with only the start vertex in the PQ.
 * 2.When relaxing an edge, if the relaxation is successful and the target vertex is not in the PQ, add it.
 * 3.If the algorithm takes longer than some timeout value, it stops running.
 *
 *
 * relax(e):
 * p = e.from(), q = e.to(), w = e.weight()
 * if distTo[p] + w < distTo[q]:
 * distTo[q] = distTo[p] + w
 * if q is in the PQ: changePriority(q, distTo[q] + h(q, goal))
 * if q is not in PQ: add(q, distTo[q] + h(q, goal))

 */

public class AStarSolver<Vertex> implements ShortestPathsSolver<Vertex> {
    private SolverOutcome outcome;
    private LinkedList<Vertex> solution = new LinkedList<>();
    private double solutionWeight;
    private double timeSpent;
    private int numStatesExplored;
    private final double INF = Double.POSITIVE_INFINITY;


    public AStarSolver(AStarGraph<Vertex> input, Vertex start, Vertex goal, double timeout) {
        ArrayHeapMinPQ<Vertex> PQ = new ArrayHeapMinPQ<>();
        Map<Vertex, Vertex> edgeTo = new HashMap<>();
        Map<Vertex, Double> distToStart = new HashMap<>();
        Map<Vertex, Double> distToGoal = new HashMap<>();

        Stopwatch sw = new Stopwatch();
        //put the start vertex into thePQ
        distToStart.put(start, 0.0);
        PQ.add(start, distToStart.get(start));

        while (PQ.size() != 0) {

            // Check whether the end is reached or not.
            if (PQ.getSmallest().equals(goal)){
                solution.addFirst(PQ.getSmallest());
                //创建一个临时指针
                Vertex curr = PQ.getSmallest();
                //当curr不是初始点的时候，循环遍历点，加入solution
                while(!curr.equals(start)){
                    solution.addFirst(edgeTo.get(curr));
                    curr = edgeTo.get(curr);
                }

            }

            List<WeightedEdge<Vertex>> neighborEdges = input.neighbors(PQ.removeSmallest());
            numStatesExplored += 1;

            // Check time spent, if exceeds the timeout, return.
            timeSpent = sw.elapsedTime();
            if (timeSpent > timeout) {
                outcome = SolverOutcome.TIMEOUT;
                solution = new LinkedList<>();
                solutionWeight = 0;
                return;
            }

            for (WeightedEdge<Vertex> edge : neighborEdges) {
                Vertex source = edge.from();
                //dest = destination
                Vertex dest = edge.to();
                double weight = edge.weight();

                if (!distToStart.containsKey(dest)) {
                    distToStart.put(dest, INF);
                }

                if (!distToGoal.containsKey(dest)) {
                    distToGoal.put(dest, input.estimatedDistanceToGoal(dest, goal));
                }

                // Relax all edges outgoing from source one at a time.
                if (distToStart.get(source) + weight < distToStart.get(dest)) {
                    distToStart.put(dest, distToStart.get(source) + weight);

                    // Update the edge used by the dest vertex.
                    edgeTo.put(dest, source);

                    if (PQ.contains(dest)) {
                        PQ.changePriority(dest, distToStart.get(dest) + distToGoal.get(dest));
                    } else {
                        PQ.add(dest, distToStart.get(dest) + distToGoal.get(dest));
                    }
                }
            }
        }
        outcome = SolverOutcome.UNSOLVABLE;
        solution = new LinkedList<>();
        solutionWeight = 0;
        timeSpent = sw.elapsedTime();
    }

    /**
     * Returns one of SolverOutcome.SOLVED, SolverOutcome.TIMEOUT,
     * or SolverOutcome.UNSOLVABLE. Should be SOLVED if the AStarSolver
     * was able to complete all work in the time given. UNSOLVABLE if
     * the priority queue became empty. TIMEOUT if the solver ran out
     * of time. You should check to see if you have run out of time
     * every time you dequeue.
     *
     * @return the outcome
     */
    public SolverOutcome outcome() {
        return outcome;
    }

    /**
     * A list of vertices corresponding to a solution. Should be empty
     * if result was TIMEOUT or UNSOLVABLE.
     *
     * @return a list of vertices corresponding to a solution
     */
    public List<Vertex> solution() {
        return solution;
    }

    /**
     * The total weight of the given solution, taking into account edge
     * weights. Should be 0 if result was TIMEOUT or UNSOLVABLE.
     *
     * @return the total weight of the given solution
     */
    public double solutionWeight() {
        return solutionWeight;
    }

    /**
     * The total number of priority queue dequeue operations.
     *
     * @return the total number of priority queue dequeue operations
     */
    public int numStatesExplored() {
        return numStatesExplored;
    }

    /**
     * The total time spent in seconds by the constructor.
     *
     * @return the total time spent in seconds by the constructor
     */
    public double explorationTime() {
        return timeSpent;
    }
}