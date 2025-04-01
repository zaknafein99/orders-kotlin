/**
 * Translations for the Orders Management System
 */
export const messages = {
  en: {
    // App header and general
    appTitle: 'Orders Management',
    allRightsReserved: 'All rights reserved',
    ordersManagementSystem: 'Orders Management System',
    loading: 'Loading...',
    refresh: 'Refresh',
    login: 'Login',
    logout: 'Logout',
    dashboard: 'Dashboard',
    customers: 'Customers',
    orders: 'Orders',
    inventory: 'Inventory',
    error: 'Error',
    success: 'Success',
    // Orders
    createOrder: 'Create Order',
    orderNumber: 'Order Number',
    customer: 'Customer',
    status: 'Status',
    total: 'Total',
    createdAt: 'Created At',
    actions: 'Actions',
    noOrders: 'No orders found',
    cancelOrder: 'Cancel Order',
    // New Order
    newOrder: 'New Order',
    selectCustomer: 'Select a customer',
    items: 'Items',
    addItem: 'Add Item',
    item: 'Item',
    quantity: 'Quantity',
    price: 'Price',
    subtotal: 'Subtotal',
    noItems: 'No items added',
    orderSummary: 'Order Summary',
    tax: 'Tax',
    cancel: 'Cancel',
    // Items Management
    itemsManagement: 'Items Management',
    newItem: 'New Item',
    editItem: 'Edit Item',
    deleteItem: 'Delete Item',
    confirmDelete: 'Confirm Delete',
    itemName: 'Name',
    itemPrice: 'Price',
    itemCategory: 'Category',
    itemDescription: 'Description',
    itemStock: 'Stock',
    itemCreated: 'Item created successfully',
    itemUpdated: 'Item updated successfully',
    itemDeleted: 'Item deleted successfully',
    noItemsAvailable: 'No items available',
    // Profile
    name: 'Name',
    currentPassword: 'Current Password',
    newPassword: 'New Password',
    confirmPassword: 'Confirm Password',
    saveChanges: 'Save Changes',
    reset: 'Reset',
    // 404 Page
    pageNotFound: 'Page Not Found',
    pageDoesNotExist: 'The page you are looking for does not exist.',
    backToHome: 'Back to Home'
  },
  es: {
    // App header and general
    appTitle: 'Gestión de Pedidos',
    allRightsReserved: 'Todos los derechos reservados',
    ordersManagementSystem: 'Sistema de Gestión de Pedidos',
    loading: 'Cargando...',
    refresh: 'Actualizar',
    login: 'Iniciar sesión',
    logout: 'Cerrar sesión',
    dashboard: 'Panel de control',
    customers: 'Clientes',
    orders: 'Pedidos',
    error: 'Error',
    success: 'Éxito',
    // Orders
    createOrder: 'Crear Pedido',
    orderNumber: 'Número de Pedido',
    customer: 'Cliente',
    status: 'Estado',
    total: 'Total',
    createdAt: 'Fecha de Creación',
    actions: 'Acciones',
    noOrders: 'No se encontraron pedidos',
    cancelOrder: 'Cancelar Pedido',
    // New Order
    newOrder: 'Nuevo Pedido',
    selectCustomer: 'Seleccionar cliente',
    items: 'Artículos',
    addItem: 'Agregar Artículo',
    item: 'Artículo',
    quantity: 'Cantidad',
    price: 'Precio',
    subtotal: 'Subtotal',
    noItems: 'No hay artículos agregados',
    orderSummary: 'Resumen del Pedido',
    tax: 'Impuesto',
    cancel: 'Cancelar',
    save: 'Guardar',
    delete: 'Eliminar',
    edit: 'Editar',
    view: 'Ver',
    back: 'Volver',

    // Authentication
    login: 'Iniciar Sesión',
    loggingIn: 'Iniciando Sesión...',
    email: 'Correo Electrónico',
    password: 'Contraseña',
    loginToContinue: 'Inicia sesión para continuar',
    loginFailed: 'Error al iniciar sesión',
    authRequired: 'Autenticación requerida',
    logout: 'Cerrar Sesión',
    profile: 'Perfil',
    // Profile
    name: 'Nombre',
    currentPassword: 'Contraseña Actual',
    newPassword: 'Nueva Contraseña',
    confirmPassword: 'Confirmar Contraseña',
    saveChanges: 'Guardar Cambios',
    reset: 'Restablecer',
    // 404 Page
    pageNotFound: 'Página No Encontrada',
    pageDoesNotExist: 'La página que estás buscando no existe.',
    backToHome: 'Volver al Inicio',

    // Dashboard
    dashboard: 'Panel de Control',
    statistics: 'Estadísticas',
    recentOrders: 'Pedidos Recientes',
    ordersTrend: 'Tendencia de Pedidos',
    totalSales: 'Ventas Totales',
    averageOrderValue: 'Valor Promedio',
    topCustomers: 'Mejores Clientes',
    ordersByStatus: 'Pedidos por Estado',
    
    // Customer search
    searchCustomer: 'Buscar Cliente',
    searchByPhoneNumber: 'Buscar por número de teléfono',
    search: 'Buscar',
    searching: 'Buscando...',
    customerNotFound: 'Cliente no encontrado',
    errorSearchingCustomer: 'Error al buscar cliente',
    addNewCustomer: 'Añadir Nuevo Cliente',
    
    // Customer details
    customerDetails: 'Detalles del Cliente',
    name: 'Nombre',
    phoneNumber: 'Teléfono',
    address: 'Dirección',
    customerType: 'Tipo de Cliente',
    customerState: 'Estado',
    active: 'Activo',
    inactive: 'Inactivo',
    
    // Orders
    createOrder: 'Crear Pedido',
    newOrder: 'Nuevo Pedido',
    pendingOrders: 'Pedidos Pendientes',
    deliveredOrders: 'Pedidos Entregados',
    orderID: 'ID Pedido',
    customer: 'Cliente',
    createdAt: 'Fecha Creación',
    deliveredAt: 'Fecha Entrega',
    itemCount: 'Items',
    total: 'Total',
    actions: 'Acciones',
    markDelivered: 'Marcar Entregado',
    noOrders: 'No hay pedidos para mostrar',
    truck: 'Camión',
    selectTruck: 'Seleccionar Camión',
    truckRequired: 'Se requiere asignar un camión',
    noTruck: 'Sin camión',
    updating: 'Actualizando',
    truckUpdated: 'Camión actualizado',
    errorUpdatingTruck: 'Error al actualizar camión',
    
    // Order creation
    selectItems: 'Seleccionar Items',
    orderSummary: 'Resumen del Pedido',
    addItem: 'Agregar Item',
    itemName: 'Nombre del Item',
    price: 'Precio',
    quantity: 'Cantidad',
    subtotal: 'Subtotal',
    orderTotal: 'Total del Pedido',
    submit: 'Enviar',
    submitting: 'Enviando...',
    cancel: 'Cancelar',
    cancelOrder: 'Cancelar Pedido',
    
    // Messages
    orderCreated: 'Pedido creado exitosamente',
    orderUpdated: 'Pedido actualizado exitosamente',
    orderDelivered: 'Pedido marcado como entregado',
    orderCanceled: 'Pedido cancelado exitosamente',
    
    // Errors
    networkError: 'Error de red. Por favor, verifica tu conexión',
    unexpectedError: 'Error inesperado',
    authError: 'Error de autenticación. Por favor, inicia sesión nuevamente',
    orderCancelError: 'Error al cancelar el pedido',
    orderNotFound: 'Pedido no encontrado',
    permissionDenied: 'No tienes permisos para realizar esta acción'
  },
  en: {
    // App header and general
    appTitle: 'Orders Management',
    allRightsReserved: 'All rights reserved',
    ordersManagementSystem: 'Orders Management System',
    loading: 'Loading...',
    refresh: 'Refresh',
    login: 'Login',
    logout: 'Log out',
    profile: 'Profile',
    dashboard: 'Dashboard',
    customers: 'Customers',
    orders: 'Orders',
    error: 'Error',
    success: 'Success',
    save: 'Save',
    delete: 'Delete',
    edit: 'Edit',
    view: 'View',
    back: 'Back',
    
    // Dashboard
    statistics: 'Statistics',
    recentOrders: 'Recent Orders',
    ordersTrend: 'Orders Trend',
    totalSales: 'Total Sales',
    averageOrderValue: 'Average Order Value',
    topCustomers: 'Top Customers',
    ordersByStatus: 'Orders by Status',
    
    // Customer search
    searchCustomer: 'Search Customer',
    searchByPhoneNumber: 'Search by phone number',
    search: 'Search',
    searching: 'Searching...',
    customerNotFound: 'Customer not found',
    errorSearchingCustomer: 'Error searching customer',
    addNewCustomer: 'Add New Customer',
    
    // Customer details
    customerDetails: 'Customer Details',
    name: 'Name',
    phoneNumber: 'Phone Number',
    address: 'Address',
    customerType: 'Customer Type',
    customerState: 'State',
    active: 'Active',
    inactive: 'Inactive',
    
    // Orders
    createOrder: 'Create Order',
    newOrder: 'New Order',
    pendingOrders: 'Pending Orders',
    deliveredOrders: 'Delivered Orders',
    orderID: 'Order ID',
    customer: 'Customer',
    createdAt: 'Created At',
    deliveredAt: 'Delivered At',
    itemCount: 'Items',
    total: 'Total',
    actions: 'Actions',
    markDelivered: 'Mark Delivered',
    noOrders: 'No orders to display',
    truck: 'Truck',
    selectTruck: 'Select Truck',
    truckRequired: 'A truck assignment is required',
    noTruck: 'No truck',
    updating: 'Updating',
    truckUpdated: 'Truck updated',
    errorUpdatingTruck: 'Error updating truck',
    
    // Order creation
    selectItems: 'Select Items',
    orderSummary: 'Order Summary',
    addItem: 'Add Item',
    itemName: 'Item Name',
    price: 'Price',
    quantity: 'Quantity',
    subtotal: 'Subtotal',
    orderTotal: 'Order Total',
    submit: 'Submit',
    submitting: 'Submitting...',
    cancel: 'Cancel',
    cancelOrder: 'Cancel Order',
    
    // Messages
    orderCreated: 'Order created successfully',
    orderUpdated: 'Order updated successfully',
    orderDelivered: 'Order marked as delivered',
    orderCanceled: 'Order canceled successfully',
    
    // Errors
    networkError: 'Network error. Please check your connection',
    unexpectedError: 'Unexpected error',
    authError: 'Authentication error. Please log in again',
    orderCancelError: 'Error canceling the order',
    orderNotFound: 'Order not found',
    permissionDenied: 'You don\'t have permission to perform this action'
  }
}

// Legacy export for backward compatibility
export const translations = messages.es
