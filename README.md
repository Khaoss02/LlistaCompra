# 🛒 LlistaCompra - Lista de la Compra Compartida

Una aplicación móvil colaborativa para Android diseñada para simplificar la gestión y organización de las compras del hogar en tiempo real. Desarrollada con tecnologías modernas de Android, permite a los usuarios mantener un control estricto de sus listas personales y cooperar de forma instantánea con familiares, amigos o compañeros de piso.

El proyecto está disponible en su repositorio oficial: [GitHub - Khaoss02/LlistaCompra](https://github.com/Khaoss02/LlistaCompra).

---

## ✨ Características Principales

### 👥 Gestión de Listas: Personales y Compartidas
* **Organización Multilista:** Crea, edita y elimina múltiples listas de la compra de forma independiente desde una interfaz limpia.
* **Colaboración en Tiempo Real:** Comparte cualquier lista mediante un código identificador único (ID de documento) para que otros usuarios visualicen y editen los cambios al instante.
* **Estructura tipo Cloud (Gmail/Drive):** Clasificación automática e intuitiva de los paneles de navegación entre tus listas propias (creadas por ti) y las listas compartidas contigo.
* **Organización Visual:** Soporte nativo para reordenación de listas prioritarias en la pantalla principal mediante gestos **Drag & Drop** (arrastrar y soltar).

### 🗂️ Categorización Dinámica e Inteligente
* **Estructura por Categorías:** Clasifica tus productos de manera personalizada para optimizar el recorrido en el supermercado (ej: *Lácteos, Frutas, Limpieza, Carnes, Bebidas*).
* **Gestión CRUD Completa:** Crea, renombra o elimina categorías completas de acuerdo a tus necesidades de compra.
* **Interfaz Dinámica:** Secciones totalmente expandibles y colapsables (`expand/collapse`) por categoría para una navegación cómoda cuando las listas son extensas.

### 🍏 Control de Productos y Metadatos Avanzados
* **Ciclo de Vida del Producto:** Añade, edita los detalles o elimina artículos en cada sección de manera fluida.
* **Check Completo sin Borrado:** Marca productos como completados (comprados). La interfaz aplica un tachado visual inmediato y permite filtrar la pantalla para ocultar o mostrar los elementos ya adquiridos.
* **Contador Visual:** Indicador en tiempo real con el número exacto de productos pendientes en cada categoría y lista.
* **Metadatos Detallados:** Posibilidad de asociar información extra crítica a cada artículo:
  * Tienda o supermercado de preferencia (ej: *Mercadona, Lidl, Carrefour*).
  * Etiquetas personalizadas.
  * Precio estimado o unitario.

---

## 🛠️ Stack Tecnológico y Arquitectura

* **Lenguaje:** [Kotlin](https://kotlinlang.org/) — 100% moderno, seguro y conciso.
* **Interfaz de Usuario:** [Jetpack Compose](https://developer.android.com/jetpack/compose) y componentes avanzados de Material Design 3 para una UI reactiva, moderna y adaptada a las directrices de diseño actuales.
* **Arquitectura:** Patrón arquitectónico limpio enfocado en la separación de responsabilidades, flujos de datos asíncronos y desacoplamiento de componentes.
* **Base de Datos y Sincronización:** [Firebase Firestore](https://firebase.google.com/docs/firestore) — Base de datos NoSQL orientada a documentos que gestiona la persistencia local y la sincronización remota bidireccional inmediata.
* **Autenticación:** [Firebase Anonymous Auth](https://firebase.google.com/docs/auth/android/anonymous-auth) — Acceso inmediato sin fricciones ni registros obligatorios para agilizar la experiencia de usuario inicial.
* **Componentes de Rendimiento:** Uso eficiente de `DiffUtil` y estructuras optimizadas para actualizaciones reactivas de la UI sin redibujados innecesarios.

---

## 🚀 Instalación y Configuración

Sigue estos pasos para clonar el repositorio y vincular tu propia instancia de Firebase para desplegar la aplicación:

### 1. Clonar el repositorio
Abre una terminal en tu equipo y ejecuta el siguiente comando:
```bash
git clone [https://github.com/Khaoss02/LlistaCompra.git](https://github.com/Khaoss02/LlistaCompra.git)
