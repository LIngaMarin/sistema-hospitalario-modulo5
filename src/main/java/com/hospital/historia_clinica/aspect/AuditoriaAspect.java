package com.hospital.historia_clinica.aspect;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.hospital.historia_clinica.model.Auditoria;
import com.hospital.historia_clinica.repository.AuditoriaRepository;

@Aspect
@Component
public class AuditoriaAspect {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaAspect(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    // Se intercepta cualquier metodo de los services que cree, modifique o elimine datos
    @Around("execution(* com.hospital.historia_clinica.service.*.guardar*(..))"
            + " || execution(* com.hospital.historia_clinica.service.*.crear*(..))"
            + " || execution(* com.hospital.historia_clinica.service.*.registrar*(..))"
            + " || execution(* com.hospital.historia_clinica.service.*.actualizar*(..))"
            + " || execution(* com.hospital.historia_clinica.service.*.editar*(..))"
            + " || execution(* com.hospital.historia_clinica.service.*.agregar*(..))"
            + " || execution(* com.hospital.historia_clinica.service.*.cambiar*(..))"
            + " || execution(* com.hospital.historia_clinica.service.*.activar*(..))"
            + " || execution(* com.hospital.historia_clinica.service.*.desactivar*(..))"
            + " || execution(* com.hospital.historia_clinica.service.*.eliminar*(..))")
    public Object auditar(ProceedingJoinPoint joinPoint) throws Throwable {

        String metodo = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        Object primerArgumento = args.length > 0 ? args[0] : null;

        // guardar(entidad) con id es una edicion; sin id es un registro nuevo
        Long idAntes = leerId(primerArgumento);
        String operacion;
        if (metodo.startsWith("eliminar")) {
            operacion = "ELIMINACION";
        } else if (metodo.startsWith("guardar")) {
            operacion = idAntes == null ? "REGISTRO" : "MODIFICACION";
        } else if (metodo.startsWith("crear") || metodo.startsWith("registrar")) {
            operacion = "REGISTRO";
        } else {
            operacion = "MODIFICACION";
        }

        // Si el metodo falla no se registra nada en la auditoria
        Object resultado = joinPoint.proceed();

        Long idRegistro = leerId(resultado);
        if (idRegistro == null && primerArgumento instanceof Long) {
            idRegistro = (Long) primerArgumento;
        }

        // AntecedenteService -> Antecedente
        String entidad = joinPoint.getTarget().getClass().getSimpleName().replace("Service", "");

        auditoriaRepository.save(new Auditoria(usuarioActual(), operacion, entidad, idRegistro));
        return resultado;
    }

    private String usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            return "anonimo";
        }
        return auth.getName();
    }

    // Lee el id de una entidad llamando a su getId(), si lo tiene
    private Long leerId(Object objeto) {
        if (objeto == null || objeto instanceof Long) {
            return null;
        }
        try {
            Method getId = objeto.getClass().getMethod("getId");
            Object id = getId.invoke(objeto);
            return id instanceof Long ? (Long) id : null;
        } catch (Exception e) {
            return null;
        }
    }
}
