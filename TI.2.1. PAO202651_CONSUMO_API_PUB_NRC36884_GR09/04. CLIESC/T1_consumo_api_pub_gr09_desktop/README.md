# Atlas GR09 · Cliente desktop

Aplicación Java Swing para consultar REST Countries, guardar países en MySQL y administrar el catálogo local. El único cliente implementado en el taller es escritorio.

## Requisitos

- JDK 17 o posterior.
- Apache NetBeans con soporte Maven.
- MySQL 8.0.16 o posterior.
- Una API key de REST Countries v5 para consultas reales.

## Preparar MySQL

Ejecuta `../../03. BDD/01_crear_api_paises.sql` en MySQL Workbench. Configura el acceso de red y el usuario de aplicación en AWS antes de usar una base RDS. El script crea la base y la tabla si aún no existen y no elimina datos.

## Configuración externa

Define estas variables de entorno en la sesión que inicia NetBeans:

| Variable | Uso |
| --- | --- |
| `DB_URL` | JDBC URL, por ejemplo `jdbc:mysql://HOST:3306/api_paises?sslMode=VERIFY_IDENTITY` |
| `DB_USER` | Usuario con permisos solo sobre `api_paises.pais` |
| `DB_PASSWORD` | Contraseña de ese usuario |
| `REST_COUNTRIES_API_KEY` | Token para la cabecera `Authorization: Bearer` |

No guardes valores reales en este README, el código, las capturas ni Git. En PowerShell puedes definirlos en una terminal antes de iniciar NetBeans:

```powershell
$env:DB_URL = 'jdbc:mysql://HOST:3306/api_paises?sslMode=VERIFY_IDENTITY'
$env:DB_USER = 'paises_app'
$env:DB_PASSWORD = 'CONFIGURAR_LOCALMENTE'
$env:REST_COUNTRIES_API_KEY = 'CONFIGURAR_LOCALMENTE'
```

El escritorio puede abrirse sin configuración para revisar la interfaz, pero las operaciones que necesiten MySQL o REST Countries mostrarán el motivo y no simularán resultados exitosos.

## Ejecutar

Abre `04. CLIESC/T1_consumo_api_pub_gr09_desktop` en Apache NetBeans y usa **Run Project**. El proyecto es Maven JAR y no requiere desplegar el WAR Jakarta EE que permanece en `T1_consumo_api_pub_gr09`.

La integración usa el contrato REST Countries v5; si la API devuelve el bloque de datos de demostración, el cliente lo rechaza para importaciones reales. La descarga de banderas usa el CDN de banderas enlazado en la documentación oficial.

## Seguridad

Una aplicación desktop no puede ocultar credenciales que necesita para conectarse directamente a RDS. Mantén la base privada, limita el security group a la IP de desarrollo y usa un usuario con permisos mínimos. Para distribuirla a usuarios fuera del entorno controlado, una capa servidor sería necesaria para no entregar credenciales de base de datos con el cliente.
