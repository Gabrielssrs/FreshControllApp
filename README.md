Quiero darte contexto completo de mi proyecto antes de empezar a trabajar juntos. Por ahora no necesito que hagas ningún cambio ni petición — solo que entiendas el estado actual, para que cuando te pida algo más adelante ya tengas el panorama completo.

PROYECTO: FreshControl App

App Android (Kotlin) para gestión de inventario y ventas de una bodega/minimarket, desarrollada en equipo con metodología SCRUM para un curso universitario. Usa Clean Architecture con estas capas: domain, data, di, presentation. Stack: MVVM, Hilt (inyección de dependencias), Room (base de datos local), Firebase (Firestore, Auth) como backend con sincronización offline-first, y navegación con Jetpack Navigation Component.

Mi módulo específico dentro del equipo es: registro de productos mediante escaneo de código de barras, usando CameraX + ML Kit para la lectura, con la idea de autocompletar los datos del producto (nombre, marca, categoría, imagen) desde la API pública de Open Food Facts cuando el producto no exista todavía en la base de datos local.

ESTADO ACTUAL DEL PROYECTO (según mapeo previo realizado con otra IA):
- Domain: 31 archivos .kt — 8 modelos de dominio (Product, Sale, User, Store, AuditLog, SaleDetail, StockMovement, CashRegisterClose), 6 interfaces de repositorio, 20 Use Cases.
- Data: 32 archivos .kt — repositorios implementados con lógica offline-first (Room como fuente primaria, Firestore como espejo de sincronización), 7 DAOs, mappers Entity<->Domain. NO existía Retrofit ni ninguna integración de API externa hasta este momento.
- DI: 3 módulos Hilt (DatabaseModule, NetworkModule, RepositoryModule) — sin proveedores de cliente HTTP todavía.
- Presentation: 43 archivos .kt distribuidos en auth, home, inventory, profile, sales. BarcodeScannerFragment ya tiene CameraX integrado con PreviewView y analizador conectado a ML Kit; BarcodeScannerViewModel solo buscaba productos en Room local, sin fallback a ninguna API externa.
- Navegación: root_nav_graph.xml con auth_nav_graph.xml (5 destinos) y main_nav_graph.xml (15+ destinos).
- Testing: prácticamente inexistente (solo plantillas por defecto de Android Studio).

TRABAJO YA DISEÑADO (con otra IA, en formato de código, PENDIENTE DE VERIFICAR e INTEGRAR contra el código real del proyecto, no confirmado todavía que encaje):
Se diseñó la integración completa de Open Food Facts:
1. Dependencias Retrofit 2.9.0 + Moshi + OkHttp con logging interceptor (solo debug).
2. data/remote/dto/OpenFoodFactsResponseDto.kt — DTOs (OpenFoodFactsResponseDto, OpenFoodFactsProductDto) mapeando status, product.code, product_name, brands, categories, image_front_url, quantity.
3. data/remote/OpenFoodFactsApiService.kt — interfaz Retrofit con @GET("api/v2/product/{barcode}.json").
4. di/NetworkModule.kt — provee Moshi, OkHttpClient, Retrofit (baseUrl https://world.openfoodfacts.org/), y OpenFoodFactsApiService.
5. data/mapper/ProductMapper.kt — mappers ProductEntity<->Product (domain), más OpenFoodFactsProductDto.toDomainProduct(storeId) que rellena con valores por defecto los campos de negocio que la API no provee (sku, currentStock, minStock, price, etc.).
6. domain/repository/ProductRepository.kt — interfaz ampliada con un método nuevo findByBarcodeLocalOrRemote(storeId, barcode): BarcodeLookupResult, manteniendo también el método viejo getProductByBarcode(storeId, barcode) para búsquedas estrictamente locales (usado en el módulo de ventas). BarcodeLookupResult es un sealed class con EncontradoLocal, EncontradoEnApi, NoEncontrado, Error.
7. data/repository/ProductRepositoryImpl.kt — implementa la lógica: busca primero en Room vía ProductDao, si no encuentra, consulta OpenFoodFactsApiService y mapea el resultado.
8. domain/usecase/inventory/LookupProductByBarcodeUseCase.kt — Use Case limpio, solo depende de ProductRepository (sin conocer Retrofit ni DTOs, respetando la regla de dependencia de Clean Architecture).
9. Gestión de sesión/storeId: se propuso una interfaz domain/repository/SessionManager.kt + implementación con DataStore Preferences (SessionManagerImpl + DataStoreModule), PERO ESTO ESTÁ SIN CONFIRMAR contra el proyecto real — es posible que ya exista un mecanismo de sesión vía AuthRepository/FirebaseAuthService que debería usarse en su lugar, evitando duplicar la fuente de verdad del storeId.
10. presentation/barcode/BarcodeScannerViewModel.kt — actualizado para usar storeId + barcode en el Use Case, con un ScannerUiState (Idle, Loading, NavigateToEditExisting, NavigateToCreateNew, ShowError).

Por ahora solo quiero que explores el código fuente real del proyecto (carpeta app/src/main/java/com/example/freshcontroll/) para que tengas este contexto internalizado junto con la estructura real de archivos, nombres de clases, métodos y convenciones que ya existen. No apliques cambios ni me generes código todavía — yo te iré guiando en los próximos mensajes sobre cómo avanzar.