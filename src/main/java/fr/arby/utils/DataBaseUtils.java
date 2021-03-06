package fr.arby.utils;

import fr.arby.beans.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * On devrait créer une classe repository pour requêter dans la BDD,
 * mais comme on ne va faire qu'une seule lecture, on se permet de le faire de manière statique ici.
 * Idem, on devrait initier la connexion et le mettre en attribut de classe...
 */

public class DataBaseUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataBaseUtils.class);

    private static String SELECT_ALL_ROUTE = "SELECT * FROM ROUTES";

    /**
     * Méthode statique de récupération des routes contenue en BDD
     * @param path le chemin vers la base SQLite
     * @return La liste des routes contenue dans la base de donnée
     * @throws SQLException
     */
    public static List<Route> getAllRoute(String path) throws SQLException {
        String url = "jdbc:sqlite:" + path;
        List<Route> routesListe = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url)){
            Statement req  = connection.createStatement();
            ResultSet rs    = req.executeQuery(SELECT_ALL_ROUTE);
            while (rs.next()) {
                Route route = Route.builder()
                        .origin(rs.getString("ORIGIN"))
                        .destination(rs.getString("DESTINATION"))
                        .travelTime(rs.getInt("TRAVEL_TIME"))
                        .build();
                routesListe.add(route);
            }
        } catch (SQLException e) {
            LOGGER.error("Problème lors de la lecture de la base de donnée " + path, e);
            throw e;
        }
        return routesListe;
    }

}
