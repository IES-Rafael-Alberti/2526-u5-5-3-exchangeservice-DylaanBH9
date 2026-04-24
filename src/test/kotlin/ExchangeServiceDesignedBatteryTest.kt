package com.example.exchange

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifySequence
import org.iesra.revilofe.ExchangeRateProvider
import org.iesra.revilofe.ExchangeService
import org.iesra.revilofe.InMemoryExchangeRateProvider
import org.iesra.revilofe.Money

class ExchangeServiceDesignedBatteryTest : DescribeSpec({

    afterTest {
        clearAllMocks()
    }

    describe("battery designed from equivalence classes for ExchangeService") {

        describe("input validation (Clases inválidas)") {
            val provider = mockk<ExchangeRateProvider>()
            val service = ExchangeService(provider)

            it("throws an exception when the amount is zero") {
                shouldThrow<IllegalArgumentException> {
                    service.exchange(Money(0, "USD"), "EUR")
                }
            }

            it("throws an exception when the amount is negative") {
                shouldThrow<IllegalArgumentException> {
                    service.exchange(Money(-100, "USD"), "EUR")
                }
            }

            it("throws an exception when the source currency code is invalid") {
                shouldThrow<IllegalArgumentException> {
                    service.exchange(Money(100, "US"), "EUR") // Longitud distinta de 3
                }
            }

            it("throws an exception when the target currency code is invalid") {
                shouldThrow<IllegalArgumentException> {
                    service.exchange(Money(100, "USD"), "EURO") // Longitud distinta de 3
                }
            }
        }

        describe("Monedas origen y destino idénticas") {
            it("devuelve la misma cantidad sin consultar al provider") {
                val provider = mockk<ExchangeRateProvider>()
                val service = ExchangeService(provider)

                val result = service.exchange(Money(100, "USD"), "USD")
                result shouldBe 100

                // Verificamos que no se ha hecho ninguna llamada
                verify { provider wasNot Called }
            }
        }

        describe("Uso de STUB para conversión directa") {
            it("convierte correctamente usando una tasa directa fija") {
                val provider = mockk<ExchangeRateProvider>()
                // Configuramos un stub simple
                every { provider.rate("USDEUR") } returns 0.90
                val service = ExchangeService(provider)

                val result = service.exchange(Money(1000, "USD"), "EUR")
                result shouldBe 900 // 1000 * 0.90
            }
        }

        describe("Uso de SPY para verificar una llamada real") {
            it("usa InMemoryExchangeRateProvider real y verifica la interacción") {
                val realProvider = InMemoryExchangeRateProvider(mapOf("USDEUR" to 0.92))
                val providerSpy = spyk(realProvider)
                val service = ExchangeService(providerSpy)

                val result = service.exchange(Money(1000, "USD"), "EUR")
                result shouldBe 920

                // Comprobamos la interacción exacta
                verify(exactly = 1) { providerSpy.rate("USDEUR") }
            }
        }

        describe("Uso de MOCK para conversiones cruzadas") {
            it("resuelve una conversión cruzada cuando no existe tasa directa") {
                val provider = mockk<ExchangeRateProvider>()
                
                every { provider.rate("JPYGBP") } throws IllegalArgumentException() // Directa falla
                every { provider.rate("JPYUSD") } returns 0.0067
                every { provider.rate("USDGBP") } returns 0.79

                // Usamos USD como intermedia
                val service = ExchangeService(provider, setOf("USD"))

                val result = service.exchange(Money(10000, "JPY"), "GBP")
                result shouldBe (10000 * 0.0067 * 0.79).toLong()

                verifySequence {
                    provider.rate("JPYGBP")
                    provider.rate("JPYUSD")
                    provider.rate("USDGBP")
                }
            }

            it("intenta una segunda ruta si la primera falla") {
                val provider = mockk<ExchangeRateProvider>()
                
                every { provider.rate("USDEUR") } throws IllegalArgumentException() // Directa falla
                
                // Ruta 1: vía GBP (falla en el segundo tramo)
                every { provider.rate("USDGBP") } returns 0.79
                every { provider.rate("GBPEUR") } throws IllegalArgumentException()
                
                // Ruta 2: vía JPY (funciona)
                every { provider.rate("USDJPY") } returns 150.0
                every { provider.rate("JPYEUR") } returns 0.006

                // Forzamos el orden de las monedas soportadas usando LinkedHashSet
                val service = ExchangeService(provider, linkedSetOf("GBP", "JPY"))

                val result = service.exchange(Money(100, "USD"), "EUR")
                result shouldBe (100 * 150.0 * 0.006).toLong()

                verifySequence {
                    provider.rate("USDEUR") // Directa
                    provider.rate("USDGBP") // Int 1 Tramo 1
                    provider.rate("GBPEUR") // Int 1 Tramo 2 (Falla)
                    provider.rate("USDJPY") // Int 2 Tramo 1
                    provider.rate("JPYEUR") // Int 2 Tramo 2 (Éxito)
                }
            }

            it("lanza excepción si no existe ninguna ruta válida") {
                val provider = mockk<ExchangeRateProvider>()
                every { provider.rate(any()) } throws IllegalArgumentException() // Todo falla

                val service = ExchangeService(provider, setOf("GBP"))

                shouldThrow<IllegalArgumentException> {
                    service.exchange(Money(100, "USD"), "EUR")
                }
            }
        }
    }
})package com.example.exchange

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifySequence
import org.iesra.revilofe.ExchangeRateProvider
import org.iesra.revilofe.ExchangeService
import org.iesra.revilofe.InMemoryExchangeRateProvider
import org.iesra.revilofe.Money

class ExchangeServiceDesignedBatteryTest : DescribeSpec({

    afterTest {
        clearAllMocks()
    }

    describe("battery designed from equivalence classes for ExchangeService") {

        describe("input validation") {
            val provider = mockk<ExchangeRateProvider>()
            val service = ExchangeService(provider)

            it("throws an exception when the amount is zero") {
                shouldThrow<IllegalArgumentException> {
                    service.exchange(Money(0, "USD"), "EUR")
                }
            }

            it("throws an exception when the amount is negative") {
            }

            it("throws an exception when the source currency code is invalid") {
            }

            it("throws an exception when the target currency code is invalid") {

            }
        }

       //..
}})
