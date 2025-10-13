package com.gidoc.gdoc.Usuarios.domain.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

    private final BCryptPasswordEncoder passwordEncoder;

    public SecurityService() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String encriptarPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public boolean verificarPassword(String password, String passwordEncriptado) {
        return passwordEncoder.matches(password, passwordEncriptado);
    }

    public String generarTokenAutenticacion() {
        return java.util.UUID.randomUUID().toString();
    }
}