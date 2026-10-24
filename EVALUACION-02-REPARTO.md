# Evaluación 02 – Reparto del trabajo (Módulo 5 · Historia Clínica)

Repositorio: https://github.com/LIngaMarin/sistema-hospitalario-modulo5 (rama `main`)
Integrantes: **Renzo León** y **Lucas Inga**

---

## 1. Estado actual del proyecto (lo que dejó la Evaluación 01)

| Parte | Qué hay | Qué falta para la Eval 02 |
|---|---|---|
| Stack | Spring Boot 4.1.1, Java 17, JPA, Thymeleaf, MariaDB (XAMPP) | Spring Security y AOP (`spring-boot-starter-aspectj`) no están en el `pom.xml` |
| BD | `ddl-auto=create-drop`: **borra todo al apagar** | Cambiar a `update`; si no, no habrá evidencia en la base de datos ni en la tabla de auditoría |
| **Lucas:** Paciente, HistoriaClinica, CondicionMedica | `HistoriaClinica` **@OneToOne** `Paciente`; `HistoriaClinica` **@ManyToMany** `CondicionMedica` (tabla `historia_condicion`) | Paciente sin editar ni eliminar; CondicionMedica solo por API (sin vista); HistoriaClinica sin editar desde la web |
| **Renzo:** Antecedente, AntecedenteFamiliar, Alergia | Las tres con **@ManyToOne** a `HistoriaClinica` (FK `historia_clinica_id`) | Falta el lado **@OneToMany** en `HistoriaClinica`; la web solo lista, crea y elimina (no edita) |
| Auditoría | No existe | Todo |
| Usuarios y roles | No existe | Todo |
| Seguridad / login | No existe | Todo |

---

## 2. Reparto

La regla del profe es "mínimo dos entidades relacionadas por alumno". Cada uno se queda con sus entidades de la Eval 01 y además con dos preguntas completas.

### Lucas Inga → Pregunta 1 (sus entidades) + Pregunta 3 + Pregunta 4

**P1 – Relaciones (Paciente, HistoriaClinica, CondicionMedica)**
- Mantener el `@OneToOne` Paciente ↔ HistoriaClinica y el `@ManyToMany` HistoriaClinica ↔ CondicionMedica.
- CRUD completo desde la web:
  - Paciente: editar y eliminar, controlando que no se pueda eliminar si tiene una historia clínica.
  - HistoriaClinica: editar el estado (ACTIVA / INACTIVA).
  - CondicionMedica: listar y registrar, y asignar condiciones a una historia.
- Evidencia: capturas de las entidades, de las tablas `paciente`, `historia_clinica` e `historia_condicion`, y del CRUD funcionando.

**P3 – Gestión de usuarios y roles (backend)**
- Entidades nuevas:
  - `Rol`: id, nombre, descripción, activo.
  - `Usuario`: id, username, password con BCrypt, nombre completo, email, activo, y `@ManyToOne Rol` con la FK `rol_id`.
- Repository, Service y Controller de los dos, con registrar, listar, editar, activar/desactivar y asignar rol.
- Datos iniciales: los roles **ADMINISTRADOR**, **MEDICO** y **RECEPCIONISTA**, y un usuario admin, cargados con un `DataLoader` como en el Lab 07.
- **Hacerlo primero**, porque Renzo necesita `Usuario` y `Rol` para el login.

**P4 – Frontend de usuarios y roles (Thymeleaf)**
- `usuarios.html` (listado), `usuario-form.html` (registro y edición, con un select de rol y un check de activo).
- `roles.html` (listado) y `rol-form.html` (registro y edición).
- Botones de activar/desactivar en las dos listas.
- Evidencia: capturas de cada pantalla y de las operaciones.

### Renzo León → Pregunta 1 (sus entidades) + Pregunta 2 + Pregunta 5

**P1 – Relaciones (Antecedente, AntecedenteFamiliar, Alergia)**
- En `HistoriaClinica`, agregar `@OneToMany(mappedBy = "historiaClinica")` para las tres listas, con `@JsonIgnore` para evitar la recursión.
- Integridad: al eliminar una historia, que no queden antecedentes ni alergias huérfanos, usando cascade y orphanRemoval o validándolo.
- Agregar la **edición** web, con el formulario reutilizado y `/editar/{id}`, para las tres entidades.
- Mostrar los antecedentes y las alergias dentro de `historia-detalle.html`.
- Evidencia: capturas de las entidades, de las tablas con su FK y del CRUD.

**P2 – Auditoría con AOP**
- Entidad `Auditoria`: id, usuario, fechaHora, operación (REGISTRO / MODIFICACIÓN / ELIMINACIÓN), entidad y idRegistro.
- Un `@Aspect` con `@AfterReturning` sobre los métodos `guardar`, `actualizar` y `eliminar` de los services. El usuario se toma del `SecurityContextHolder` y el aspecto guarda un registro en la tabla `auditoria`.
- Vista `auditoria.html` con la bitácora, solo para el ADMINISTRADOR.
- Evidencia: tabla `auditoria` llena automáticamente y la bitácora en la web.

**P5 – Control de acceso por rol (Spring Security)**
- `UserDetailsService` que carga el `Usuario` y su `Rol` de la base de datos (de Lucas), y rechaza a los usuarios o roles inactivos.
- Login con formulario (`login.html`) y redirección al panel después de iniciar sesión.
- `SecurityFilterChain` con estas reglas:
  - ADMINISTRADOR: `/usuarios/**`, `/roles/**`, `/auditoria/**` y todo lo demás.
  - MEDICO: pacientes, historias, antecedentes y alergias.
  - RECEPCIONISTA: pacientes y citas. En este módulo solo hay pacientes, así que la regla de citas queda lista para cuando exista ese módulo.
- En `panel.html`, mostrar solo las opciones de cada rol con `sec:authorize` (dependencia `thymeleaf-extras-springsecurity6`).
- Evidencia: login de cada rol, el menú distinto en cada uno y un 403 al entrar a una ruta no permitida.

---

## 3. Orden de trabajo y archivos compartidos

1. **Día 1, los dos:** Renzo agrega al `pom.xml` las dependencias de security, aspectj y thymeleaf-extras, y cambia `ddl-auto=update`. Hace commit y push **antes** de que Lucas empiece, para no chocar en el `pom.xml`.
2. **Lucas:** `Rol` + `Usuario` + repositories + DataLoader, con push temprano.
3. **Renzo:** la P1 de sus entidades y la auditoría, que no dependen de Lucas, mientras tanto.
4. **Renzo:** P5 (login), apenas estén `Usuario` y `Rol` en `main`.
5. **Lucas:** P4 (vistas) y la P1 de sus entidades.
6. **Al final, los dos:** probar con los 3 roles, sacar las capturas y actualizar el README.

**Archivos que tocan los dos (coordinar antes de editarlos):** `pom.xml`, `HistoriaClinica.java` (Renzo agrega las listas `@OneToMany`, Lucas no la toca salvo el estado) y `panel.html` (Renzo pone el `sec:authorize` y Lucas solo agrega los links de usuarios y roles).

Se recomienda que cada uno trabaje en su propia rama (`eval02-renzo` y `eval02-lucas`) y que hagan un PR a `main`.
