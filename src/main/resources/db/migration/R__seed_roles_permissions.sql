-- Semilla repetible e idempotente: catalogo de permisos y roles predefinidos.
-- Fuente de verdad: docs/01-requisitos/matriz-roles-permisos.md
-- Flyway reejecuta este script cada vez que cambia su contenido.

INSERT INTO iam_permissions (code, resource, action, description) VALUES
    ('case:create:self',           'case',    'create:self',        'Radicar una solicitud propia'),
    ('case:create:staff',          'case',    'create:staff',       'Radicar un reporte sobre un estudiante'),
    ('case:read:own',              'case',    'read:own',           'Ver los casos propios'),
    ('case:read:assigned',         'case',    'read:assigned',      'Ver los casos asignados a si mismo o a su area'),
    ('case:read:any',              'case',    'read:any',           'Ver cualquier caso, incluidos los confidenciales'),
    ('case:update:classification', 'case',    'update:classification', 'Confirmar o corregir la clasificacion'),
    ('case:assign',                'case',    'assign',             'Asignar y reasignar casos'),
    ('case:transition',            'case',    'transition',         'Cambiar el estado de un caso'),
    ('case:followup:create',       'case',    'followup:create',    'Registrar seguimientos'),
    ('case:close',                 'case',    'close',              'Cerrar casos'),
    ('student:read:dossier',       'student', 'read:dossier',       'Ver el expediente consolidado de un estudiante'),
    ('student:read:group',         'student', 'read:group',         'Ver los estudiantes de los grupos propios'),
    ('student:import',             'student', 'import',             'Cargar estudiantes desde archivo'),
    ('mood:create:self',           'mood',    'create:self',        'Registrar el check-in de animo propio'),
    ('mood:read:aggregate',        'mood',    'read:aggregate',     'Ver tendencias de animo agregadas'),
    ('risk:read:student',          'risk',    'read:student',       'Ver el puntaje y los factores de un estudiante'),
    ('risk:read:dashboard',        'risk',    'read:dashboard',     'Ver el tablero agregado de riesgo'),
    ('risk:model:manage',          'risk',    'model:manage',       'Editar factores, pesos y umbrales'),
    ('report:generate',            'report',  'generate',           'Generar reportes institucionales'),
    ('report:template:manage',     'report',  'template:manage',    'Crear y versionar plantillas de reporte'),
    ('user:read',                  'user',    'read',               'Consultar usuarios'),
    ('user:manage',                'user',    'manage',             'Crear, editar y desactivar usuarios'),
    ('role:read',                  'role',    'read',               'Consultar roles'),
    ('role:manage',                'role',    'manage',             'Crear y editar roles y sus permisos'),
    ('audit:read',                 'audit',   'read',               'Consultar la bitacora de auditoria'),
    ('ai:metrics:read',            'ai',      'metrics:read',       'Ver metricas de precision del clasificador')
ON CONFLICT (code) DO UPDATE
    SET resource = EXCLUDED.resource,
        action = EXCLUDED.action,
        description = EXCLUDED.description;

INSERT INTO iam_roles (code, name, description, is_system) VALUES
    ('STUDENT',      'Estudiante',              'Radica solicitudes propias y registra su estado de animo', TRUE),
    ('TEACHER',      'Docente',                 'Reporta situaciones de los estudiantes de sus grupos',     TRUE),
    ('WELLBEING',    'Profesional de bienestar','Atiende y resuelve los casos asignados',                   TRUE),
    ('COORDINATOR',  'Coordinador de permanencia','Supervisa casos, riesgo y reportes',                     TRUE),
    ('ADMIN',        'Administrador',           'Gestiona usuarios, roles, permisos y parametros',          TRUE)
ON CONFLICT (code) DO UPDATE
    SET name = EXCLUDED.name,
        description = EXCLUDED.description,
        is_system = EXCLUDED.is_system;

-- Asignacion rol → permisos. Se recalcula por completo en cada ejecucion,
-- de modo que la matriz del documento sea siempre la vigente.
DELETE FROM iam_role_permissions
WHERE role_id IN (SELECT id FROM iam_roles WHERE is_system = TRUE);

INSERT INTO iam_role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM iam_roles r
JOIN iam_permissions p ON TRUE
WHERE (r.code = 'STUDENT' AND p.code IN (
        'case:create:self', 'case:read:own', 'mood:create:self'))
   OR (r.code = 'TEACHER' AND p.code IN (
        'case:create:self', 'case:create:staff', 'case:read:own', 'student:read:group'))
   OR (r.code = 'WELLBEING' AND p.code IN (
        'case:create:self', 'case:create:staff', 'case:read:own', 'case:read:assigned',
        'case:update:classification', 'case:transition', 'case:followup:create', 'case:close',
        'student:read:dossier', 'student:read:group', 'mood:read:aggregate',
        'risk:read:student', 'risk:read:dashboard'))
   OR (r.code = 'COORDINATOR' AND p.code IN (
        'case:create:self', 'case:create:staff', 'case:read:own', 'case:read:assigned',
        'case:read:any', 'case:update:classification', 'case:assign', 'case:transition',
        'case:followup:create', 'case:close', 'student:read:dossier', 'student:read:group',
        'mood:read:aggregate', 'risk:read:student', 'risk:read:dashboard',
        'report:generate', 'ai:metrics:read'))
   OR (r.code = 'ADMIN' AND p.code IN (
        'case:create:self', 'case:create:staff', 'case:read:own', 'case:read:assigned',
        'case:read:any', 'case:update:classification', 'case:assign', 'case:transition',
        'case:followup:create', 'case:close', 'student:read:dossier', 'student:read:group',
        'student:import', 'mood:read:aggregate', 'risk:read:student', 'risk:read:dashboard',
        'risk:model:manage', 'report:generate', 'report:template:manage',
        'user:read', 'user:manage', 'role:read', 'role:manage', 'audit:read', 'ai:metrics:read'));
