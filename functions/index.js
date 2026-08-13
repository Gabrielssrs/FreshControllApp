// Archivo: index.js (Firebase Functions)
const functions = require('firebase-functions/v1'); // <-- ¡Aquí está el cambio!
const admin = require('firebase-admin');
admin.initializeApp();

exports.checkLowStock = functions.firestore
    .document('products/{productId}')
    .onUpdate(async (change, context) => {
        const newData = change.after.data();
        const previousData = change.before.data();

        // 1. Detectar si el stock bajó del límite
        if (newData.currentStock <= newData.minStock && previousData.currentStock > newData.minStock) {
            const storeId = newData.storeId;

            // 2. Buscar al DUEÑO de esa tienda
            const usersSnapshot = await admin.firestore().collection('users')
                .where('storeId', '==', storeId)
                .where('role', '==', 'OWNER')
                .get();

            // 3. Enviar la notificación a todos los dispositivos del dueño
            usersSnapshot.forEach(doc => {
                const userData = doc.data();
                if (userData.fcmToken) {
                    const message = {
                        notification: {
                            title: '⚠️ Stock Crítico',
                            body: `¡Atención! El producto "${newData.name}" se está agotando.
                            Quedan solo ${newData.currentStock} unidades.`
                        },
                        token: userData.fcmToken
                    };
                    admin.messaging().send(message);
                }
            });
        }
    });

