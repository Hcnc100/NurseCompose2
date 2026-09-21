# Plan incremental: recordatorios de medicamentos

## Objetivo
Incorporar recordatorios de medicamentos sin perder los registros de mediciones existentes, con una experiencia clara y compatible con Android moderno.

## Estado encontrado
- La aplicación actual es un módulo Android Compose con Room, Hilt y Compose Destinations; las pantallas actuales se centran en captura y gráficos de glucosa, presión, temperatura y oxígeno.
- La navegación inferior tiene cuatro entradas y se genera a partir de `HomeNavItems`; una quinta entrada de medicamentos puede colocarse en el centro.
- `NourseCompose` contiene una implementación antigua de alarmas, pero debe ser referencia funcional, no código para copiar: no cubre las restricciones actuales de Android ni el flujo completo de medicamentos.
- El proyecto usa `compileSdk`/`targetSdk` 36, pero aún no declara permisos para notificaciones, alarmas ni reinicio. El BoM de Compose actual es de 2023 y requiere actualización controlada.

## Fase 0 — Línea base y seguridad de la actualización
1. Confirmar que la rama está limpia y ejecutar el ensamblado y pruebas actuales.
2. Inventariar dependencias y actualizar únicamente versiones compatibles con Kotlin 2.0, AGP y Gradle instalados; primero Compose BoM y AndroidX, después bibliotecas no críticas.
3. Eliminar la configuración obsoleta de compilador Compose si el plugin Kotlin Compose ya la reemplaza y el build lo confirma.
4. No modificar el identificador publicado `com.nullpointer.nourseCompose` ni el flujo de publicación.

**Criterio de salida:** build de depuración y pruebas existentes verdes antes y después de cada grupo de versiones.

## Fase 1 — Modelo y persistencia local
1. Añadir `MedicationReminderEntity` a Room: nombre, dosis opcional, comentario, URI de foto, fecha/hora inicial, fecha final opcional, intervalo en horas y estado activo.
2. Crear DAO, repositorio, datasource y bindings Hilt siguiendo el patrón de mediciones existente.
3. Subir la versión de la base y definir una migración no destructiva; las mediciones existentes no deben borrarse.
4. Implementar una función pura que calcule las horas siguientes para: un único día, repetición indefinida y rango de fechas.

**Criterio de salida:** crear, editar, desactivar y eliminar recordatorios sobre base local; pruebas unitarias de cálculo horario.

## Fase 2 — Detección de coincidencias
1. Calcular las ocurrencias de cada recordatorio activo dentro de una ventana razonable (por ejemplo, 24–48 horas).
2. Avisar antes de guardar cuando otro medicamento coincida en la misma hora/minuto; mostrar ambos nombres y permitir cancelar o guardar explícitamente.
3. Mantenerlo como advertencia, no como recomendación médica ni bloqueo absoluto.

**Criterio de salida:** los casos con misma hora, distinto intervalo y rangos superpuestos quedan cubiertos por pruebas.

## Fase 3 — Pantallas y navegación
1. Insertar **Medicamentos** entre Presión y Temperatura, dejándolo como quinta opción centrada del bottom bar.
2. Crear lista de recordatorios, estado vacío y acción para crear/editar.
3. Formulario: medicamento, dosis, comentario, foto opcional, hora inicial, intervalo, un solo día/rango/indefinido y vista previa de las siguientes tomas.
4. Obtener foto mediante el selector del sistema y persistir permisos de lectura de la URI cuando estén disponibles; no pedir permisos amplios de galería.

**Criterio de salida:** flujo usable de alta, edición y consulta sin depender de la red.

## Fase 4 — Notificaciones y alarmas
1. Crear canal de notificaciones, receptor de `BroadcastReceiver` y reprogramación del siguiente evento después de cada aviso.
2. Declarar y solicitar `POST_NOTIFICATIONS` en Android 13+ antes de activar avisos.
3. Evaluar `SCHEDULE_EXACT_ALARM` en Android 12+: solicitar acceso especial sólo cuando el usuario active alarmas puntuales; disponer de notificación programada no exacta como alternativa si no se concede.
4. Declarar `RECEIVE_BOOT_COMPLETED` y reprogramar recordatorios activos tras reinicio, actualización de la app o cambio de hora/zona horaria.
5. No iniciar una pantalla a pantalla completa ni sonido invasivo por defecto; el modo alarma queda explícitamente configurable por el usuario.

**Criterio de salida:** un recordatorio llega con la app cerrada, se reprograma y sobrevive a reinicios en un dispositivo físico.

## Fase 5 — Calidad, permisos y entrega
1. Pruebas unitarias para calendario, rangos y colisiones; pruebas instrumentadas para DAO y navegación básica.
2. Probar permisos denegados, alarmas no exactas, Android 12, Android 13+ y la restauración tras boot.
3. Revisar copias de seguridad, exportación/importación y la política de Play para permisos de alarmas exactas antes de liberar.
4. Documentar los permisos y limitaciones de puntualidad para el usuario.

## Orden de trabajo propuesto
1. Fase 0.
2. Fases 1 y 2 (base funcional verificable, sin permisos).
3. Fase 3 (interfaz y foto).
4. Fase 4 (notificaciones/alarma).
5. Fase 5 (pruebas, actualización final de dependencias y revisión de entrega).

## Decisiones que se validarán antes de liberar
- Si "alarma" significa sólo una notificación de alta prioridad o también sonido persistente/pantalla completa.
- Intervalos permitidos (por defecto se propondrá un mínimo seguro de una hora, sin dar consejo clínico).
- Si los recordatorios y fotografías deben incluirse en la exportación/importación existente.
