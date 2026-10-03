# Privacidad: verificación y pendientes

## Aprobado
- Audiencia 13+, no dirigida a menores de 13. Guardada en Console, pendiente de revisión/publicación.
- Excluir base de salud, medicamentos e historial de backup y transferencias Android; mantener ajustes generales. No elimina copias anteriores.
- El titular publicará el HTML en Firebase. Se conservan las tres clases container-terms del sitio proporcionado.
- El titular solicita mantener la política en inglés, como la original. Usar `docs/privacy-policy.en.html` para reemplazarla; la versión española queda como alternativa.

## Verificado
- Analytics: eventos 2 meses, usuarios 14 meses, reinicio con actividad nueva habilitado. Configuración sin cambios.
- Analytics y Crashlytics habilitados por defecto, sin interruptor en la app. Los errores incluyen excepciones y contexto del tipo de medición; no garantizar ausencia absoluta de contexto sensible.
- Build debug, APK de instrumentación y pruebas unitarias correctos. Tres pruebas PrivacyConfigurationTest pasaron en API 34.
- Enlace a política HTTPS incorporado en Ajustes con mensaje de fallo.
- Política anterior de 2023 ya excluía menores de 13, pero omitía medicamentos, servicios Firebase específicos, conservación y backup. Reemplazo elimina promesas de controles de cookies y eliminación remota inmediata no implementados.

## Pendiente
- Política inglesa verificada públicamente con fecha 3 de octubre de 2026. Borrador de Seguridad de datos guardado: cifrado en tránsito, ausencia de cuentas y categorías de ubicación aproximada, uso, fallos/diagnóstico e identificadores. Aún requiere revisión final, especialmente la declaración histórica de información sanitaria compartida y configuración de compartición de Analytics; no se envió a revisión.
- Video de alarma grabado y validado en emulator-5556; el titular proporcionó enlace YouTube. Declaración FGS guardada con ese enlace y confirmación de Console; pendiente de envío/revisión. Véase ALARM_VIDEO_2026-10-03.md.
- Publicar docs/privacy-policy.es.html y comprobar URL pública; ajustar fecha si se publica otro día.
- Completar Seguridad de datos de todos los SDK/versiones activas. No se certificó ni envió formulario final. Revisar ubicación aproximada, interacciones, identificadores, errores/diagnóstico y contexto sensible.
- Enviar a revisión la declaración de servicios en primer plano ya guardada. Su aprobación no está confirmada.
- Renovar capturas de la versión firmada final; las anteriores son debug.
- No se envió esta actualización de privacidad a producción ni a revisión de publicación.
