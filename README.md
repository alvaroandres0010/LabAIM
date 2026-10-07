# LabAIM

Entrenador de punteria en Java con telemetria abierta.

La idea no es clonar Aimlabs. Los escenarios de Aimlabs son gratis; lo que esta detras
del muro de pago es el analisis: overshoot de flicks, eficiencia de trayecto, sesgo
direccional, buscador de sensibilidad. Nada de eso es magia, es estadistica sobre la
trayectoria de la mira. Pero Aimlabs no exporta esa trayectoria, asi que hay que
generarla uno mismo. De ahi este proyecto.

## Estado

| Fase | Que | Estado |
|---|---|---|
| 0 | Proyecto Maven, importable en Eclipse | hecho |
| 1 | Camara, sensibilidad calibrada, proyeccion perspectiva | hecho |
| 2 | Gridshot jugable con score y temporizador | hecho |
| 3 | Telemetria por frame a CSV | hecho |
| 4 | Analizador de metricas | pendiente |
| 5 | Mas escenarios: tracking, switching, microshots | pendiente |
| 6 | Historial y graficos de progreso | pendiente |

## Abrirlo en Eclipse

1. `File > Import... > Maven > Existing Maven Projects`
2. Apunta a la carpeta del repo y acepta.
3. Eclipse descarga libGDX solo. La primera vez tarda un poco.

Si Eclipse se queja del nivel de compilador: el proyecto usa Java 17. Ve a
`Project > Properties > Java Compiler` y asegurate de que haya un JDK 17 o superior.

## Ejecutarlo

Desde Eclipse: clic derecho en `DesktopLauncher.java` > `Run As > Java Application`.

Desde la terminal:

```
mvn exec:java
```

O generando un jar que corre solo:

```
mvn package
java -jar target/labaim-0.1.0-SNAPSHOT.jar
```

Controles: `ESPACIO` empieza, clic izquierdo dispara, `R` repite, `ESC` sale.

## Configuracion

La primera ejecucion crea `labaim.properties` al lado del jar. Editalo y vuelve a
lanzar:

```properties
perfil=VALORANT          # VALORANT, CS2, OVERWATCH2 o RAW
sens=0.643               # la misma sensibilidad que tienes en el juego
dpi=800                  # el DPI real del raton
fovHorizontal=103        # 103 para Valorant, 90 para CS2
duracionSegundos=60
radioObjetivoGrados=1.1  # mas chico = mas dificil
separacionGrados=4.5
columnas=5
filas=5
objetivosSimultaneos=3
pantallaCompleta=false
escalaCounts=1.0         # dejar en 1.0; ver "la prueba de la regla"
```

## La prueba de la regla

Esto es lo que separa un entrenador util de un juego de clicar circulos. Si la
sensibilidad no es identica a la del juego objetivo, entrenas un movimiento que
despues no sirve.

1. Lanza LabAIM. En la pantalla inicial aparece tu `cm/360` calculado.
2. Pon una regla en el mousepad. Marca el punto de partida del raton.
3. Dentro del juego, gira la vista hasta completar una vuelta entera.
4. Mide cuanto se movio el raton.

Debe coincidir con el `cm/360` que muestra la pantalla. Comprobacion de referencia:
perfil Valorant, sens 1, 800 DPI da 16.33 cm/360, que es exactamente lo que reporta
Aimlabs para esa configuracion.

Si no cuadra, el sospechoso numero uno es el escalado de pantalla de Windows. El
factor `escalaCounts` lo compensa: si giraste de mas, bajalo proporcionalmente.

## Arquitectura

```
cl.labaim.core      Java puro, sin libGDX. Toda la logica y toda la matematica.
  math/             Vec3, Angles, Sensitivity, Projection, HitTest
  model/            Camera, Target, Viewport
  scenario/         Scenario, GridShot, ScenarioConfig
  session/          Session, SessionStats
  telemetry/        FrameBuffer, GameEvent, TelemetryRecorder
cl.labaim.app       libGDX. Solo traduce input a camara y estado a pixeles.
```

El nucleo no importa nada de libGDX a proposito. Por eso los tests pueden simular una
ronda de 60 segundos completa, con 14.400 frames, en milisegundos y sin abrir ninguna
ventana.

Decisiones que vale la pena conocer antes de tocar el codigo:

- **Todo en `double`, nunca `float`.** El yaw se actualiza decenas de miles de veces
  por sesion. Con `float` el error acumulado descalibra la sensibilidad. Hay un test
  que hace 100.000 updates de ida y vuelta y exige volver a cero.
- **El impacto se decide por angulos, no por pixeles.** Comparar la mira contra el
  circulo dibujado arrastraria el error de la proyeccion y dependeria de la resolucion.
  El test angular es exacto y de paso entrega el error del disparo, que es el dato que
  alimenta todo el analisis.
- **El angulo se calcula con `atan2`, no con `acos`.** Para los angulos diminutos que
  medimos al analizar punteria, `acos` pierde la mitad de los digitos significativos.
- **La proyeccion es perspectiva de verdad.** El desplazamiento en pantalla va con la
  tangente del angulo. Un flick de 10 grados no recorre el doble de pixeles que uno de
  5: recorre 2.015 veces mas. Si fuera lineal, el entrenamiento no transferiria.
- **Nada de I/O durante la partida.** La telemetria se acumula en arreglos de
  primitivos preasignados y se escribe al terminar la ronda.

## Telemetria

Cada ronda crea `sessions/<fecha-hora>/` con dos archivos.

`frames.csv`, una fila por frame:

```
t_ns, yaw_deg, pitch_deg, nearest_target_id
```

`events.csv`, una fila por suceso:

```
t_ns, tipo, target_id, target_yaw, target_pitch, cam_yaw, cam_pitch, error_angular_deg
```

donde `tipo` es `SESSION_START`, `SPAWN`, `SHOT_HIT`, `SHOT_MISS` o `SESSION_END`.

Cruzando ambos salen las metricas de la fase 4: tiempo de reaccion, overshoot de
flick, eficiencia de trayecto, numero de correcciones y sesgo direccional.

## Tests

```
mvn test
```

Son 40 y pico de aserciones sobre la matematica y la logica de juego. Los numeros
esperados de sensibilidad salen de capturas reales de Aimlabs, no de nuestra propia
formula: si LabAIM se descalibra respecto al juego objetivo, esos tests fallan.

## Nota sobre la version de libGDX

El `pom.xml` fija libGDX 1.13.1. Si tu entorno no la resuelve, baja a 1.12.1 cambiando
la propiedad `gdx.version`.

`RawMouse.java` es la unica clase que toca GLFW directamente, para activar la entrada
cruda del raton. Si una futura version de libGDX cambia esa API, es el unico archivo
que hay que ajustar.
