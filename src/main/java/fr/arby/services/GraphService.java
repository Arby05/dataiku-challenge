package fr.arby.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.arby.beans.*;
import fr.arby.utils.DataBaseUtils;
import fr.arby.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GraphService {

    private ObjectMapper mapper = new ObjectMapper();

    private Map<String, Node> nodesMap = new HashMap<>();

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public Double computeSuccessProbability(String falconPath, String empirePath) throws IOException {
        // Init des paramètres
        Falcon falcon = mapper.readValue(new File(falconPath), Falcon.class);
        Empire empire = mapper.readValue(new File(empirePath), Empire.class);
        String absoluteDbPath = Utils.getAbsolutePath(falconPath, falcon.getRoutes_db());
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
                falcon.getAutonomy(), empire.getCountdown());
        if (resultPath == null) {
            LOGGER.error("Chemin trop long, la galaxie est condamnée ...");
            return 0d;
        } else {
            LOGGER.info(resultPath.toString());
            resultPath.computeProbability();
            return resultPath.getProbability();
        }
    }

    private ComputedPath listAllPath(Node source, Node destination, Integer maxAutonomy, Integer countdown) {
        Map<String, Boolean> isVisitedMap = new HashMap<>();
        ComputedPath computedPath = new ComputedPath();
        // Init du path avec notre première étape
        Step initialStep = Step.builder()
                .planetName(source.getPlanetName())
                .build();
        computedPath.addStep(initialStep);
        List<ComputedPath> resultPaths = new ArrayList<>();
        // Appelle récursif à notre méthode de parcours en profondeur
        recursiveFindPath(source, destination, isVisitedMap, computedPath, 0, maxAutonomy, maxAutonomy, countdown, resultPaths);
        // On regarde si on peut éviter les chasseurs de prime dans nos chemins trouvés
        for (ComputedPath path : resultPaths) {
            //tryToOptimizePath(path, countdown);
        }
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
        // Si on est au bout du chemin, on construit notre Path, on l'ajoute à la liste des résultats et on return
        if (currentNode.equals(destination)) {
            computedPath.setTotalDistance(computedPath.getSteps().stream().mapToInt(Step::getDistanceFromPreviousJump).sum());
            computedPath.setTotalDays(computedPath.getTotalDistance() + computedPath.getRefuelNumber()
                    + computedPath.getSteps().stream().mapToInt(Step::getDaysToWait).sum());
            resultPaths.add(new ComputedPath(computedPath));
            return;
        }
        // Ajout du noeud courant au noeuds visités
        isVisitedMap.put(currentNode.getPlanetName(), true);
        // Recursivité sur tout les noeuds adjacents
        for (Map.Entry<String, Integer> adjNode : currentNode.getAdjacentNodes().entrySet()) {
            // Arrivée sur une nouvelle planète
            String nextName = adjNode.getKey();
            Integer nextDistance = adjNode.getValue();
            Node nextNode = nodesMap.get(nextName);
            // Y a t il des chasseurs de prime ?
            int bountyHunterCount = 0; // On utilise un compteur pour gérer le cas où on doit refuel avec des BH présents
            boolean isBountyHunter = currentNode.getDaysWithBountyHunters().contains(currentDay);
            if (isBountyHunter) {
                bountyHunterCount++;
            }
            boolean isRefuel = false;
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
                int oldAutonomy = currentAutonomy;
                if (isRefuel) {
                    computedPath.refuel();
                    computedPath.getSteps().get(computedPath.getSteps().size() - 1).setRefuelOnPlanet(true);
                    currentAutonomy = maxAutonomy;
                    currentDay++;
                }
                computedPath.bountyHunterSpotted(bountyHunterCount);
                // On calcul le fuel qu'on brûle
                currentAutonomy = currentAutonomy - nextDistance;
                // Et le temps qui passe
                currentDay = currentDay + nextDistance;
                // On contruit l'étape suivante
                Step nextStep = Step.builder()
                        .planetName(nextName)
                        .distanceFromPreviousJump(nextDistance)
                        .dayOfArrival(currentDay + nextDistance)
                        .build();
                // On mets à jour le chemin qui suit cet étape
                computedPath.getSteps().add(nextStep);
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

    /**
     * On fait l'hypothèse qu'on à jamais de chasseur de prime au départ de Tatooine. Cela simplifie l'agorithme d'optimisation
     *
     * @param path
     * @param countdown
     */
    private void tryToOptimizePath(ComputedPath path, Integer countdown) {
        // Y a t il besoin d'essayer d'éviter les chasseurs de prime ?
        if (path.getBountyHunterEncounter() == 0) return;
        // Combien de jour pouvons nous attendre au maximum
        int maxWait = countdown - path.getTotalDays();
        // Si pas de marge, on ne peut pas faire mieux
        if (maxWait == 0) return;
        // On parcours nos step en essayant d'éviter les chasseurs de prime. Pas la peine de passer par Tatooine, on considère qu'il n'y à jamais de chasseurs de prime au départ
        int alreadyWaited = 0;
        for (int i = 1; i < path.getSteps().size(); i++) {
            Step currentStep = path.getSteps().get(i);
            Node currentNode = nodesMap.get(currentStep.getPlanetName());
            int dayOfArrival = currentStep.getDayOfArrival();
            if (currentNode.getDaysWithBountyHunters().contains(dayOfArrival)) {
                for (int j = 1; j - alreadyWaited <= maxWait; j++) {
                    if (!currentNode.getDaysWithBountyHunters().contains(dayOfArrival + j)) {
                        alreadyWaited = j; // On note qu'on à attendu
                        maxWait = maxWait - j;
                        path.getSteps().get(i - 1).setDaysToWait(j); // On met à jour le step correspondant
                        // Et le Path
                        if (currentStep.isRefuelOnPlanet())
                            path.revertBountyHunterSpotted(2);
                        else
                            path.revertBountyHunterSpotted(1);
                    }
                }
            }
        }
    }

}
