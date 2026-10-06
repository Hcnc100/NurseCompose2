# Workflows independientes

## Configuración
- `closed-testing.yml` restaurado idéntico al original, con pruebas, firma y publicación a Internal.
- Nuevo `production.yml`: push a master o ejecución manual desde master promueve a Production el binario ya probado en Internal, sin recompilar ni crear otro contador.
- Exige un run Internal exitoso entre los 100 últimos con árbol Git idéntico a master y su código activo en Internal. Si falta, falla sin publicar: ejecutar Internal con esa fuente primero.
- Producción tiene cola propia sin cancelación activa. Internal mantiene su configuración original. Ediciones simultáneas de Play pueden fallar; revisar Console antes de repetir.
- Reutiliza cuenta de servicio; requiere permisos Production. No modifica Internal; rechaza retrocesos y evita repetir una versión presente en Production.
- Si Google ya revisa cambios, falla sin cancelar esa revisión (`ERROR_IF_IN_REVIEW`).
- La cuenta de servicio debe tener permiso para publicar en Production. Google puede requerir revisión; publicación gestionada puede retener cambios aprobados. Éxito de upload no demuestra disponibilidad pública inmediata.

## Activación y verificación
- Primero probar estos archivos en testing; después incorporar el mismo árbol aprobado a master para activar la promoción automática.
- No se ha actualizado ni enviado `master` en esta tarea, ni cambiado permisos de Google Play.
- Validación local de YAML, sintaxis Python y comprobación de que Internal no tiene diff. Entrega real pendiente de verificación en Actions/Play.
