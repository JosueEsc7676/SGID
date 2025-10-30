package com.gidoc.gdoc.REGDOC.repo.interfaces;

import com.gidoc.gdoc.REGDOC.domain.entities.RegistroMes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RegistroMesRepository extends JpaRepository<RegistroMes, Long> {
    List<RegistroMes> findByMes(String mes);
    List<RegistroMes> findByNip(String nip);
    List<RegistroMes> findByMesIgnoreCase(String mes);
    List<RegistroMes> findByMesAndNipIgnoreCase(String mes, String nip);

    // Reemplazar los métodos problemáticos con @Query
    @Query("SELECT r FROM RegistroMes r WHERE LOWER(r.mes) = LOWER(:mes) AND LOWER(r.cInfra) = LOWER(:cInfra)")
    List<RegistroMes> findByMesAndCInfraIgnoreCase(@Param("mes") String mes, @Param("cInfra") String cInfra);

    @Query("SELECT r FROM RegistroMes r WHERE LOWER(r.cInfra) = LOWER(:cInfra)")
    List<RegistroMes> findByCInfraIgnoreCase(@Param("cInfra") String cInfra);

    @Query("SELECT DISTINCT r.mes FROM RegistroMes r ORDER BY " +
            "CASE r.mes " +
            "WHEN 'enero' THEN 1 " +
            "WHEN 'febrero' THEN 2 " +
            "WHEN 'marzo' THEN 3 " +
            "WHEN 'abril' THEN 4 " +
            "WHEN 'mayo' THEN 5 " +
            "WHEN 'junio' THEN 6 " +
            "WHEN 'julio' THEN 7 " +
            "WHEN 'agosto' THEN 8 " +
            "WHEN 'septiembre' THEN 9 " +
            "WHEN 'octubre' THEN 10 " +
            "WHEN 'noviembre' THEN 11 " +
            "WHEN 'diciembre' THEN 12 " +
            "ELSE 13 END")
    List<String> findAllMeses();

    // ✅ OPCIONAL: Método para obtener todos los registros de todos los meses
    @Query("SELECT r FROM RegistroMes r ORDER BY " +
            "CASE r.mes " +
            "WHEN 'enero' THEN 1 " +
            "WHEN 'febrero' THEN 2 " +
            "WHEN 'marzo' THEN 3 " +
            "WHEN 'abril' THEN 4 " +
            "WHEN 'mayo' THEN 5 " +
            "WHEN 'junio' THEN 6 " +
            "WHEN 'julio' THEN 7 " +
            "WHEN 'agosto' THEN 8 " +
            "WHEN 'septiembre' THEN 9 " +
            "WHEN 'octubre' THEN 10 " +
            "WHEN 'noviembre' THEN 11 " +
            "WHEN 'diciembre' THEN 12 " +
            "ELSE 13 END, r.createdAt DESC")
    List<RegistroMes> findAllOrderByMes();
}