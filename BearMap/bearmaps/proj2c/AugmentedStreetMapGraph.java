package bearmaps.proj2c;

import bearmaps.hw4.WeightedEdge;
import bearmaps.hw4.streetmap.Node;
import bearmaps.hw4.streetmap.StreetMapGraph;
import bearmaps.lab9.MyTrieSet;
import bearmaps.lab9.TrieSet61B;
import bearmaps.proj2ab.KDTree;
import bearmaps.proj2ab.Point;
import bearmaps.proj2ab.PointSet;
import bearmaps.proj2ab.WeirdPointSet;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * An augmented graph that is more powerful that a standard StreetMapGraph.
 * Specifically, it supports the following additional operations:
 *
 *
 * @author Alan Yao, Josh Hug, ________
 */
public class AugmentedStreetMapGraph extends StreetMapGraph {
    private PointSet kdtree;
    private Map<Point, Node> pointToNode;
    private Map<Long, Node> vertexToNode;
    private List<Point> points;
    private TrieSet61B trieSet;
    private Map<String, String> prefixMap;
    private Map<String, List<Node>> nameToNodes; //lowerCaseName

    private List<Long> routes;
    public AugmentedStreetMapGraph(String dbPath) {
        super(dbPath);
        // You might find it helpful to uncomment the line below:
        List<Node> nodes = this.getNodes();
        pointToNode = new HashMap<>();
        vertexToNode = new HashMap<>();
        points = new ArrayList<>();
        trieSet = new MyTrieSet();
        prefixMap = new HashMap<>();
        nameToNodes = new HashMap<>();
        // set = new HashSet<>();
        routes = new LinkedList<>();
        for (Node node : nodes) {
            Point p = new Point(node.lon(), node.lat());
            pointToNode.put(p, node);
            vertexToNode.put(node.id(), node);
            if (this.name(node.id()) == null) {
                points.add(p);
                routes.add(node.id());
            }else {
                // System.out.println(node.name());
                node.setName(name(node.id()));
                // System.out.println(node.name());
                String lcName = cleanString(node.name());
                prefixMap.put(lcName, node.name());
                trieSet.add(lcName);
                if (!nameToNodes.containsKey(lcName)) {
                    List<Node> list = new LinkedList<>();
                    list.add(node);
                    nameToNodes.put(lcName, list);
                }else {
                    nameToNodes.get(lcName).add(node);
                }
            }
        }
        // System.out.println(nameToNodes.size());
        kdtree = new WeirdPointSet(points);
    }


    /**
     * For Project Part II
     * Returns the vertex closest to the given longitude and latitude.
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    public long closest(double lon, double lat) {
        // Point point = new Point(lon, lat);
        // Node node = pointToNode.get(point);
        // if (node.name() == null) return 0;
        /*Long id = node.id();
        List<WeightedEdge<Long>> neighbors = this.neighbors(id);
        List<Point> list = new ArrayList<>();
        int lo = 0;
        int hi = neighbors.size() - 1;
        while (lo <= hi) {
            if (lo == hi) {
                list.add(nodeToPoint.get(neighbors.get(lo)));
            }else {
                list.add(nodeToPoint.get(neighbors.get(lo)));
                list.add(nodeToPoint.get(neighbors.get(hi)));
            }
            lo++;
            hi--;
        }
        */
        Point n = kdtree.nearest(lon, lat);
        return pointToNode.get(n).id();
    }
        // must LogN runtime

    /**
     * For Project Part III (gold points)
     * In linear time, collect all the names of OSM locations that prefix-match the query string.
     * @param prefix Prefix string to be searched for. Could be any case, with our without
     *               punctuation.
     * @return A <code>List</code> of the full names of locations whose cleaned name matches the
     * cleaned <code>prefix</code>.
     */
    public List<String> getLocationsByPrefix(String prefix) {
        prefix = cleanString(prefix);
        List<String> list = trieSet.keysWithPrefix(prefix);;
        List<String> result = new LinkedList<>();
        for (String lcName: list) {
            result.add(prefixMap.get(lcName));
        }
        return result;
    }


    /**
     * For Project Part III (gold points)
     * Collect all locations that match a cleaned <code>locationName</code>, and return
     * information about each node that matches.
     * @param locationName A full name of a location searched for.
     * @return A list of locations whose cleaned name matches the
     * cleaned <code>locationName</code>, and each location is a map of parameters for the Json
     * response as specified: <br>
     * "lat" -> Number, The latitude of the node. <br>
     * "lon" -> Number, The longitude of the node. <br>
     * "name" -> String, The actual name of the node. <br>
     * "id" -> Number, The id of the node. <br>
     */
    public List<Map<String, Object>> getLocations(String locationName) {
        String lcName = cleanString(locationName);
        if (!nameToNodes.containsKey(lcName)) throw new IllegalArgumentException();
        List<Map<String, Object>> result = new LinkedList<>();
        List<Node> nodes = nameToNodes.get(lcName);
        for (Node node : nodes) {
            Map<String, Object> location = new HashMap<>();
            location.put("lat", node.lat());
            location.put("lon", node.lon());
            location.put("name", node.name());
            location.put("id", node.id());
            result.add(location);
        }
        return result;
    }


    /**
     * Useful for Part III. Do not modify.
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    private static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }
    public List<Long> getRoutes() {
        return this.routes;
    }
    public Map<Long, Node> getPosition() {
        return this.vertexToNode;
    }
}
