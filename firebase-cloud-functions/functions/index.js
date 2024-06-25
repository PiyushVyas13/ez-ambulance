/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const logger = require("firebase-functions/logger");
const {onDocumentDeleted, onDocumentCreated} = require("firebase-functions/v2/firestore");
const {onCall, HttpsError} = require("firebase-functions/v2/https");
const {getMessaging} = require("firebase-admin/messaging");
const {initializeApp, applicationDefault} = require("firebase-admin/app");
const {getAuth} = require("firebase-admin/auth");
const {getFirestore} = require("firebase-admin/firestore");
const crypto = require("crypto");

// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });

const app = initializeApp({
    credential: applicationDefault(),
});
const db = getFirestore();


exports.deleteEmployee = onDocumentDeleted("/employees/{employeeMail}", (event) => {
    const snap = event.data;

    if (!snap) {
        return logger.error("No data associated with the snapshot.");
    }
    const data = snap.data();
    const userId = data.user_id;

    if (!userId) {
        return logger.error("User id field did not exist in the document");
    }

    logger.log("Beggining deletion operation");
    return getAuth(app).deleteUser(userId)
        .then(() => {
            return logger.log("Deleted user account" + userId);
        })
        .catch((error) => {
            return logger.error("Unable to delete account" + error);
        })
        .finally(() => {
            logger.log("finished function");
        });
});

const generateDummyPassword = (length) => {
    const chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    let password = "";

    for (let i=0; i<length; i++) {
        const index = crypto.randomInt(chars.length);
        password += chars.charAt(index);
    }

    return password;
};

exports.addEmployee = onDocumentCreated("/employees/{employeeMail}", (event) => {
    const snap = event.data;

    if (!snap) {
        return logger.error("No data associated with the snapshot");
    }

    const data = snap.data();
    const employeeEmail = event.params.employeeMail;
    // const ownerId = event.params.ownerId;

    logger.log("Creating User...");
    return getAuth(app).createUser({
        email: employeeEmail,
        password: generateDummyPassword(6),
        phoneNumber: data.phone_number,
        displayName: data.name,
    })
        .then((userRecord) => {
            logger.log("Successfully created new user", userRecord.uid);
            logger.log("Starting update operation...");
            return db.collection("employees").doc(employeeEmail).update({user_id: userRecord.uid})
                .then((result) => {
                    return logger.log("userId added to employee at:", result.writeTime);
                })
                .catch((error) => {
                    return logger.error("cannot update document", error);
                })
                .finally(() => {
                    return logger.log("finished update document operation");
                });
        })
        .catch((error) => {
            return logger.log("Cannot create user", error);
        })
        .finally(() => {
            return logger.log("Finished addEmployee");
        });
});

exports.notifyDriver = onCall((request) => {
    logger.log(request.data);
    const registrationToken = request.data.fcm_token || null;
    const tripId = request.data.trip_id || null;


    if (registrationToken === null) {
        logger.error("registration token not provided");
        throw new HttpsError("invalid-argument", "registration token not provided");
    }

    if (tripId === null) {
        logger.error("Trip ID not provided");
        throw new HttpsError("invalid-argument", "trip Id was absent");
    }

    if (!request.auth) {
        throw new HttpsError("unauthenticated", "You must be logged in to send messages");
    }

    const message = {
        notification: {
            title: "New Request received",
            body: `You have a new drive request`,
        },
        data: {
            tripId: tripId,
        },
        token: registrationToken,
    };

    logger.log("Sending message...");
    return getMessaging().send(message)
        .then((data) => {
            return logger.log("Message sent successfully!");
        })
        .catch((error) => {
            throw new HttpsError("invalid-argument", error);
        })
        .finally(() => {
            logger.log("Finished sending message");
        });
});
