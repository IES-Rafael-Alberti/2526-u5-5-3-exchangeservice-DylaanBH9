### 1) CE b) Se han definido casos de prueba

1. **Caso:** "throws an exception when the amount is negative".
    * **Clase de equivalencia:** Inválida (cantidad < 0).
    * **Condición evaluada:** Validación de la entrada (el dinero no puede ser negativo). Es representativo porque valida los límites y previene conversiones maliciosas antes de llegar al cálculo.
    * **Enlace al test:** [Test de cantidad negativa](https://github.com/IES-Rafael-Alberti/2526-u5-5-3-exchangeservice-DylaanBH9/blob/main/src/test/kotlin/ExchangeServiceDesignedBatteryTest.kt#L35-L39)

2. **Caso:** "devuelve la misma cantidad sin consultar al provider".
    * **Clase de equivalencia:** Válida (moneda de origen == moneda de destino).
    * **Condición evaluada:** Manejo de conversiones redundantes. Es representativo porque garantiza que no se hacen llamadas innecesarias al proveedor.
    * **Enlace al test:** [Test de monedas idénticas](https://github.com/IES-Rafael-Alberti/2526-u5-5-3-exchangeservice-DylaanBH9/blob/main/src/test/kotlin/ExchangeServiceDesignedBatteryTest.kt#L55-L63)

3. **Caso:** "intenta una segunda ruta si la primera falla".
    * **Clase de equivalencia:** Válida compleja (sin tasa directa, falla la primera conversión cruzada, funciona la segunda).
    * **Condición evaluada:** Lógica de búsqueda alternativa y resiliencia en `ExchangeService`. Es representativo porque prueba la robustez del servicio al recorrer todas las `supportedCurrencies`.
    * **Enlace al test:** [Test de múltiples rutas cruzadas](https://github.com/IES-Rafael-Alberti/2526-u5-5-3-exchangeservice-DylaanBH9/blob/main/src/test/kotlin/ExchangeServiceDesignedBatteryTest.kt#L108-L130)

---

### 2) CE f) Se han efectuado pruebas unitarias de clases y funciones

* **Test seleccionado:** "resuelve una conversión cruzada cuando no existe tasa directa".
* **Método que se prueba:** `ExchangeService.exchange()`.
* **Aislamiento:** Se aísla el acceso a datos inyectando `provider = mockk<ExchangeRateProvider>()`. Así nos desvinculamos de fallos en la clase real `InMemoryExchangeRateProvider`.
* **Entrada/Salida:** Entra `Money(10000, "JPY")` hacia `"GBP"`. Salida esperada calculada en base a los mocks predefinidos: `(10000 * 0.0067 * 0.79).toLong()`. Cumple con la definición de unidad porque solo evalúa la lógica del servicio aislada.
* **Enlace al test:** [Test de conversión cruzada aislada](https://github.com/IES-Rafael-Alberti/2526-u5-5-3-exchangeservice-DylaanBH9/blob/main/src/test/kotlin/ExchangeServiceDesignedBatteryTest.kt#L89-L106)

---

### 3) CE g) Se han implementado pruebas automáticas

* **Herramienta:** Utilizamos Kotest para estructurar la prueba y aserciones, y Gradle para la automatización.
* **Ejecución automática:** Se lanzan automáticamente mediante el comando `./gradlew test`. Gradle compila y ejecuta todas las clases sin intervención manual.
* **Evidencia:** Las aserciones (`shouldBe`, `shouldThrow`) actúan como verificadores automáticos. Si el código falla, se lanza un `AssertionError` parando la build.
* **Enlaces:**
    * [Configuración build.gradle.kts](https://github.com/IES-Rafael-Alberti/2526-u5-5-3-exchangeservice-DylaanBH9/blob/main/build.gradle.kts)

---

### 4) CE h) Se han documentado las incidencias detectadas

* **Incidencia observada:** Durante la creación de las pruebas, se observa que la implementación de `runCatching` en el cruce de `ExchangeService` silencia cualquier error en el `rateProvider`. Si el proveedor lanza un error grave (ej. pérdida de red), se silencia como si fuera simplemente un "par no encontrado".
* **Solución propuesta:** Debería capturarse únicamente `IllegalArgumentException` y dejar que el resto de excepciones críticas sigan su curso. Documentar esto asegura que el servicio no devuelva falsos negativos.
* **Enlace al test:** [Test en el fallo (lanza excepción general)](https://github.com/IES-Rafael-Alberti/2526-u5-5-3-exchangeservice-DylaanBH9/blob/main/src/test/kotlin/ExchangeServiceDesignedBatteryTest.kt#L132-L140)

---

### 5) CE i) Se han utilizado dobles de prueba para aislar los componentes durante las pruebas

* **Uso de Stub:** Usado para devolver rápidamente un `0.90` a la llamada "USDEUR" porque solo queríamos verificar el cálculo.
    * **Enlace:** [Test de Stub](https://github.com/IES-Rafael-Alberti/2526-u5-5-3-exchangeservice-DylaanBH9/blob/main/src/test/kotlin/ExchangeServiceDesignedBatteryTest.kt#L67-L75)
* **Uso de Spy:** Usado para verificar que la instancia real de `InMemoryExchangeRateProvider` recibe la llamada correcta, sin modificar su comportamiento.
    * **Enlace:** [Test de Spy](https://github.com/IES-Rafael-Alberti/2526-u5-5-3-exchangeservice-DylaanBH9/blob/main/src/test/kotlin/ExchangeServiceDesignedBatteryTest.kt#L78-L85)
* **Uso de Mock:** Usado para programar excepciones y valores múltiples, simulando fallos en rutas concretas y obligando al servicio a iterar.
    * **Enlace:** [Test de Mock](https://github.com/IES-Rafael-Alberti/2526-u5-5-3-exchangeservice-DylaanBH9/blob/main/src/test/kotlin/ExchangeServiceDesignedBatteryTest.kt#L108-L130)
* **Problema sin dobles:** Si usáramos siempre `InMemoryExchangeRateProvider`, tendríamos alto acoplamiento. Cualquier fallo en la implementación real de ese provider rompería los tests de `ExchangeService`, lo cual invalida el propósito del "testing unitario".