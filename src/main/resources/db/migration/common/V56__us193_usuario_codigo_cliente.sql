-- =====================================================================
-- V56 — US-193: vínculo directo usuario ↔ cliente (acceso a la app)
--
-- «Dar acceso a la app» (US-159) crea un usuario VENDEDOR para un cliente,
-- pero no quedaba registrado de qué cliente era: el fix US-189 lo deducía de
-- cliente.id_vendedor → empleado → usuario, y como id_vendedor es "quién
-- atiende al cliente" (1 = empleado genérico, compartido por varios
-- usuarios) bloqueaba a casi todos los clientes (Samuel: 50 de 54).
--
-- codigo_cliente = cliente cuyo acceso a la app es este usuario. Regla (en
-- la API): un cliente tiene como máximo UN usuario vendedor ACTIVO
-- vinculado; desactivar el usuario libera al cliente. No toca
-- usuario.codigo_empleado, empleados.usuario ni cliente.id_vendedor.
--
-- Aditiva: columna NULL; los accesos existentes se vinculan a mano desde
-- Usuarios (decisión del usuario 2026-09-24). El Swing inserta en usuario
-- nombrando columnas, así que no le afecta.
-- =====================================================================

ALTER TABLE usuario
  ADD COLUMN codigo_cliente INT NULL COMMENT 'US-193: cliente cuyo acceso a la app es este usuario vendedor',
  ADD KEY idx_usuario_codigo_cliente (codigo_cliente);
