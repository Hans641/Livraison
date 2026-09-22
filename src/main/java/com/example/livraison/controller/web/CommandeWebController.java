package com.example.livraison.controller.web;

import com.example.livraison.dto.request.CreateCommandeRequest;
import com.example.livraison.dto.request.IncidentRequest;
import com.example.livraison.dto.request.PreuveLivraisonRequest;
import com.example.livraison.entity.enums.PrioriteCommande;
import com.example.livraison.entity.enums.StatutCommande;
import com.example.livraison.entity.enums.TypeIncident;
import com.example.livraison.exception.BusinessException;
import com.example.livraison.exception.InvalidOperationException;
import com.example.livraison.service.ClientService;
import com.example.livraison.service.CommandeService;
import com.example.livraison.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Interface Thymeleaf pour la gestion des commandes.
 * Reutilise exactement CommandeService, la meme couche metier que l'API REST.
 */
@Controller
@RequestMapping("/commandes")
@RequiredArgsConstructor
public class CommandeWebController {

    private final CommandeService commandeService;
    private final ClientService clientService;
    private final IncidentService incidentService;

    @GetMapping
    public String liste(@RequestParam(required = false) StatutCommande statut,
                         @RequestParam(required = false) String zone,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        model.addAttribute("commandes", commandeService.lister(statut, zone, PageRequest.of(page, 10)));
        model.addAttribute("statuts", StatutCommande.values());
        model.addAttribute("statutFiltre", statut);
        model.addAttribute("zoneFiltre", zone);
        return "commandes/list";
    }

    @GetMapping("/nouvelle")
    public String formulaireCreation(Model model) {
        model.addAttribute("commande", new CreateCommandeRequest());
        model.addAttribute("clients", clientService.lister(null, PageRequest.of(0, 100)).getContent());
        model.addAttribute("priorites", PrioriteCommande.values());
        return "commandes/form";
    }

    @PostMapping
    public String creer(@Valid @ModelAttribute("commande") CreateCommandeRequest req, BindingResult result,
                         Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("clients", clientService.lister(null, PageRequest.of(0, 100)).getContent());
            model.addAttribute("priorites", PrioriteCommande.values());
            return "commandes/form";
        }
        try {
            var res = commandeService.creer(req);
            redirectAttributes.addFlashAttribute("succes", "Commande " + res.getNumero() + " creee avec succes");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/commandes";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("commande", commandeService.obtenir(id));
        model.addAttribute("typesIncident", TypeIncident.values());
        return "commandes/detail";
    }

    @PostMapping("/{id}/assigner")
    public String assigner(@PathVariable Long id, @RequestParam(required = false) Long livreurId,
                            RedirectAttributes redirectAttributes) {
        try {
            commandeService.assigner(id, livreurId);
            redirectAttributes.addFlashAttribute("succes", "Commande assignee avec succes");
        } catch (BusinessException | InvalidOperationException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/commandes/" + id;
    }

    @PostMapping("/{id}/pickup")
    public String priseEnCharge(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            commandeService.priseEnCharge(id);
        } catch (BusinessException | InvalidOperationException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/commandes/" + id;
    }

    @PostMapping("/{id}/transit")
    public String transit(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            commandeService.demarrerTransit(id);
        } catch (BusinessException | InvalidOperationException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/commandes/" + id;
    }

    @PostMapping("/{id}/livrer")
    public String livrer(@PathVariable Long id, @ModelAttribute PreuveLivraisonRequest preuve,
                          RedirectAttributes redirectAttributes) {
        try {
            var res = commandeService.livrer(id, preuve);
            redirectAttributes.addFlashAttribute("succes", "Livraison confirmee, code : " + res.getCodeConfirmation());
        } catch (BusinessException | InvalidOperationException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/commandes/" + id;
    }

    @PostMapping("/{id}/annuler")
    public String annuler(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            commandeService.annuler(id);
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/commandes/" + id;
    }

    @PostMapping("/{id}/incident")
    public String signalerIncident(@PathVariable Long id, @ModelAttribute IncidentRequest incident,
                                    RedirectAttributes redirectAttributes) {
        incidentService.signaler(id, incident);
        redirectAttributes.addFlashAttribute("succes", "Incident signale");
        return "redirect:/commandes/" + id;
    }
}
