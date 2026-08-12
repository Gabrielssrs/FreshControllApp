# Mejoras en el Resumen de Venta (fragment_sale_receipt.xml)

Este plan detalla la implementación de nuevos botones en la pantalla de recibo para mejorar el flujo del usuario, incluyendo opciones para finalizar (confirmar), anular (cancelar) y realizar una nueva venta, con diálogos de doble confirmación para acciones críticas.

## User Review Required

> [!IMPORTANT]
> - **Lógica de Anulación**: Al "Cancelar Venta" en esta pantalla, se eliminará el registro de la base de datos local y de Firestore.
> - **Doble Confirmación**: Se implementarán diálogos de confirmación para evitar cierres o anulaciones accidentales.

## Proposed Changes

### [Data Layer]

#### [MODIFY] [SaleDao.kt](file:///C:/Users/ramir/AndroidStudioProjects/FreshControll/app/src/main/java/com/example/freshcontroll/data/local/dao/SaleDao.kt)
- Añadir métodos para eliminar una venta y sus detalles por ID.

#### [MODIFY] [SaleRepository.kt](file:///C:/Users/ramir/AndroidStudioProjects/FreshControll/app/src/main/java/com/example/freshcontroll/domain/repository/SaleRepository.kt)
- Añadir `deleteSale(saleId: String): Result<Unit>` a la interfaz.

#### [MODIFY] [SaleRepositoryImpl.kt](file:///C:/Users/ramir/AndroidStudioProjects/FreshControll/app/src/main/java/com/example/freshcontroll/data/repository/SaleRepositoryImpl.kt)
- Implementar `deleteSale` eliminando datos de Room y Firestore.

---

### [Presentation Layer]

#### [MODIFY] [SaleReceiptViewModel.kt](file:///C:/Users/ramir/AndroidStudioProjects/FreshControll/app/src/main/java/com/example/freshcontroll/presentation/sales/SaleReceiptViewModel.kt)
- Añadir función `voidSale()` para ejecutar la eliminación.
- Añadir un `SharedFlow` para notificar éxito o error de la anulación.

#### [MODIFY] [fragment_sale_receipt.xml](file:///C:/Users/ramir/AndroidStudioProjects/FreshControll/app/src/main/res/layout/fragment_sale_receipt.xml)
- Reorganizar botones:
  - **Nueva Venta** (Botón principal verde).
  - **Finalizar** (Botón secundario morado).
  - **Ver Recibo** (Botón con icono).
  - **Anular Venta** (Botón de texto rojo).

#### [MODIFY] [SaleReceiptFragment.kt](file:///C:/Users/ramir/AndroidStudioProjects/FreshControll/app/src/main/java/com/example/freshcontroll/presentation/sales/SaleReceiptFragment.kt)
- Implementar `MaterialAlertDialogBuilder` para la doble confirmación de "Finalizar" y "Anular".
- Implementar lógica para "Ver Recibo" (Compartir resumen de venta).
- Gestionar navegación al Inicio (`HomeFragment`).

---

## Verification Plan

### Automated Tests
- No se incluyen en este paso, pero se verificará mediante compilación.

### Manual Verification
- Realizar una venta y llegar a la pantalla de resumen.
- Probar el botón **Nueva Venta** (debe ir a NewSaleFragment).
- Probar el botón **Finalizar** con sus dos diálogos (debe ir a HomeFragment).
- Probar el botón **Anular Venta** con sus dos diálogos (debe borrar la venta y volver a HomeFragment).
- Probar **Ver Recibo** (debe abrir el menú de compartir de Android).
