package com.example.livraison.service.impl;

import com.example.livraison.dto.request.ClientRequest;
import com.example.livraison.dto.response.ClientResponse;
import com.example.livraison.entity.Client;
import com.example.livraison.exception.ResourceNotFoundException;
import com.example.livraison.mapper.ClientMapper;
import com.example.livraison.repository.ClientRepository;
import com.example.livraison.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Override
    @Transactional
    public ClientResponse creer(ClientRequest req) {
        Client client = clientMapper.toEntity(req);
        return clientMapper.toResponse(clientRepository.save(client));
    }

    @Override
    @Transactional
    public ClientResponse modifier(Long id, ClientRequest req) {
        Client client = trouver(id);
        clientMapper.updateEntity(client, req);
        return clientMapper.toResponse(clientRepository.save(client));
    }

    @Override
    public ClientResponse obtenir(Long id) {
        return clientMapper.toResponse(trouver(id));
    }

    @Override
    public Page<ClientResponse> lister(String recherche, Pageable pageable) {
        Specification<Client> spec = (root, query, cb) -> {
            if (recherche == null || recherche.isBlank()) return cb.conjunction();
            String like = "%" + recherche.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("nom")), like),
                    cb.like(cb.lower(root.get("telephone")), like)
            );
        };
        return clientRepository.findAll(spec, pageable).map(clientMapper::toResponse);
    }

    @Override
    @Transactional
    public void supprimer(Long id) {
        Client client = trouver(id);
        clientRepository.delete(client);
    }

    private Client trouver(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable : id=" + id));
    }
}
