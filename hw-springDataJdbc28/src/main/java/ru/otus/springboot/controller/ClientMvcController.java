package ru.otus.springboot.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.otus.springboot.service.ClientService;

@Controller
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientMvcController {

    private final ClientService clientService;

    @GetMapping
    public String listClients(Model model) {

        model.addAttribute("clients", clientService.findAllClients());
        return "list";
    }

    @GetMapping("/{id}")
    public String getClient(@PathVariable Long id, Model model) {
        model.addAttribute("client", clientService.findClient(id).orElse(null));
        return "view";
    }

    @GetMapping("/new")
    public String newClientForm() {
        return "form";
    }

    @PostMapping
    public String createClient(
            @RequestParam String name,
            @RequestParam(required = false) String street,
            @RequestParam(required = false) List<String> phones) {

        if (phones != null && !phones.isEmpty()) {
            clientService.saveClientWithAddressAndPhones(name, street, phones);
        } else if (street != null && !street.isEmpty()) {
            clientService.saveClientWithStreet(name, street);
        }

        return "redirect:/clients";
    }

    @GetMapping("/delete/{id}")
    public String deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return "redirect:/clients";
    }
}
