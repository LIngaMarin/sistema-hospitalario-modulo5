# Exposición — Evaluación 02 (Módulo 5 · Historia Clínica)
**Integrantes:** Renzo León y Lucas Inga · **Curso:** Desarrollo de Aplicaciones Web · **Rama:** `eval02-renzo` (incluye el trabajo de los dos)

| Pregunta | Tema | Expone |
|---|---|---|
| P1 | Relaciones JPA y CRUD | Lucas (Paciente, HistoriaClinica, CondicionMedica) y Renzo (Antecedente, AntecedenteFamiliar, Alergia) |
| P2 | Auditoría con AOP | Renzo |
| P3 | Usuarios y roles (backend) | Lucas |
| P4 | Frontend de usuarios y roles | Lucas |
| P5 | Control de acceso por rol | Renzo |

**Orden sugerido (unos 10 minutos):** login con los 3 roles (P5, 1 min) → P1 Lucas → P1 Renzo → P3 y P4 Lucas → P2 Renzo (cerrar mostrando la bitácora con lo que se hizo en la demo) → P5 Renzo (403 y usuario desactivado) → preguntas.

---

## 0. Antes de exponer (checklist)

1. XAMPP con **MySQL encendido**. La base `historia_clinica_db` se llena sola (`ddl-auto=update`).
2. Ejecutar la aplicación con `mvnw.cmd spring-boot:run` o desde IntelliJ (`HistoriaClinicaApplication`).
3. Abrir http://localhost:8080. Redirige al login.
4. Usuarios de prueba, creados por `DataLoader`:

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | ADMINISTRADOR |
| `medico` | `medico123` | MEDICO |
| `recepcion` | `recepcion123` | RECEPCIONISTA |

5. Tener abierto el panel de XAMPP (phpMyAdmin) o una consola MySQL para mostrar las tablas.

---

## 1. Introducción (30 s)

"Buenas tardes, profesor. Sobre el proyecto de la Evaluación 01, el Módulo 5 de Historia Clínica, implementamos las cinco preguntas de la Evaluación 02: las relaciones con Hibernate, la auditoría, la gestión de usuarios y roles con su frontend, y el control de acceso según el rol. Lucas va a explicar las relaciones de paciente e historia y el módulo de usuarios y roles. Yo voy a explicar las relaciones de antecedentes y alergias, la auditoría y la seguridad."

---

## 2. Modelo de base de datos completo

```
ROL 1 ──── N USUARIO                      (Usuario @ManyToOne Rol, FK rol_id)

PACIENTE 1 ──── 1 HISTORIA_CLINICA        (@OneToOne, FK paciente_id UNIQUE)
HISTORIA_CLINICA N ──── M CONDICION_MEDICA (@ManyToMany, tabla historia_condicion)
HISTORIA_CLINICA 1 ──── N ANTECEDENTE          (FK historia_clinica_id)
HISTORIA_CLINICA 1 ──── N ANTECEDENTE_FAMILIAR (FK historia_clinica_id)
HISTORIA_CLINICA 1 ──── N ALERGIA              (FK historia_clinica_id)

AUDITORIA (independiente: usuario, fecha_hora, operacion, entidad, id_registro)
```

Las cuatro anotaciones que pide la pregunta están usadas: `@OneToOne`, `@ManyToMany`, `@ManyToOne` y `@OneToMany`.

---

## 3. P1 — Relaciones JPA (parte de Renzo)

**Archivos:** `model/HistoriaClinica.java`, `model/Antecedente.java`, `model/AntecedenteFamiliar.java`, `model/Alergia.java`

**Qué decir:**
- "Cada antecedente, antecedente familiar o alergia pertenece a una sola historia clínica. Eso es un `@ManyToOne` con `@JoinColumn(name = "historia_clinica_id", nullable = false)`, así que la base de datos no deja guardar un registro sin historia."
- "En la Evaluación 02 agregué el lado inverso en `HistoriaClinica`: tres listas `@OneToMany(mappedBy = "historiaClinica")`. `mappedBy` indica que la llave foránea está en la otra tabla y no se crea ninguna columna nueva."
- "Para mantener la integridad usé `cascade = CascadeType.ALL` y `orphanRemoval = true`: si se elimina una historia clínica, se eliminan también sus antecedentes y alergias, y no quedan filas huérfanas."
- "Las listas tienen `@JsonIgnore` para que la API REST no entre en un bucle infinito (historia → antecedente → historia → …)."
- "El service valida que la historia exista antes de guardar (`historiaClinicaRepository.findById(...)`)."

**CRUD con la relación (demo como `medico`):**
1. Antecedentes personales → **Nuevo** → ID de historia, categoría, descripción → Guardar.
2. Botón **Editar**: el mismo formulario se carga con los datos (un campo oculto lleva el `id`) → cambiar la descripción → Guardar.
3. Abrir la historia clínica → **detalle**: se ven sus antecedentes, familiares y alergias.
4. Eliminar.
5. **En la base de datos:** `SELECT * FROM antecedente;` muestra la columna `historia_clinica_id`. En phpMyAdmin, pestaña Estructura → Vista de relaciones, se ve la llave foránea.

**Si pregunta "¿por qué `mappedBy` y no `@JoinColumn` en los dos lados?":** porque la relación la maneja el lado `@ManyToOne`, el dueño. Si se ponen las dos, Hibernate crearía una tabla intermedia o una columna duplicada.

---

## 4. P2 — Auditoría con AOP (Renzo)

**Archivos:** `aspect/AuditoriaAspect.java`, `model/Auditoria.java`, `repository/AuditoriaRepository.java`, `controller/AuditoriaController.java`, `templates/auditoria.html`

**Qué decir:**
- "La auditoría no está escrita dentro de cada service, sino en un **aspecto** (programación orientada a aspectos). Así no se repite código y los services de Lucas, como `UsuarioService` y `RolService`, quedaron auditados sin tocarlos."
- "`@Around` intercepta los métodos de **todos** los services cuyo nombre empieza con `guardar`, `crear`, `registrar`, `actualizar`, `agregar`, `cambiar`, `activar`, `desactivar` o `eliminar`."
- "Antes de ejecutar el método decido la operación:
  - `eliminar` → ELIMINACION.
  - `guardar` sin id → REGISTRO; con id → MODIFICACION, porque el mismo método sirve para crear y editar.
  - `crear` / `registrar` → REGISTRO; el resto → MODIFICACION."
- "Llamo a `joinPoint.proceed()`. **Si el método falla, no se registra nada**, porque solo se audita lo que realmente se hizo."
- "Después guardo en la tabla `auditoria`:
  - el **usuario**, sacado del `SecurityContextHolder`, es decir, el que inició sesión;
  - la **fecha y hora**;
  - la **operación**;
  - la **entidad**, que es el nombre del service sin la palabra *Service*;
  - el **id del registro**, que se lee con `getId()` del objeto devuelto o del parámetro en el caso de eliminar."

**Demo:** al final de la exposición, entrar como `admin` → **Bitácora de auditoría**. Aparece todo lo que se hizo en la demo, con el usuario que lo hizo (admin o medico), en orden del más reciente al más antiguo y con colores (verde = registro, amarillo = modificación, rojo = eliminación). Mostrar también `SELECT * FROM auditoria;`.

**Posibles preguntas:**
- *¿Por qué `@Around` y no `@AfterReturning`?* Porque necesito ver el objeto **antes** de guardarlo, para saber si tenía id (edición) o no (registro). Después de guardar, siempre tiene id.
- *¿Qué es un pointcut?* Es la expresión `execution(* com.hospital.historia_clinica.service.*.guardar*(..))`, que dice a qué métodos se aplica el aspecto.
- *¿Qué dependencia usa?* `spring-boot-starter-aspectj`, que en Spring Boot 4 reemplaza a `spring-boot-starter-aop`.

---

## 5. P5 — Control de acceso por rol (Renzo)

**Archivos:** `config/SecurityConfig.java`, `service/UserDetailsServiceImpl.java`, `controller/PanelController.java`, `templates/login.html`, `templates/panel.html`, `templates/acceso-denegado.html`

**Qué decir:**
- "El login usa los usuarios de la base de datos que hizo Lucas. `UserDetailsServiceImpl` busca el `Usuario` por username y le da la autoridad `ROLE_` + nombre del rol, por ejemplo `ROLE_MEDICO`."
- "Si el usuario **o su rol** están desactivados, el usuario queda `disabled` y Spring Security no lo deja entrar."
- "Las contraseñas se comparan con **BCrypt**; en la base de datos nunca se guarda la contraseña en texto plano."
- "En `SecurityConfig`, el `SecurityFilterChain` valida los permisos **en el backend**:

| Rutas | Roles |
|---|---|
| `/usuarios/**`, `/roles/**`, `/auditoria/**` (y sus `/api`) | ADMINISTRADOR |
| `/historias/**`, `/antecedentes/**`, `/antecedentes-familiares/**`, `/alergias/**`, `/condiciones/**` | ADMINISTRADOR, MEDICO |
| `/citas/**` (módulo de citas, fuera de este proyecto) | ADMINISTRADOR, RECEPCIONISTA |
| `/pacientes/**` | los tres |
| todo lo demás (panel) | cualquier usuario autenticado |"

- "El login es un formulario propio (`formLogin().loginPage("/login")`) y, con `defaultSuccessUrl("/panel", true)`, después de iniciar sesión **siempre redirige al panel**."
- "En el **frontend**, `PanelController` lee el rol del usuario autenticado y el panel muestra **solo las opciones de su rol**. Aunque alguien escriba la URL a mano, el backend responde con la página de **403 – Acceso denegado**."

**Demo (lo que sale en las pruebas):**

| Usuario | Ve en el menú | Puede entrar | 403 en |
|---|---|---|---|
| admin | todo | todo | — |
| medico | Pacientes, Historias, Antecedentes, Alergias, Condiciones | pacientes, historias, alergias | usuarios, roles, auditoría |
| recepcion | Pacientes, Citas | pacientes | historias, alergias, usuarios, roles, auditoría |

1. Sin sesión, ir a `/panel` → redirige a `/login`.
2. Entrar como `recepcion` → el menú solo tiene Pacientes → escribir `/historias` en la barra → **403**.
3. Entrar como `medico` → `/usuarios` → **403**.
4. Entrar como `admin` → desactivar al usuario `medico` (P4 de Lucas) → cerrar sesión → intentar entrar como `medico` → "Usuario o contraseña incorrectos, o el usuario está desactivado". Volver a activarlo.
5. Contraseña equivocada → mensaje de error en el login.

**Posibles preguntas:**
- *¿Diferencia entre autenticación y autorización?* La autenticación comprueba quién eres (login con BCrypt). La autorización decide qué puedes hacer según tu rol (`hasRole` / `hasAnyRole`).
- *¿Por qué `hasRole("MEDICO")` si en la base dice "MEDICO" y no "ROLE_MEDICO"?* Porque `hasRole` agrega el prefijo `ROLE_` solo, y `UserDetailsServiceImpl` crea la autoridad con ese prefijo.
- *¿Basta con ocultar los botones?* No. Ocultar es solo comodidad para el usuario; la seguridad real está en el `SecurityFilterChain`, que bloquea la URL aunque se escriba a mano.
- *¿Por qué está desactivado CSRF?* Porque los formularios del proyecto se envían sin token CSRF. En un sistema real convendría activarlo y usar `th:action` en los formularios.

---

## 6. P1, P3 y P4 — parte de Lucas (resumen para apoyarlo)

- **P1:** `Paciente` `@OneToOne` `HistoriaClinica` (FK `paciente_id` única) y `HistoriaClinica` `@ManyToMany` `CondicionMedica` (tabla `historia_condicion`). CRUD web de pacientes (editar y eliminar), estado de la historia y condiciones médicas.
- **P3:** `Rol` (nombre, descripción, activo) y `Usuario` (username, password BCrypt, nombre, email, activo) con `@ManyToOne Rol` (FK `rol_id`). Registrar, listar, editar, activar/desactivar y asignar rol. No deja desactivar ni quitar el rol al **último ADMINISTRADOR activo**. `DataLoader` crea los 3 roles y los 3 usuarios.
- **P4:** `usuarios.html`, `usuario-form.html` (con select de rol), `roles.html`, `rol-form.html`, con botones de activar/desactivar. Todo desde la web, sin tocar la base de datos.

---

## 7. Cierre (20 s)

"En resumen, el sistema tiene relaciones JPA con integridad referencial, una auditoría automática que registra quién hizo cada operación, la gestión de usuarios y roles con su interfaz, y un control de acceso que valida el rol tanto en el menú como en el backend. Muchas gracias."
