package fr.arby.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.arby.services.GraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@ConditionalOnWebApplication
public class WebappController {

    @Autowired
    private GraphService graphService;

    private ObjectMapper mapper = new ObjectMapper();

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    /**
     * @return la vue de base
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    private String welcomePage() {
        return "welcome";
    }

    /**
     * Méthode qui receptionne le submit du formulaire
     * @param file le fichier passé en formulaire
     * @param attributes les attributs qu'on va insérer dans la vue
     * @return
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    private String computePage(@RequestParam("file") MultipartFile file, RedirectAttributes attributes) {
        // Controle de présence du fichier
        if (file.isEmpty()) {
            attributes.addFlashAttribute("message", "Aucun fichier sélectionné");
            return "redirect:/";
        }
        double result;
        // Calcul de la probabilité de succès
        try {
            result = graphService.computeSuccessProbability(file.getInputStream());
        } catch (Exception e) {
            LOGGER.error("Fichier reçu non valide", e);
            attributes.addFlashAttribute("message", "Fichier invalide, veuillez réessayer");
            return "redirect:/";
        }
        // Affichage de la probabilité
        attributes.addFlashAttribute("message", "Probabilité de succès : " + result * 100 + "% !");

        return "redirect:/";
    }

}