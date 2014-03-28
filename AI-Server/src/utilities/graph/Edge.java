package utilities.graph;

/**
 * http://www.algolist.com/code/java/Dijkstra%27s_algorithm
 */
public class Edge
{
    public final Vertex target;
    public final double weight;
    public Edge(Vertex argTarget, double argWeight)
    { target = argTarget; weight = argWeight; }
}