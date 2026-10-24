# Sistema Hospitalario — Módulo 5: Historia Clínica

Proyecto de la Evaluación N° 01 del curso Desarrollo de Aplicaciones Web (Semana 4).

**Integrantes:**
- Renzo León
- Lucas Inga

La especificación completa del módulo está en [MODULO-5-HISTORIA-CLINICA.md](MODULO-5-HISTORIA-CLINICA.md).

---

## Tecnologías

Spring Boot · Spring Data JPA (Hibernate) · MariaDB · Thymeleaf · Bootstrap · Postman

Arquitectura por capas: Entidad → Repository → Service → Controller.

---

## Reparto del trabajo

| Requerimiento | Descripción | Responsable | Estado |
|---|---|---|---|
| RF-HC-01 | Generar un número único de historia clínica | Lucas Inga | **Terminado** |
| RF-HC-02 | Consultar la historia clínica de un paciente | Lucas Inga | **Terminado** |
| RF-HC-03 | Mostrar los datos básicos del paciente | Lucas Inga | **Terminado** |
| RF-HC-05 | Registrar antecedentes personales | Renzo León | **Terminado** |
| RF-HC-06 | Registrar antecedentes familiares | Renzo León | **Terminado** |
| RF-HC-07 | Registrar alergias | Renzo León | **Terminado** |

---

## Avance hasta el momento

### RF-HC-01, RF-HC-02, RF-HC-03 — Paciente e Historia Clínica

Entidades `Paciente` e `HistoriaClinica` con repository, service y controller REST.

`Paciente` tiene `id`, `nombres`, `apellidos` y `dni` (único).

`HistoriaClinica` tiene `id`, `numeroHistoria` (único), relación `@OneToOne` con `Paciente`, `fechaApertura` y `estado` (ACTIVA / INACTIVA). El número de historia se genera automáticamente al crearla, combinando el id del paciente con la marca de tiempo.

También se agregó la entidad `CondicionMedica`, relacionada con `HistoriaClinica` mediante `@ManyToMany`, para registrar antecedentes familiares como catálogo reutilizable entre historias.

Antecedentes y alergias ya no guardan `historiaClinicaId` como número suelto: ahora tienen una relación `@ManyToOne` real hacia `HistoriaClinica`.

### RF-HC-05 — Antecedentes personales

Entidad `Antecedente` con las cuatro capas completas y vistas Thymeleaf.

Campos: `id`, `historiaClinica` (relación `@ManyToOne`, obligatoria), `categoria`, `descripcion`, `fechaRegistro`, `observaciones`.

Las categorías que maneja el formulario salen del documento del módulo: enfermedad previa, cirugía, hospitalización, enfermedad crónica, traumatismo, transfusión y otro.

El `AntecedenteService` no guarda un registro si la historia clínica indicada no existe: la busca primero con `historiaClinicaRepository.findById(...).orElseThrow(...)`, igual que se hizo en el laboratorio de la Semana 4 con Producto y Categoría.

### RF-HC-06 — Antecedentes familiares

Entidad `AntecedenteFamiliar` con las cuatro capas completas y vistas Thymeleaf.

Campos: `id`, `historiaClinica` (relación `@ManyToOne`, obligatoria), `enfermedad`, `parentesco`, `fechaRegistro`, `observaciones`.

Las enfermedades del formulario son las del documento: diabetes, hipertensión, enfermedad cardiovascular, cáncer, enfermedad hereditaria y otro. El parentesco permite padre, madre, hermano, hermana, abuelo, abuela y otro.

Antes esta entidad guardaba `historiaClinicaId` como un número suelto, sin relación real. Ahora usa `@ManyToOne` hacia `HistoriaClinica`, igual que `Antecedente` y `Alergia`, con la misma validación de que la historia clínica debe existir.

### RF-HC-07 — Alergias

Entidad `Alergia` con las cuatro capas completas y vistas Thymeleaf.

Campos: `id`, `historiaClinica` (relación `@ManyToOne`, obligatoria), `nombre`, `tipo`, `reaccion`, `fechaRegistro`, `observaciones`.

Los tipos que maneja el formulario son medicamento, alimento, ambiental y otro. El ejemplo del documento del módulo es una alergia a la penicilina con erupción cutánea como reacción.

### Vistas de Paciente e Historia Clínica

Se agregaron los controladores `PacienteController` y `HistoriaClinicaController` (vistas Thymeleaf), separados de `PacienteRestController` y `HistoriaClinicaRestController` (API REST), siguiendo el mismo patrón que ya usaban Antecedente, Antecedente familiar y Alergia.

- `/pacientes`: tabla de pacientes registrados y formulario para crear uno nuevo.
- `/historias`: tabla de historias clínicas (número, paciente, fecha de apertura, estado) y formulario para generar una nueva eligiendo el paciente de una lista.
- `/historias/{id}`: ficha de la historia clínica con los datos básicos del paciente y sus antecedentes personales, familiares y alergias — resuelve RF-HC-02 (consultar) y RF-HC-03 (mostrar datos básicos).

Para armar esa ficha se agregaron las consultas derivadas `findByHistoriaClinicaId` en los repositorios de `Antecedente`, `AntecedenteFamiliar` y `Alergia`, igual que `findByCategoriaId` en el laboratorio de la Semana 4.

### Panel de navegación

`PanelController` muestra en `/` y en `/panel` un menú con enlaces a Pacientes, Historias, Antecedentes personales, Antecedentes familiares y Alergias. Cada listado tiene un botón "Inicio" para volver a este panel.

---

## Endpoints de la API REST

### Pacientes

| Verbo | Ruta | Respuesta |
|---|---|---|
| GET | `/api/pacientes` | Lista completa |
| POST | `/api/pacientes` | 201 con el registro creado |
| GET | `/api/pacientes/{id}` | 200 con el registro, o 404 |

### Historias clínicas

| Verbo | Ruta | Respuesta |
|---|---|---|
| GET | `/api/historias` | Lista completa |
| POST | `/api/historias` | 201, recibe `{"pacienteId": ...}` y genera el número único |
| GET | `/api/historias/{id}` | 200 con el registro, o 404 |
| GET | `/api/historias/paciente/{pacienteId}` | 200 con la historia del paciente, o 404 |
| PUT | `/api/historias/{id}` | 200, actualiza el `estado` |
| DELETE | `/api/historias/{id}` | 204 sin contenido |
| POST | `/api/historias/{id}/condiciones/{condicionId}` | 200, asocia una condición médica a la historia |
| GET | `/api/historias/{id}/condiciones` | 200 con las condiciones asociadas |

### Condiciones médicas

| Verbo | Ruta | Respuesta |
|---|---|---|
| GET | `/api/condiciones` | Lista completa |
| POST | `/api/condiciones` | 201 con el registro creado |

### Antecedentes personales

| Verbo | Ruta | Respuesta |
|---|---|---|
| GET | `/api/antecedentes` | Lista completa |
| POST | `/api/antecedentes` | 201 con el registro creado |
| GET | `/api/antecedentes/{id}` | 200 con el registro, o 404 |
| PUT | `/api/antecedentes/{id}` | 200 con el actualizado, o 404 |
| DELETE | `/api/antecedentes/{id}` | 204 sin contenido, o 404 |

### Antecedentes familiares

| Verbo | Ruta | Respuesta |
|---|---|---|
| GET | `/api/antecedentes-familiares` | Lista completa |
| POST | `/api/antecedentes-familiares` | 201 con el registro creado |
| GET | `/api/antecedentes-familiares/{id}` | 200 con el registro, o 404 |
| PUT | `/api/antecedentes-familiares/{id}` | 200 con el actualizado, o 404 |
| DELETE | `/api/antecedentes-familiares/{id}` | 204 sin contenido, o 404 |

### Alergias

| Verbo | Ruta | Respuesta |
|---|---|---|
| GET | `/api/alergias` | Lista completa |
| POST | `/api/alergias` | 201 con el registro creado |
| GET | `/api/alergias/{id}` | 200 con el registro, o 404 |
| PUT | `/api/alergias/{id}` | 200 con el actualizado, o 404 |
| DELETE | `/api/alergias/{id}` | 204 sin contenido, o 404 |

### Ejemplos para Postman

Los tres recursos reciben la historia clínica como objeto anidado con su `id` (igual que Producto recibía `categoria: { "id": 1 }` en el laboratorio de la Semana 4), no como un número suelto:

```
POST http://localhost:8080/api/historias
{ "pacienteId": 1 }

POST http://localhost:8080/api/antecedentes
{
  "historiaClinica": { "id": 1 },
  "categoria": "Cirugia",
  "descripcion": "Apendicectomia",
  "fechaRegistro": "11/09/2026",
  "observaciones": "Sin complicaciones"
}

POST http://localhost:8080/api/antecedentes-familiares
{
  "historiaClinica": { "id": 1 },
  "enfermedad": "Diabetes",
  "parentesco": "Padre",
  "fechaRegistro": "11/09/2026",
  "observaciones": "Diagnosticado a los 50 anios"
}

POST http://localhost:8080/api/alergias
{
  "historiaClinica": { "id": 1 },
  "nombre": "Penicilina",
  "tipo": "Medicamento",
  "reaccion": "Erupcion cutanea",
  "fechaRegistro": "11/09/2026",
  "observaciones": "Reaccion reportada por el paciente"
}
```

---

## Vistas web

| Ruta | Qué muestra |
|---|---|
| `/` o `/panel` | Panel de navegación con enlaces a todo el módulo |
| `/pacientes` | Tabla con los pacientes registrados |
| `/pacientes/nuevo` | Formulario de registro de paciente |
| `/historias` | Tabla con las historias clínicas |
| `/historias/nueva` | Formulario para generar una historia clínica eligiendo el paciente |
| `/historias/{id}` | Ficha de la historia clínica: datos del paciente, antecedentes y alergias |
| `/antecedentes` | Tabla con los antecedentes personales |
| `/antecedentes/nuevo` | Formulario de registro de antecedente personal |
| `/antecedentes-familiares` | Tabla con los antecedentes familiares |
| `/antecedentes-familiares/nuevo` | Formulario de registro de antecedente familiar |
| `/alergias` | Tabla con las alergias |
| `/alergias/nuevo` | Formulario de registro de alergia |

---

## Base de datos

La base se llama `historia_clinica_db`. Hay que crearla una sola vez:

```sql
CREATE DATABASE historia_clinica_db;
```

Las tablas las genera Hibernate solo al arrancar. `application.properties` tiene `ddl-auto=create-drop`, es decir que Hibernate borra y vuelve a crear todas las tablas cada vez que se reinicia la aplicación — útil mientras se sigue definiendo el modelo, pero hay que cambiarlo a `update` antes de la entrega para no perder los datos de prueba entre ejecuciones. Hasta ahora se crean `paciente`, `historia_clinica`, `condicion_medica`, `historia_condicion` (tabla intermedia del `@ManyToMany`), `antecedente`, `antecedente_familiar` y `alergia`.

La configuración de conexión está en `src/main/resources/application.properties` y usa el usuario `root` sin contraseña.

---

## Cómo ejecutar el proyecto

```
mvnw.cmd spring-boot:run
```

La aplicación queda en http://localhost:8080

---

## Evaluación 02 (terminada)

La Evaluación 02 pide, sobre este mismo proyecto: relaciones JPA con CRUD (P1), auditoría (P2), gestión de usuarios y roles (P3), su frontend (P4) y control de acceso por rol (P5). El reparto está en [EVALUACION-02-REPARTO.md](EVALUACION-02-REPARTO.md) y el guion de exposición en [EXPOSICION-EVALUACION-02.md](EXPOSICION-EVALUACION-02.md).

| Integrante | Preguntas |
|---|---|
| **Lucas Inga** | P1 (Paciente, HistoriaClinica, CondicionMedica), P3 usuarios y roles, P4 vistas de usuarios y roles |
| **Renzo León** | P1 (Antecedente, AntecedenteFamiliar, Alergia), P2 auditoría con AOP, P5 Spring Security por rol |

- [x] `pom.xml`: se agregaron `spring-boot-starter-security` y `spring-boot-starter-aspectj`.
- [x] `ddl-auto=update`: los datos ya no se borran al reiniciar.
- [x] P1: `HistoriaClinica` tiene ahora el lado `@OneToMany` de `Antecedente`, `AntecedenteFamiliar` y `Alergia` (con `cascade` y `orphanRemoval`: al eliminar la historia se eliminan sus registros). Las tres vistas tienen botón **Editar** (`/antecedentes/editar/{id}`, `/antecedentes-familiares/editar/{id}`, `/alergias/editar/{id}`) que reutiliza el formulario.
- [x] P2: auditoría con AOP. `AuditoriaAspect` (`@Around`) intercepta los métodos `guardar`, `crear`, `actualizar`, `eliminar`, `agregar`, `activar` y `desactivar` de **todos** los services y guarda en la tabla `auditoria` el usuario, la fecha y hora, la operación (REGISTRO / MODIFICACION / ELIMINACION), la entidad y el id del registro. La bitácora se ve en `/auditoria`, con enlace en el panel. Los services que se agreguen después (por ejemplo, `UsuarioService` y `RolService`) quedan auditados solos si usan esos mismos nombres de método.
- [x] P5: login y control de acceso por rol. `UserDetailsServiceImpl` carga el `Usuario` y su `Rol` (no deja entrar si el usuario o el rol están inactivos). `SecurityConfig` valida las rutas en el backend: ADMINISTRADOR → usuarios, roles y auditoría; MEDICO → historias, antecedentes, alergias y condiciones; RECEPCIONISTA → pacientes y citas; pacientes → los tres. El login es un formulario propio (`/login`), redirige al panel, y el panel muestra solo las opciones del rol. Una ruta no permitida muestra `/acceso-denegado` (403).
- [x] Guion de exposición de la Evaluación 02: [EXPOSICION-EVALUACION-02.md](EXPOSICION-EVALUACION-02.md).

**Usuarios de prueba** (los crea `DataLoader`): `admin` / `admin123`, `medico` / `medico123`, `recepcion` / `recepcion123`.

## Pendiente

- Probar todos los endpoints en Postman (incluidos los nuevos de `Paciente` y `HistoriaClinica`) y tomar las capturas para el informe.
- Verificar en la base de datos que las llaves foráneas de `antecedente`, `antecedente_familiar` y `alergia` hacia `historia_clinica` se crearon correctamente.
- Revisar la relación `AntecedenteFamiliar` frente a `CondicionMedica`: hay dos formas de registrar antecedentes familiares (la entidad propia de Renzo, ya con relación real hacia `HistoriaClinica`, y el catálogo `@ManyToMany` de Lucas) y conviene que el equipo decida si se unifican antes de la entrega.
- Capturas de pantalla del código y evidencia de la base de datos para el informe final.
