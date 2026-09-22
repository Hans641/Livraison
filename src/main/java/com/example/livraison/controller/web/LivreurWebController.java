package com.example.livraison.controller.web;

import com.example.livraison.dto.request.LivreurRequest;
import com.example.livraison.entity.enums.StatutLivreur;
import com.example.livraison.exception.BusinessException;
import com.example.livraison.service.LivreurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/livreurs")
@RequiredArgsConstructor
public class LivreurWebController {

    private final LivreurService livreurService;

    @GetMapping
    public String liste(@RequestParam(required = false) StatutLivreur statut,
                         @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("livreurs", livreurService.lister(statut, PageRequest.of(page, 10)));
        model.addAttribute("statuts", StatutLivreur.values());
        model.addAttribute("statutFiltre", statut);
        return "livreurs/list";
    }

    @GetMapping("/nouveau")
    public String formulaireCreation(Model model) {
        model.addAttribute("livreur", new LivreurRequest());
        model.addAttribute("statuts", StatutLivreur.values());
        return "livreurs/form";
    }

    @PostMapping
    public String creer(@Valid @ModelAttribute("livreur") LivreurRequest req, BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("statuts", StatutLivreur.values());
            return "livreurs/form";
        }
        livreurService.creer(req);
        redirectAttributes.addFlashAttribute("succes", "Livreur cree avec succes");
        return "redirect:/livreurs";
    }

    @GetMapping("/{id}/modifier")
    public String formulaireModification(@PathVariable Long id, Model model) {
        var livreur = livreurService.obtenir(id);
        LivreurRequest req = new LivreurRequest(livreur.getNom(), livreur.getTelephone(), livreur.getVehicule(),
                livreur.getZoneActivite(), livreur.getStatut(), livreur.getCapaciteMaximale());
        model.addAttribute("id", id);
        model.addAttribute("livreur", req);
        model.addAttribute("statuts", StatutLivreur.values());
        return "livreurs/form";
    }

    @PostMapping("/{id}")
    public String modifier(@PathVariable Long id, @Valid @ModelAttribute("livreur") LivreurRequest req,
                            BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("id", id);
            model.addAttribute("statuts", StatutLivreur.values());
            return "livreurs/form";
        }
        try {
            livreurService.modifier(id, req);
            redirectAttributes.addFlashAttribute("succes", "Livreur modifie avec succes");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/livreurs";
    }

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            livreurService.supprimer(id);
            redirectAttributes.addFlashAttribute("succes", "Livreur supprime");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/livreurs";
    }
}
