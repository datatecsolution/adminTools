-- Bases v0 solo tenían tipo_permiso 1 (admin), 2 (cajero), 3 (ventas).
-- El tipo 1 original era admin con acceso total, que ahora corresponde a tipo 4.
-- El nuevo tipo 1 es "Supervisor" con acceso limitado.
-- Esta migración promueve todos los admins v0 al tipo 4.
UPDATE usuario SET tipo_permiso = 4 WHERE tipo_permiso = 1;
