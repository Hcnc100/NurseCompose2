# Evaluación del formulario de recordatorios

## Veredicto

Es una base adecuada, pero no es todavía el formato más intuitivo para este producto. Una revisión heurística no demuestra un «mejor UX» universal: hace falta probar tareas con personas usuarias, incluyendo baja visión y poca familiaridad tecnológica. Esta revisión evalúa la programación del recordatorio, no prescribe dosis ni frecuencia de medicamentos.

Se aplicó `ui-ux-pro-max`: agrupación de campos, información esencial primero, divulgación progresiva, recuperación de errores, teclado apropiado, escalado de texto y feedback de guardado. Las sugerencias genéricas de paleta/neumorfismo de la skill no se aplican automáticamente: conservar el tema rosa Material existente evita introducir otra inconsistencia.

## Lo que conviene conservar

- Destino independiente de Home, título de tarea y botón Atrás visible.
- Un único CTA principal fijo, con espacio reservado para no cubrir el formulario.
- Campos con etiquetas Material (no son placeholders sin etiqueta).
- Dosis/comentario identificados como opcionales.
- Error junto al campo y vista previa de próximas tomas.
- Filas completas seleccionables para switches y duración. Android recomienda blancos táctiles de 48dp y semántica de selección en la fila: [documentación oficial](https://developer.android.com/develop/ui/compose/accessibility/api-defaults).

## Problemas y prioridades

| Prioridad | Evidencia | Mejora |
|---|---|---|
| Alta | `MedicationReminderEditorScreen` llama `viewModel.save()` y navega de inmediato; `save()` lanza una coroutine y solo registra fallos en logs. Comprobado en código, no provocado un fallo de almacenamiento en el emulador. | Estado guardando/resultado; bloquear doble envío, regresar solo al persistir, informar fallos y distinguir guardado de programación efectiva de alarma. |
| Alta | El formulario prioriza tres campos y foto antes del horario. La única repetición editable es un entero en minutos; valor inicial 60. | Nombre y horario primero. Número + unidad explícita (horas/minutos), sin cálculos mentales. No presentar una frecuencia predeterminada como recomendación médica. |
| Media | Teclado QWERTY para intervalo, reproducido en emulador; el campo no especifica `KeyboardOptions`. | Teclado numérico y validación de límites; mantener error claro para vacío, cero y desbordamiento. [Configuración de campos Compose](https://developer.android.com/develop/ui/compose/text/user-input). |
| Media | El diálogo de hora usa la misma etiqueta «Guardar recordatorio» que el formulario. Para fin de rango, `DateTimeButton` también fija el título a «Primera toma». | «Confirmar hora» / «Confirm time» dentro del diálogo; título correspondiente a primera/última toma. Confirmar una hora no debe aparentar guardar todo. |
| Media | `DateTimeButton` fija `is24Hour = false`; español muestra formato 24 h en el resumen pero AM/PM en selector. | Respetar preferencia horaria del sistema. |
| Media | `onDismiss` navega directamente; no hay confirmación de cambios sin guardar en este flujo. | Advertir al salir con cambios o conservar borrador. Verificar Atrás del sistema y toolbar. |
| Media | Sonido, vibración y pantalla completa aparecen como tres opciones técnicas y están apagadas inicialmente. | Explicar qué aviso se recibirá; dar resumen «sin sonido ni vibración» cuando corresponda. Opciones avanzadas colapsables; no cambiar silenciosamente preferencias existentes. |
| Media | Gran espacio vacío entre nombre/dosis; foto tiene peso visual de botón primario. | Espaciado consistente; foto como acción secundaria y detalles opcionales detrás del horario. |
| Pendiente | Conversión de fecha UTC del DatePicker mediante Calendar local; requiere prueba de confirmación en zonas positivas/negativas y medianoche. | Añadir regresión de zona horaria antes de aprobar la exactitud de fechas. Es riesgo identificado en código, no fallo confirmado por las capturas. |

## Organización propuesta (sin cambiar aún la lógica ni el modelo)

Una pantalla, no un asistente de múltiples pasos obligatorio:

1. **Medicamento** — nombre obligatorio; dosis opcional con ejemplo de formato, sin sugerir una dosis.
2. **Horario** — primera toma con fecha/hora como filas editables claramente identificables; «Repetir cada [número] [unidad]». Ayuda: configurar conforme a las indicaciones recibidas.
3. **Duración** — un día / hasta una fecha / sin fecha de fin; último momento aparece solo si aplica.
4. **Resumen siempre comprensible** — repetición, inicio/fin y próximas tomas. Si hay tomas nocturnas, mostrarlas sin suprimirlas automáticamente.
5. **Cómo avisarte** — sonido y vibración con estado claro; pantalla completa/explicación de permisos bajo opciones avanzadas.
6. **Detalles opcionales** — nota y foto, acción secundaria.
7. **Guardar recordatorio** — estado guardando, confirmación real o error con reintento.

Un modo de «horas específicas del día» puede ser útil, pero es una funcionalidad futura distinta de «cada N horas». El modelo actual solo representa intervalos: no simular horas fijas mediante conversiones que cambien el significado del horario.

## Criterios de aprobación posteriores

- Crear un horario sin tener que convertir horas a minutos; entender su primera y próxima toma antes de guardar.
- Confirmar fecha/hora no guarda el formulario; cancelar conserva valores originales.
- Guardado fallido conserva entradas, informa el problema y permite reintentar; guardado exitoso no se anuncia antes de completar.
- Todo operable con fuente ampliada, teclado y lector de pantalla, sin perder etiquetas esenciales.
- Tareas con usuarios: crear recordatorio, revisar próximas tomas, cambiar intervalo, corregir error y salir sin guardar. Medir finalización, errores de horario y necesidad de ayuda; no afirmar una mejora demostrada solo por estética.

## Verificación automatizada ejecutada

13 pruebas unitarias existentes aprobadas: `ReminderScheduleTest` (5), `MedicationReminderCollisionDetectorTest` (3), `ReminderEditorNavigationTest` (1), `AppThemeTest` (4). Estas pruebas no comprueban usabilidad, entrega real de notificaciones ni todos los estados del formulario.

Pruebas de UI ampliadas: `tools/qa/Test-UiMatrix.ps1 -ExtendedOnly`; matriz de cuatro combinaciones, errores de nombre/intervalo, diálogos de inicio/fin, opciones de foto, teclados y fuente 1.3. No se realiza guardado válido, borrado, captura de cámara ni importación/exportación real. Los resultados de ejecución se detallan en `UI_MATRIX_2026-10-02.md`.
