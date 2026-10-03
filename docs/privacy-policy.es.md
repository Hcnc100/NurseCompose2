# Política de privacidad de NurseApp — BORRADOR

**Preparado el 3 de octubre de 2026. No publicar todavía:** confirmar la retención y configuración de Firebase, la audiencia objetivo y las declaraciones de Play Console. La fecha de entrada en vigor será la fecha de publicación aprobada, no la de este borrador. Este texto describe la implementación revisada; no promete controles que la aplicación aún no tiene.

## Responsable y contacto

NurseApp es una aplicación de Ricardo Pájaro Coatl, publicada en Google Play como NullPointerDev. Para consultas de privacidad: nullpointerdeveloper@gmail.com.

## Información que introduces

La aplicación guarda en su almacenamiento local las mediciones de glucosa, presión arterial, temperatura y oxígeno en sangre que introduces, con sus fechas. También guarda nombres de medicamentos, dosis, comentarios, horarios, duración de recordatorios, referencias a fotos y respuestas registradas sobre las tomas. El historial puede incluir correcciones y actividad de alarmas.

Estos datos se utilizan para mostrar registros y gráficas, organizar recordatorios, presentar el historial y generar las exportaciones que solicites. Marcar una toma no permite comprobar que hayas ingerido el medicamento.

## Fotos

Puedes seleccionar una imagen mediante el selector del sistema o abrir una aplicación de cámara para tomar una foto. NurseApp accede a la imagen seleccionada; no necesita acceso general a todas tus fotos. Las fotos tomadas para recordatorios se guardan en la caché de la aplicación y el sistema puede eliminarlas. Las imágenes originales de la galería se conservan fuera de NurseApp.

## Datos técnicos y servicios de Google

La aplicación incluye Firebase Analytics y Firebase Crashlytics, servicios de Google utilizados para analizar el uso y diagnosticar fallos. Estos servicios pueden enviar información técnica fuera del dispositivo, como eventos de uso, información de la aplicación y del dispositivo, identificadores de instalación e informes de errores. El tratamiento concreto depende de las versiones y la configuración de los servicios.

Los informes de errores pueden incluir contexto técnico, como el tipo de medición implicado y mensajes de excepción. Por ello no afirmamos que toda la información tratada por la aplicación permanezca exclusivamente en el dispositivo. La aplicación revisada no ofrece un interruptor para desactivar estos servicios.

Consulta [privacidad de Firebase](https://firebase.google.com/support/privacy) y [privacidad de Google](https://policies.google.com/privacy). No se ha identificado en esta revisión un envío dedicado de los valores de mediciones o de las fotos a un servidor propio del desarrollador. Esto no equivale a garantizar que ningún informe de error pueda contener contexto sensible.

**Pendiente antes de publicar:** documentar la retención efectiva configurada en Analytics y Crashlytics, las opciones de eliminación disponibles y verificar el contenido real de los informes enviados por la versión de lanzamiento. Incorporar aquí los plazos y el procedimiento comprobados; no inventar un plazo ni prometer una eliminación remota no implementada.

## Copias de seguridad de Android

La versión revisada permite las copias de seguridad del sistema Android y no excluye expresamente la base de datos. Según la configuración del dispositivo y el sistema, los datos locales pueden formar parte de una copia en la cuenta del usuario o de una transferencia entre dispositivos. Estas operaciones dependen de Android y del proveedor de copia de seguridad. No debe interpretarse «almacenamiento local» como ausencia de toda copia fuera del dispositivo.

## Exportaciones y soporte

Los PDF, archivos CSV y reportes de diagnóstico se generan cuando los solicitas. Un reporte de diagnóstico puede incluir nombres de medicamentos, actividad de alarmas y trazas técnicas. Tú decides dónde guardar o con quién compartir estos archivos mediante las opciones del sistema. Los destinatarios y aplicaciones que elijas tratarán la información conforme a sus propias políticas. No compartas reportes con personas en las que no confíes.

## Permisos y funcionamiento

Las notificaciones permiten mostrar recordatorios. El acceso a alarmas exactas y a pantalla completa ayuda a presentar las alarmas cuando Android lo permite. La vibración, el servicio de reproducción y el permiso de reactivación apoyan el aviso sonoro y vibratorio. La recepción del reinicio permite reprogramar recordatorios. El acceso a Internet permite funcionar a los servicios técnicos descritos. Los registros y recordatorios pueden utilizarse sin conexión, pero la aplicación no es exclusivamente desconectada.

Puedes gestionar los permisos desde Android. Su denegación o las restricciones de batería pueden impedir o limitar los avisos. No hay garantía de entrega de todas las alarmas.

## Conservación y eliminación

Los registros locales se conservan mientras permanezcan en el almacenamiento de la aplicación. Puedes eliminar mediciones desde sus controles y eliminar recordatorios individualmente. La opción para eliminar todas las mediciones no elimina necesariamente recordatorios ni todo el historial técnico o de tomas.

Borrar el almacenamiento de la aplicación o desinstalarla elimina sus datos locales privados. Esto no elimina automáticamente archivos exportados, fotos originales de la galería, datos ya enviados a servicios técnicos ni posibles copias del sistema. Gestiona esos archivos y copias desde sus ubicaciones o servicios correspondientes.

NurseApp no requiere crear una cuenta propia. Para preguntas sobre acceso o eliminación de información tratada por el desarrollador, escribe al correo de contacto. No envíes datos médicos innecesarios. La respuesta y las medidas disponibles dependerán de la información que pueda identificarse y de las obligaciones aplicables; no prometemos vincular automáticamente una instalación anónima con una persona.

## Seguridad

El almacenamiento privado está sujeto a las protecciones del sistema Android. No se declara que la base de datos local tenga cifrado adicional propio. La seguridad del dispositivo, las copias de seguridad y los archivos compartidos también depende de tu configuración y de los servicios utilizados. Ningún sistema permite garantizar seguridad absoluta.

## Audiencia

**Pendiente antes de publicar:** confirmar la audiencia real en Play Console. La política pública anterior indicaba que la aplicación no se dirige a menores de 13 años, mientras que la ficha muestra un compromiso con la política de Familias. La clasificación «Para todos» no define por sí sola la audiencia. El texto definitivo y el tratamiento de datos de menores deben corresponder a la audiencia y configuración efectivas.

## Uso de salud y cambios de política

NurseApp no es un dispositivo médico y no diagnostica, trata, cura ni previene ninguna enfermedad o condición médica. Consulta a un profesional de la salud para decisiones sobre tus valores y tratamientos.

Las actualizaciones de esta política se publicarán en su URL pública con su fecha de entrada en vigor. Los cambios relevantes en el tratamiento de datos deberán reflejarse también en la aplicación y en las declaraciones de Google Play.
