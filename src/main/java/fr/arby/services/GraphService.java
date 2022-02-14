package fr.arby.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.arby.beans.*;
import fr.arby.utils.DataBaseUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

@Service
public class GraphService {

    private ObjectMapper mapper = new ObjectMapper();

    public void launchPathComputing(String falconPath, String empirePath) throws IOException {
        // Init des paramètres
        Falcon falcon = mapper.readValue(Paths.get(falconPath).toFile(), Falcon.class);
        Empire empire = mapper.readValue(Paths.get(empirePath).toFile(), Empire.class);
        String absoluteDbPath = getAbsolutePath(falconPath, falcon.getRoutes_db());
        // Récupération des routes
        List<Route> routesList = DataBaseUtils.getAllRoute(absoluteDbPath);
        // Initialisation du graphe à partir des routes
        Map<String, Node> nodesMap = new HashMap<>();
        for (Route route : routesList) {
            // Récupération de l'origine et de la destination avec init si ils n'existent pas
            Node origin = nodesMap.get(route.getOrigin());
            if (origin == null) {
                origin = Node.builder()
                        .planetName(route.getOrigin())
                        .build();
                nodesMap.put(origin.getPlanetName(), origin);
            }
            Node destination = nodesMap.get(route.getDestination());
            if (destination == null) {
                destination = Node.builder()
                        .planetName(route.getDestination())
                        .build();
                nodesMap.put(destination.getPlanetName(), destination);
            }
            // Ajout de la route entre les deux noeuds
            origin.addDestination(route.getDestination(), route.getTravelTime());
            destination.addDestination(route.getOrigin(), route.getTravelTime());
        }
        // Initialisation de la présence des chasseurs de primes !
        for (BountyHunter bountyHunter : empire.getBounty_hunters()) {
            nodesMap.get(bountyHunter.getPlanet()).addBountyHunter(bountyHunter.getDay());
        }
        // Maintenant on peut se lancer dans notre algorithme de Dijkstra !
        calculateShortestPathFromSource(falcon.getDeparture(), nodesMap);
        System.out.println("");
    }

    private static String getAbsolutePath(String falconPath, String routeDbPath) {
        Path dbFilePath = Paths.get(routeDbPath);
        if (dbFilePath.isAbsolute()) {
            // chemin absolu ? Rien à faire
            return routeDbPath;
        } else { // Sinon on reconstruit le chemin à partir du falconPath
            return Paths.get(falconPath).getParent() + File.separator + routeDbPath;
        }
    }

    private static void calculateShortestPathFromSource(String source, Map<String, Node> nodeMap) {
        Node sourceNode = nodeMap.get(source);
        sourceNode.setDistance(0);
        Set<Node> settledNodes = new HashSet<>();
        Set<Node> unsettledNodes = new HashSet<>();

        // init de l'algorithme
        unsettledNodes.add(sourceNode);

        while (unsettledNodes.size() != 0) {
            Node currentNode = getLowestDistanceNode(unsettledNodes);
            unsettledNodes.remove(currentNode);
            for (Entry<String, Integer> adjacencyPair :
                    currentNode.getAdjacentNodes().entrySet()) {
                Node adjacentNode = nodeMap.get(adjacencyPair.getKey());
                Integer edgeWeight = adjacencyPair.getValue();
                if (!settledNodes.contains(adjacentNode)) {
                    calculateMinimumDistance(adjacentNode, edgeWeight, currentNode);
                    unsettledNodes.add(adjacentNode);
                }
            }
            settledNodes.add(currentNode);
        }
    }

    private static Node getLowestDistanceNode(Set<Node> unsettledNodes) {
        Node lowestDistanceNode = null;
        int lowestDistance = Integer.MAX_VALUE;
        for (Node node: unsettledNodes) {
            int nodeDistance = node.getDistance();
            if (nodeDistance < lowestDistance) {
                lowestDistance = nodeDistance;
                lowestDistanceNode = node;
            }
        }
        return lowestDistanceNode;
    }

    private static void calculateMinimumDistance(Node evaluationNode,
                                                 Integer edgeWeigh, Node sourceNode) {
        Integer sourceDistance = sourceNode.getDistance();
        if (sourceDistance + edgeWeigh < evaluationNode.getDistance()) {
            evaluationNode.setDistance(sourceDistance + edgeWeigh);
            LinkedList<Node> shortestPath = new LinkedList<>(sourceNode.getShortestPath());
            shortestPath.add(sourceNode);
            evaluationNode.setShortestPath(shortestPath);
        }
    }
}
