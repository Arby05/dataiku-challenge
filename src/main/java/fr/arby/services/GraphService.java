package fr.arby.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.arby.beans.*;
import fr.arby.utils.DataBaseUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GraphService {

    private ObjectMapper mapper = new ObjectMapper();
    private Falcon falcon;
    private Empire empire;

    private Map<String, Node> nodesMap = new HashMap<>();

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    /**
     * Méthode d'entrée principale en mode console
     * @param falconPath Le chemin vers le fichier millenium-falcon.json
     * @param empirePath Le chemin vers le fichier empire.json
     * @return La probabilité de succès sous forme de Double i.e. 0.81 pour 81%, etc ...
     * @throws IOException
     * @throws SQLException
     */
    public Double computeSuccessProbability(String falconPath, String empirePath) throws IOException, SQLException {
        falcon = mapper.readValue(new File(falconPath), Falcon.class);
        empire = mapper.readValue(new File(empirePath), Empire.class);
        String absoluteDbPath = falcon.getRoutes_db();
        Path dbFilePath = Paths.get(absoluteDbPath);
        // On peut avoir notre chemin de BDD en absolu ou relatif, on gère le cas
        // Chemin absolu ? Rien à faire, sinon on doit le reconstruire à partir du fichier falcon
        if (!dbFilePath.isAbsolute())
            absoluteDbPath = Paths.get(falconPath).getParent() + File.separator + falcon.getRoutes_db();
        return computeSuccessProbability(absoluteDbPath);
    }

    /**
     * Méthode d'entrée principale en mode webapp
     * @param empireIS l'InputStream du fichier empire.json déposé via l'IHM
     * @return La probabilité de succès sous forme de Double i.e. 0.81 pour 81%, etc ...
     * @throws IOException
     * @throws SQLException
     */
    public Double computeSuccessProbability(InputStream empireIS) throws IOException, SQLException {
        // Récupération du fichier falcon en ressources si besoin
        if (falcon == null) {
            InputStream falconIS = getClass().getClassLoader().getResourceAsStream("millennium-falcon.json");
            falcon = mapper.readValue(falconIS, Falcon.class);
        }
        empire = mapper.readValue(empireIS, Empire.class);
        // Récupération du fichier database en resources
        InputStream dbIS = getClass().getClassLoader().getResourceAsStream("universe.db");
        // Copy à l'extérieur du jar. C'est moche, mais c'est une limitation du java en package jar/war
        String tmpdir = System.getProperty("java.io.tmpdir");
        File tempFile = new File(tmpdir + File.separator + "universe_temp.db");
        FileUtils.copyInputStreamToFile(dbIS, tempFile);
        return computeSuccessProbability(tempFile.getPath());
    }

    /**
     * Méthode de calcul de la probabilité à partir
     * @param routePath le chemin absolu vers le fichier database qui contient les routes
     * @return La probabilité de succès sous forme de Double i.e. 0.81 pour 81%, etc ...
     * @throws SQLException
     */
    public Double computeSuccessProbability(String routePath) throws SQLException {
        // Récupération des routes
        List<Route> routesList = DataBaseUtils.getAllRoute(routePath);
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
        ComputedPath resultPath = listAllPath(nodesMap.get(falcon.getDeparture()), nodesMap.get(falcon.getArrival()));
        if (resultPath == null) {
            LOGGER.warn("Chemin trop long, la galaxie est condamnée ...");
            return 0d;
        } else {
            LOGGER.info(resultPath.toString());
            resultPath.computeProbability();
            return resultPath.computeProbability();
        }
    }

    /**
     * Méthode d'init du parcours en profondeur du graph pour obtenir tous les chemins possible entre la source et la destionation
     * @param source le noeud de départ dans le Graph
     * @param destination le noeud cible dans le Graph
     * @return le chemin le moins risqué et le plus court entre la source et la destionation faisable dans le temps impartie, null si aucun chemins n'est possible
     */
    private ComputedPath listAllPath(Node source, Node destination) {
        Map<String, Boolean> isVisitedMap = new HashMap<>();
        ComputedPath computedPath = new ComputedPath();
        // Init du path avec notre première étape
        Step initialStep = Step.builder()
                .planetName(source.getPlanetName())
                .build();
        computedPath.addStep(initialStep);
        // tous les chemins possible entre la source et la destionation qui sont faisable dans le temps impartie
        List<ComputedPath> resultPaths = new ArrayList<>();
        // Appelle récursif à notre méthode de parcours en profondeur
        recursiveFindPath(source, destination, isVisitedMap, computedPath, initialStep, 0, falcon.getAutonomy(), resultPaths);
        // On regarde si on peut éviter les chasseurs de prime dans nos chemins trouvés si besoin
        for (ComputedPath path : resultPaths) {
            tryToOptimizePath(path);
        }
        // On va choisir notre chemin Le moins risqué et le plus court
        if (resultPaths.size() > 0) {
            return resultPaths.stream().sorted(
                    Comparator.comparingInt(ComputedPath::getTotalBountyHunterEncounter)
                            .thenComparing(ComputedPath::getTotalDistance)).collect(Collectors.toList()).get(0);
        }
        return null;
    }

    /**
     * Méthode récursive pour le parcours en profondeur du graph. Elle alimente la liste resultPaths avec tous les chemin trouvé possible dans le temps imparti
     * @param currentNode le noeud courant
     * @param destination la destination finale
     * @param isVisitedMap la map qui contient la liste des noeud visité pour éviter les cycle
     * @param computedPath le chemin en cours de calcul
     * @param currentStep l'étape en cours
     * @param currentDay le jour courant
     * @param currentAutonomy l'autonomie courante
     * @param resultPaths la liste des chemins possibles qu'on alimente au fur et à mesure du parcours
     */
    private void recursiveFindPath(Node currentNode, Node destination, Map<String, Boolean> isVisitedMap, ComputedPath computedPath, Step currentStep,
                                   Integer currentDay, Integer currentAutonomy, List<ComputedPath> resultPaths) {
        // Si on à dépassé le countdown, c'est qu'on a déjà perdu
        if (currentDay > empire.getCountdown()) {
            return;
        }
        // Si on est au bout du chemin, on construit notre Path, on l'ajoute à la liste des résultats et on return
        if (currentNode.equals(destination)) {
            resultPaths.add(new ComputedPath(computedPath));
            return;
        }
        // Ajout du noeud courant au noeuds visités
        isVisitedMap.put(currentNode.getPlanetName(), true);
        // Recursivité sur tout les noeuds adjacents
        for (Map.Entry<String, Integer> adjNode : currentNode.getAdjacentNodes().entrySet()) {
            // Préparation du départ sur une nouvelle planète
            String nextName = adjNode.getKey();
            Integer nextDistance = adjNode.getValue();
            Node nextNode = nodesMap.get(nextName);
            // Si la nouvelle destination est trop loin on passe au noeud suivant, elle est inaccessible avec l'autonomie max
            if (nextDistance > falcon.getAutonomy()) {
                continue;
            }

            // Y a t il des chasseurs de prime là où on est ?
            int bountyHunterCount = 0; // On utilise un compteur pour gérer le cas où on doit refuel avec des BH présents
            boolean isBountyHunter = currentNode.getDaysWithBountyHunters().contains(currentDay);
            if (isBountyHunter) {
                bountyHunterCount++;
            }
            boolean isRefuel = false;
            // Doit on faire le plein avant de repartir ?
            if (currentAutonomy < nextDistance) {
                isRefuel = true;
                if (isBountyHunter) {
                    bountyHunterCount++;
                }
            }
            currentStep.setRiskedEncounter(bountyHunterCount);
            // Copie des paramètres pour éviter de tout rollback
            int oldCurrentAutonomy = currentAutonomy;
            int oldCurrentDay = currentDay;
            // On vérifie aussi si on l'a déjà visité ou non pour éviter les cycles
            if (isVisitedMap.get(nextName) == null || !isVisitedMap.get(nextName)) {
                if (isRefuel) {
                    currentAutonomy = falcon.getAutonomy();
                    currentDay++;
                    currentStep.setRefuel(true);
                }
                // On calcul le fuel qu'on brûle pour le saut
                currentAutonomy = currentAutonomy - nextDistance;
                // Et le temps qui passe
                currentDay = currentDay + nextDistance;
                // On contruit l'étape suivante
                Step nextStep = Step.builder()
                        .planetName(nextName)
                        .distanceFromPreviousJump(nextDistance)
                        .dayOfArrival(currentDay)
                        .build();
                // On mets à jour le chemin qui suit cet étape
                computedPath.getSteps().add(nextStep);
                // On saute ! Avec les nouvelles valeurs d'arrivée passé en paramètre, fuel brûlé, temps passé, ...
                recursiveFindPath(nextNode, destination, isVisitedMap, computedPath, nextStep, currentDay, currentAutonomy, resultPaths);
                // Rollback des variables qui ont été modifiées
                computedPath.getSteps().remove(nextStep);
                if (isRefuel) {
                    currentStep.setRefuel(false);
                }
                currentAutonomy = oldCurrentAutonomy;
                currentDay = oldCurrentDay;
            }
        }
        isVisitedMap.put(currentNode.getPlanetName(), false);
    }

    /**
     * On fait l'hypothèse qu'on à jamais de chasseur de prime au départ de Tatooine. Cela simplifie l'agorithme d'optimisation
     * @param path le chemin qu'on cherche à optimiser
     */
    private void tryToOptimizePath(ComputedPath path) {
        // Y a t il besoin d'essayer d'éviter les chasseurs de prime ?
        if (path.getTotalBountyHunterEncounter() == 0) return;
        // Combien de jour pouvons nous attendre au maximum
        int maxWait = empire.getCountdown() - path.getTotalDays();
        // Si pas de marge, on ne peut pas faire mieux
        if (maxWait == 0) return;
        // On parcours nos step en essayant d'éviter les chasseurs de prime. Pas la peine de passer par Tatooine,
        // on considère qu'il n'y a jamais de chasseurs de prime au départ
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
                        // On met à jour le step correspondant
                        path.getSteps().get(i - 1).setDaysToWait(j);
                        // Plus de chasseur de prime !
                        path.getSteps().get(i).removeEncounter();
                        // Et les steps suivants
                        for (Step nextStep : path.getSteps().subList(i, path.getSteps().size())) {
                            nextStep.setDayOfArrival(nextStep.getDayOfArrival() + j);
                        }
                    }
                }
            }
        }
    }

}
