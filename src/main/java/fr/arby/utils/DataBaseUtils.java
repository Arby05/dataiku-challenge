package fr.arby.utils;

import fr.arby.beans.Route;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * On devrait créer une classe repository pour requêter dans la BDD,
 * mais comme on ne va faire qu'une seule lecture, on se permet de le faire dans le service ici.
 * Idem, on devrait initier la connexion et le mettre en attribut de classe.
 */

public class DataBaseUtils {

    public static List<Route> getAllRoute(String path) {
        Connection connection = null;
        List<Route> routesListe = new ArrayList<>();
        try {
            // db parameters
            String url = "jdbc:sqlite:" + path;
            // create a connection to the database
            connection = DriverManager.getConnection(url);

            System.out.println("Connection to SQLite has been established.");

            String sql = "SELECT * FROM ROUTES";
            Statement req  = connection.createStatement();
            ResultSet rs    = req.executeQuery(sql);
            while (rs.next()) {
                Route route = Route.builder()
                        .origin(rs.getString("ORIGIN"))
                        .destination(rs.getString("DESTINATION"))
                        .travelTime(rs.getInt("TRAVEL_TIME"))
                        .build();
                routesListe.add(route);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
        return routesListe;
    }

}
