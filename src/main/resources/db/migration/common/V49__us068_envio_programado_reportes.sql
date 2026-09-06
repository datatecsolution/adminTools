-- US-068: envío programado de reportes por correo (API).
--
-- report_schedules: qué reporte, con qué frecuencia, a qué hora y a quién.
-- report_send_log: histórico de envíos (OK/FALLO con intentos y detalle) —
-- también es la clave de idempotencia del scheduler: un envío AUTO por
-- (programación, fecha programada). Sin UNIQUE en BD a propósito: el
-- chequeo lo hace la API (instancia única) y el envío MANUAL comparte tabla.
--
-- Aditiva: no toca nada existente; el Swing no usa estas tablas.
CREATE TABLE report_schedules (
    id INT NOT NULL AUTO_INCREMENT,
    reporte VARCHAR(20) NOT NULL COMMENT 'DAILY|ABC|ROTATION|PURCHASE|CATEGORY',
    frecuencia VARCHAR(10) NOT NULL COMMENT 'DIARIA|SEMANAL',
    -- INT (no TINYINT): el ddl-auto=validate de la API espera Types#INTEGER
    dia_semana INT NULL COMMENT '1=lunes..7=domingo, solo SEMANAL',
    hora TIME NOT NULL,
    destinatarios VARCHAR(500) NOT NULL COMMENT 'correos separados por coma',
    activo TINYINT(1) NOT NULL DEFAULT 1,
    creado DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

CREATE TABLE report_send_log (
    id INT NOT NULL AUTO_INCREMENT,
    schedule_id INT NOT NULL,
    fecha_programada DATE NOT NULL,
    fecha_envio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    origen VARCHAR(10) NOT NULL DEFAULT 'AUTO' COMMENT 'AUTO|MANUAL',
    estado VARCHAR(10) NOT NULL COMMENT 'OK|FALLO',
    intentos INT NOT NULL DEFAULT 1,
    detalle VARCHAR(500) NULL,
    PRIMARY KEY (id),
    KEY idx_sendlog_sched_fecha (schedule_id, fecha_programada),
    CONSTRAINT fk_sendlog_schedule FOREIGN KEY (schedule_id)
        REFERENCES report_schedules(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
