# Introducción tras restauración y versión instalada

## Cambios
- La finalización de la introducción se guarda en `noBackupFilesDir/onboarding_completed`, no en preferencias restaurables. Los ajustes generales se conservan; el indicador antiguo del JSON se ignora.
- Ajustes permite revisar la introducción y los permisos sin borrar datos ni reiniciar el indicador.
- Ajustes muestra la versión y el código reales de BuildConfig, en español e inglés.

## Verificación
- Compilación debug y APK de pruebas correctos; 49 pruebas unitarias, cero fallos. Cuatro pruebas nuevas cubren preferencias restauradas, persistencia del marcador, instalación sin marcador y cambios de ajustes.
- Codex_UI_API_33, emulator-5558: primera entrada, omitir, reinicio sin repetir introducción, acción desde Ajustes, regreso a Ajustes y pantalla de permisos comprobados.
- Simulación de restauración: eliminar únicamente el marcador del emulador desechable manteniendo preferencias vuelve a mostrar Bienvenido. No equivale a una reinstalación real desde Play con restauración de Google.
- Captura `artifacts/onboarding-2026-10-05/settings-version-es.png`: versión local debug 5.0.2, código 11; no es una entrega nueva de Play.
- `git diff --check` sin errores. No se ha hecho push de este cambio.

## Migración y pendientes
- Usuarios que actualicen desde una versión sin marcador verán la introducción una vez; siguientes reinicios/actualizaciones la conservarán como completada.
- Pendiente validar una reinstalación real de Play y entregar el cambio mediante el pipeline. No se modificó el dispositivo físico ni emulator-5554.
