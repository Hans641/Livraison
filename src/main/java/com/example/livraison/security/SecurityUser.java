package com.example.livraison.security;

import com.example.livraison.entity.Utilisateur;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class SecurityUser implements UserDetails {

    private final Utilisateur utilisateur;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().name()));
    }

    @Override
    public String getPassword() {
        return utilisateur.getPassword();
    }

    @Override
    public String getUsername() {
        return utilisateur.getUsername();
    }

    @Override
    public boolean isEnabled() {
        return utilisateur.isActif();
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }
}
