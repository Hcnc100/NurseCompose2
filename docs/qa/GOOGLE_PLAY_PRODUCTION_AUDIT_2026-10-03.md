# Preparación de Google Play — 3 de octubre de 2026

## Resultado

**Todavía no se puede certificar que esté lista para producción.** Se revisaron código, ficha pública y política vinculada. No se verificaron los formularios privados, la configuración efectiva de Firebase ni la aprobación de la nueva versión en Play Console. Este documento no afirma cumplimiento legal completo.

## Evidencia pública

- [Ficha de NurseApp](https://play.google.com/store/apps/details?id=com.nullpointer.nourseCompose&hl=es&gl=MX): desarrollador NullPointerDev; versión pública 5.0.1, actualizada el 24 de julio de 2025 durante la consulta.
- [Política vinculada](https://ricardopajarocoatl.com/terms-and-conditions/C2TnDWRQv5eNTCcMkLUt): fecha 29 de diciembre de 2023, inglés, contacto nullpointerdeveloper@gmail.com. No describe suficientemente las funciones actuales de medicamentos y los SDK de Firebase.
- Seguridad de datos visible: «No se recogen datos», posible compartición de salud, ausencia de cifrado y de eliminación; requiere reconciliación con la nueva versión y todos los artefactos activos pertinentes, no copiar automáticamente esas respuestas.
- La versión pública antigua no demuestra que la nueva versión disponible en pruebas internas esté publicada en producción.

## Prioridades antes del envío

### 1. Privacidad y seguridad de datos

- Aprobar y publicar una política actualizada a partir de `docs/privacy-policy.es.md`; resolver todos sus pendientes y preparar una versión inglesa equivalente.
- Incluir un enlace accesible a esa política dentro de la aplicación; no se encontró en la revisión de Ajustes y recursos. Su incorporación aún está pendiente.
- Revisar Analytics y Crashlytics: colección, eventos, identificadores, excepciones, contexto sensible y retención real. La ausencia de permiso AD_ID no desactiva estos SDK.
- Completar Seguridad de datos con el comportamiento efectivo. La documentación de Firebase lista categorías que deben evaluarse por SDK y versión: no responder «sin recopilación» solo porque la base de datos sea local.
- Distinguir datos enviados al proveedor técnico de archivos compartidos voluntariamente por el usuario. No toda exportación equivale a «compartición» según la definición del formulario.
- Resolver copias de seguridad: `allowBackup=true` y reglas de ejemplo sin exclusiones permiten potencialmente incluir la base de datos. Elegir explícitamente mantenerlas y declararlas o excluir datos sensibles y probar restauración. No se cambió esa decisión en esta auditoría.
- Evitar prometer borrado completo: actualmente «eliminar todas las mediciones» no elimina todos los recordatorios, historiales, exportaciones o datos remotos.
- Revisar el mensaje de bienvenida «privately and offline»: los registros funcionan sin conexión, pero Firebase puede transmitir datos técnicos. Ajustar la promesa o implementar controles coherentes antes de publicar.

### 2. Ficha, salud y audiencia

- Sustituir descripciones antiguas por `docs/google-play-store-listing.md`, después de comprobarlas contra la versión final.
- Mantener el aviso explícito de que no es un dispositivo médico y no diagnostica, trata, cura ni previene condiciones médicas; recomendar consulta profesional.
- Completar la declaración de aplicaciones de salud según las funciones reales: registro manual y recordatorios, sin afirmar medición por sensores ni diagnóstico.
- Confirmar audiencia y compromiso de Familias. «Para todos» es una clasificación de contenido, no confirmación de que la app se dirija a niños.
- Confirmar datos del desarrollador, contacto, estado de verificación de la cuenta y cualquier requisito que Console indique para la categoría elegida.

### 3. Permisos y revisión técnica

- Se verificó `targetSdk=36` en el proyecto, no la configuración de un AAB futuro: comprobar el artefacto final.
- El manifiesto usa `SCHEDULE_EXACT_ALARM`, no `USE_EXACT_ALARM`. No confundir sus requisitos.
- Verificar declaraciones de servicio en primer plano y permiso de pantalla completa en Console; su presencia en el manifiesto no implica aprobación ni concesión automática.
- La selección de fotos usa el selector del sistema y la cámara externa: no declarar acceso general a la galería o permiso CAMERA inexistente.
- Revisar informe de pruebas previas al lanzamiento, Android vitals y pruebas del AAB firmado: migración, permisos denegados, reinicio, batería, idiomas, tamaños de fuente y alarmas. No se ejecutaron nuevas pruebas de emulador en esta auditoría documental.
- Mantener el pipeline en Internal testing. Un workflow exitoso no demuestra aprobación de producción; no se cambió el destino del pipeline.

### 4. Material y envío

- Renovar capturas siguiendo la lista de `docs/google-play-store-listing.md`. No se generaron ni enviaron capturas nuevas en esta auditoría.
- Revisar icono, imagen destacada y textos de español/inglés contra la versión final.
- Verificar clasificación, anuncios, acceso a la app, países y notas de versión en Console. Actualmente no se identificó inicio de sesión propio: no inventar credenciales de revisión.
- Si Console exige pruebas cerradas para esta cuenta, cumplirlas. No aplicar automáticamente requisitos de cuentas nuevas a una cuenta que ya publica una app.
- Enviar a revisión solo después de resolver los puntos anteriores. No se editó ni envió ningún formulario público o privado.

## Fuentes oficiales

- [Divulgación de datos de Firebase](https://firebase.google.com/docs/android/play-data-disclosure): evaluar los SDK instalados y su configuración, no asumir que la tabla de la última versión describe exactamente una versión antigua.
- [Seguridad de datos](https://support.google.com/googleplay/android-developer/answer/10787469): categorías, recopilación y excepciones de compartición.
- [Datos de usuario y privacidad](https://support.google.com/googleplay/android-developer/answer/10144311): política pública y dentro de la aplicación, tratamiento y eliminación.
- [Contenido y servicios de salud](https://support.google.com/googleplay/android-developer/answer/16679511): avisos y requisitos de aplicaciones de salud.
- [Declaración de aplicaciones de salud](https://support.google.com/googleplay/android-developer/answer/14738291).
- [Servicios en primer plano y pantalla completa](https://support.google.com/googleplay/android-developer/answer/13392821).

## Handoff

### Goal
Preparar ficha, privacidad y revisión de producción, aparte de capturas.

### Instructions
Conversación en español. No probar TalkBack del PDF. Mantener publicación automatizada en Internal; no publicar producción ni sustituir la política sin aprobación.

### Discoveries
Política pública de 2023 y declaraciones visibles necesitan reconciliación con Firebase, medicamentos, exportaciones y backup. Las declaraciones privadas y retención de Firebase siguen sin verificar.

### Accomplished
Borradores ES/EN de ficha, borrador español de privacidad y checklist de bloqueos. Sin cambios de funcionamiento ni publicación externa.

### Next Steps
Confirmar Firebase/audiencia/backup, finalizar política, añadir enlace en Ajustes, corregir promesa de bienvenida y obtener capturas del AAB final.

### Relevant Files
`docs/google-play-store-listing.md`, `docs/privacy-policy.es.md`, este informe; manifiesto, reglas de backup y recursos de bienvenida para cambios posteriores.
