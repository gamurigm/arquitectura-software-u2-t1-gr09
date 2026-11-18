CREATE DATABASE IF NOT EXISTS api_paises
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE api_paises;

CREATE TABLE IF NOT EXISTS pais (
    id INT NOT NULL AUTO_INCREMENT,
    codigo_iso CHAR(3) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    capital VARCHAR(255) NULL,
    region VARCHAR(100) NOT NULL,
    poblacion BIGINT NULL,
    moneda VARCHAR(255) NULL,
    bandera_url VARCHAR(512) NULL,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_pais_codigo_iso UNIQUE (codigo_iso),
    CONSTRAINT ck_pais_poblacion_no_negativa CHECK (poblacion IS NULL OR poblacion >= 0),
    INDEX ix_pais_region (region),
    INDEX ix_pais_nombre (nombre)
) ENGINE = InnoDB;
