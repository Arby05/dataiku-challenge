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
import java.util.stream.Collectors;

@Service
public class GraphService {

    private ObjectMapper mapper = new ObjectMapper();

    private Map<String, Node> nodesMap = new HashMap<>();

    public void launchPathComputing(String falconPath, String empirePath) throws IOException {
        // Init des paramètres
        Falcon falcon = mapper.readValue(new File(falconPath), Falcon.class);
        Empire empire = mapper.readValue(new File(empirePath), Empire.class);
        String absoluteDbPath = getAbsolutePath(falconPath, falcon.getRoutes_db());
        // Récupération des routes
        List<Route> routesList = DataBaseUtils.getAllRoute(absoluteDbPath);
        // Initialisation du graphe à partir des routes
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
        // Maintenant on peut se lancer dans notre algorithme de parcours en profondeur
        ComputedPath resultPath = listAllPath(nodesMap.get(falcon.getDeparture()), nodesMap.get(falcon.getArrival()),
                falcon.getAutonomy(), falcon.getAutonomy(), empire.getCountdown());
        if (resultPath == null) {
            System.out.println("Chemin trop long, la galaxie est condamnée ...");
        } else {
            System.out.println(resultPath);
        }
    }

    private String getAbsolutePath(String falconPath, String routeDbPath) {
        Path dbFilePath = Paths.get(routeDbPath);
        if (dbFilePath.isAbsolute()) {
            // chemin absolu ? Rien à faire
            return routeDbPath;
        } else { // Sinon on reconstruit le chemin à partir du falconPath
            return Paths.get(falconPath).getParent() + File.separator + routeDbPath;
        }
    }

    private ComputedPath listAllPath(Node source, Node destination, Integer maxAutonomy, Integer currentAutonomy, Integer countdown) {
        Map<String, Boolean> isVisitedMap = new HashMap<>();
        ComputedPath computedPath = new ComputedPath();
        ArrayList<Step> steps = new ArrayList<>();
        // Init du path avec notre première étape
        Step initialStep = Step.builder()
                .planetName(source.getPlanetName())
                .distanceFromPreviousJump(0)
                .build();
        computedPath.addStep(initialStep);
        // Call recursive utility
        List<ComputedPath> resultPaths = new ArrayList<>();
        recursiveFindPath(source, destination, isVisitedMap, computedPath, 0, maxAutonomy, currentAutonomy, countdown, resultPaths);
        // On va choisir notre chemin Le moins risqué et le plus court
        if (resultPaths.size() > 0) {
            return resultPaths.stream().sorted(
                    Comparator.comparingInt(ComputedPath::getBountyHunterEncounter)
                    .thenComparing(ComputedPath::getTotalDistance)).collect(Collectors.toList()).get(0);
        }
        return null;
    }

    private void recursiveFindPath(Node currentNode, Node destination, Map<String, Boolean> isVisitedMap, ComputedPath computedPath,
                                   Integer currentDay, Integer maxAutonomy, Integer currentAutonomy, Integer countdown, List<ComputedPath> resultPaths) {
        // Si on à dépssé le countdown, c'est qu'on à déjà perdu ...
        if (currentDay > countdown) {
            return;
        }
        // On est au bout du chemin, on construit notre Path, on l'ajoute à la liste des résultats et on return
        if (currentNode.equals(destination)) {
            computedPath.setTotalDistance(computedPath.getSteps().stream().mapToInt(Step::getDistanceFromPreviousJump).sum());
            resultPaths.add(new ComputedPath(computedPath));
            return;
        }
        // Ajout du noeud courant au noeud visité
        isVisitedMap.put(currentNode.getPlanetName(), true);
        // Recursivité sur tout les noeuds adjacents
        for (Map.Entry<String, Integer> adjNode : currentNode.getAdjacentNodes().entrySet()) {
            // On arrive sur une nouvelle planète. Y a t il des chasseur de prime ?
            Integer bountyHunterCount = 0;
            Boolean isBountyHunter = currentNode.getDaysWithBountyHunters().contains(currentDay);
            if (isBountyHunter) {
                bountyHunterCount++;
            }
            String nextName = adjNode.getKey();
            Integer nextDistance = adjNode.getValue();
            Node nextNode = nodesMap.get(nextName);
            Boolean isRefuel = false;
            // Doit on faire le plein avant de repartir ?
            if (currentAutonomy < nextDistance && nextDistance <= maxAutonomy) {
                isRefuel = true;
                if (isBountyHunter) {
                    bountyHunterCount++;
                }
            }
            // Si la distance à parcourir est trop grande, la planète est inaccessible, on ne peut pas suivre ce chemin
            // On vérifie aussi si on l'a déjà visité ou non pour éviter les cycles
            if (nextDistance <= maxAutonomy && (isVisitedMap.get(nextName) == null || !isVisitedMap.get(nextName))) {
                // On contruit l'étape suivante
                Step nextStep = Step.builder()
                        .planetName(nextName)
                        .distanceFromPreviousJump(nextDistance)
                        .build();
                // On mets à jour le chemin qui suit cet étape
                computedPath.getSteps().add(nextStep);
                int oldAutonomy = currentAutonomy;
                if (isRefuel) {
                    computedPath.refuel();
                    currentAutonomy = maxAutonomy;
                    currentDay++;
                }
                computedPath.bountyHunterSpotted(bountyHunterCount);
                // On calcul le fuel qu'on brûle
                currentAutonomy = currentAutonomy - nextDistance;
                // Et le temps qui passe
                currentDay = currentDay + nextDistance;
                // On saute ! Avec les nouvelles valeurs d'arrivée passé en paramètre
                recursiveFindPath(nextNode, destination, isVisitedMap, computedPath, currentDay, maxAutonomy, currentAutonomy, countdown, resultPaths);
                // Retrait des variables qui ont été modifié
                computedPath.getSteps().remove(nextStep);
                if (isRefuel) {
                    computedPath.revertRefuel();
                }
                computedPath.revertBountyHunterSpotted(bountyHunterCount);
                currentAutonomy = oldAutonomy;
                currentDay = currentDay - nextDistance;
                if (isRefuel) {
                    currentDay--;
                }
            }
        }
        isVisitedMap.put(currentNode.getPlanetName(), false);
    }

}
