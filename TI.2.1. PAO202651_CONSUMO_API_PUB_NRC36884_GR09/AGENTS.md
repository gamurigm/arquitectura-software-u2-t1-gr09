# Taller de consumo de APIs públicas GR09

## Alcance y referencias

- Este proyecto adapta la estructura de `U1/TI.1.1/TI.1.1. PAO202651_CELULAR_BDDLOCAL_RESTFUL_NRC36884_GR09` al taller de países. Ese proyecto es una referencia de solo lectura: reutilizar su organización de carpetas, no copiar sus archivos, nombres de Celulares, código ni datos.
- Antes de implementar, leer `T1_consumo_api_pub_gr09/PASOS.md` y el documento original en `requisitos/`. El plan diferencia requisitos del taller, correcciones verificadas y decisiones de diseño.
- Único cliente a implementar: aplicación de escritorio Java Swing con MVC, DAO, REST Countries y MySQL. No implementar clientes web, móvil ni consola; sus carpetas solo reservan la estructura de la plantilla.
- Conservar el proyecto web existente `T1_consumo_api_pub_gr09/` hasta acordar expresamente su reorganización. El nuevo proyecto Maven desktop irá en `04. CLIESC/T1_consumo_api_pub_gr09_desktop/`.
- Las carpetas de otros clientes, compartidos y servidor reservan su ubicación; su existencia no amplía el alcance.

## Directorios

La raíz de este taller es `U2/T1/TI.2.1. PAO202651_CONSUMO_API_PUB_NRC36884_GR09`. Mantener estos nombres, numeración, espacios y subdirectorios documentales de la plantilla:

```text
TI.2.1. PAO202651_CONSUMO_API_PUB_NRC36884_GR09/
├── .gitignore
├── AGENTS.md
├── AWs_bdd.md
├── GR09_Paises.sws                    # Workspace real de PowerDesigner, pendiente
├── requisitos/                       # Enunciado original, conservar
├── T1_consumo_api_pub_gr09/            # Proyecto existente y PASOS.md
├── 01. UML/
│   ├── 01. ERS/
│   ├── 02. ECUD/
│   ├── 03. DIAGRAMAS UML/
│   │   ├── 01_CASOS_DE_USO/ECUD/
│   │   ├── 02_ACTIVIDAD/ECUD/
│   │   ├── 03_SECUENCIA/ECUD/
│   │   ├── 04_CLASES/
│   │   └── 05_ARQUITECTURA/
│   └── PowerDesigner/
├── 02. MER/
│   ├── 01. CONCEPTUAL/
│   ├── 02. LOGICO/
│   ├── 03. FISICO/
│   └── PowerDesigner/
├── 03. BDD/
├── 04. CLICON/
├── 04. CLIESC/
│   └── T1_consumo_api_pub_gr09_desktop/ # Crear al comenzar a codificar
├── 04. CLIMOV/
├── 04. CLIWEB/
├── 04. COMPARTIDO/
├── 04. SERVIDOR/
└── 05. DOCUMENTACION/
    ├── 01. MANUAL TECNICO/Capturas/
    ├── 02. PRESENTACION/
    └── 03. PLANIFICACION/             # Planes visuales propios de este taller
```

- `03. BDD` contiene SQL para MySQL; `02. MER` contiene modelos de datos. Los proyectos, paquetes y clases internos se adaptan a Java/Maven y Países; la plantilla de Celulares no define su tecnología.
- Para publicar en Git las carpetas reservadas que sigan vacías, usar un `.gitkeep` vacío y retirarlo cuando se agreguen archivos reales. No agregar proyectos ficticios para conservarlas.

## PowerDesigner

- El workspace `.sws` debe estar en la raíz: `GR09_Paises.sws`. Está pendiente de creación y configuración real; por ahora esta instrucción sustituye al archivo vacío.
- Al configurarlo, crear y guardar un workspace válido con PowerDesigner y enlazar los proyectos/modelos UML y MER mediante rutas relativas.
- Conservar proyectos `.prj`, modelos editables y vistas previas en las ubicaciones correspondientes de `01. UML` y `02. MER`. `.sws` es el workspace y no sustituye los modelos ni sus proyectos.
- Preservar identidades y distribución manual de modelos existentes al actualizar; generar los nuevos modelos con nombres GR09 propios de Países.

## Plataforma y desarrollo

- AWS es la plataforma cloud elegida por el usuario. Documentar y configurar la solución cloud para AWS; servicios concretos, región, red y despliegue se definirán después.
- Mantener MySQL para este taller Java. Amazon RDS for MySQL es la propuesta para persistencia en AWS; no está provisionado ni confirmado como servicio definitivo.
- El cliente Swing se ejecuta en el equipo del usuario. Diseñar por separado cualquier componente que vaya a AWS; documentar el acceso seguro a la base de datos antes de conectarlo.
- Usar Apache NetBeans como IDE de desarrollo y ejecución Java, Maven y JDK 17 o superior compatible con las dependencias elegidas.
- Mantener MVC: vistas presentan y capturan datos; controlador coordina; modelo representa datos; DAO contiene SQL; cliente API gestiona HTTP y JSON.
- Interfaces visualmente cuidadas: español consistente, jerarquía tipográfica, espaciado, imágenes pertinentes, banderas, estados de carga/vacío/error y navegación por teclado. La maqueta en `05. DOCUMENTACION/03. PLANIFICACION/interfaz-propuesta.svg` es una propuesta, no evidencia de una aplicación funcionando.
- HTTP y JDBC deben ejecutarse fuera del hilo de eventos de Swing. Guardar secretos en configuración externa; no incluir claves o contraseñas en código, imágenes, logs o Git.
- Verificar el contrato vigente de REST Countries antes de codificar: las pruebas del 16 de noviembre de 2026 detectaron el retiro de v3.1. HTTP 200 por sí solo no acredita datos válidos. La clave demo no sustituye una clave de acceso real.

## Evidencias y entrega

- Usar las plantillas GR09 exactas de Word y PowerPoint cuando se suministren para los entregables. Adaptar contenido y evidencias a Países conservando diseño, portada, estilos y estructura.
- Verificar los artefactos y proyectos afectados antes de afirmar que funcionan. Distinguir maqueta, prueba con datos simulados y ejecución real.
- Mantener otros proyectos de estudiantes en solo lectura. Los archivos sueltos de la referencia no son entregables de este taller.
- Revisar el estado de Git antes de trabajar; conservar cambios del usuario. Commit solo del alcance trabajado; push a `origin/main` únicamente por petición del usuario.
