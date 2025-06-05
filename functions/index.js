const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.handleRequestStatusChange = functions
  .region("us-central1")
  .firestore
  .document("BloodRequests/{requestId}")
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();
    const requestId = context.params.requestId;

    const statusBefore = before.status;
    const statusAfter = after.status;

    const bloodGroup = after.bloodGroup;
    const city = after.city;
    const requesterName = after.requesterName;
    const requesterPhone = after.requester;

    // --- Notify donors ---
    if (statusBefore !== "notified" && statusAfter === "notified") {
      if (!bloodGroup || !city || !requesterName) {
        console.warn("Missing required fields.");
        return null;
      }

      try {
        const donorsSnapshot = await admin.firestore()
          .collection("Accounts")
          .where("role", "==", "Donor")
          .where("city", "==", city)
          .where("bloodGroup", "==", bloodGroup)
          .get();

        const donors = donorsSnapshot.docs
          .map(doc => doc.data())
          .filter(d => typeof d.fcmToken === "string" && d.fcmToken.length > 0);

        let sent = 0;
        for (const donor of donors) {
          const message = {
            token: donor.fcmToken,
            notification: {
              title: `Urgent Blood Request (${bloodGroup})`,
              body: `${requesterName} needs ${bloodGroup} blood in ${city}. Tap to help!`
            },
            data: {
              type: "blood_request",
              requestId: requestId,
              requesterName: requesterName,
              bloodGroup: bloodGroup,
              city: city,
              units: (after.units || "1").toString(),
              handledBy: after.handledBy || "Unknown Hospital"
            }
          };

          try {
            await admin.messaging().send(message);
            console.log(`✅ Notified donor: ${donor.firstName || "Donor"}`);
            sent++;
          } catch (err) {
            console.error(`❌ Failed to notify donor:`, err.message);
          }
        }

        console.log(`✅ Donor notifications sent: ${sent}/${donors.length}`);
      } catch (error) {
        console.error("❌ Error notifying donors:", error);
      }
    }

    // --- Notify requester ---
    if (statusBefore !== "fulfilled" && statusAfter === "fulfilled") {
      if (!requesterPhone) {
        console.warn("⚠️ Requester phone number missing.");
        return null;
      }

      try {
        const requesterSnapshot = await admin.firestore()
          .collection("Accounts")
          .where("phoneNumber", "==", requesterPhone)
          .limit(1)
          .get();

        if (requesterSnapshot.empty) {
          console.warn("❌ Requester not found.");
          return null;
        }

        const requesterData = requesterSnapshot.docs[0].data();
        const token = requesterData.fcmToken;

        if (!token) {
          console.warn("⚠️ Requester has no FCM token.");
          return null;
        }

        const message = {
          token,
          notification: {
            title: "Blood Request Fulfilled 🎉",
            body: "Your blood request has been fulfilled. Tap to follow up or collect."
          },
          data: {
            type: "request_fulfilled",
            bloodGroup: bloodGroup,
            units: (after.units || "1").toString(),
            handledBy: after.handledBy || "Unknown Hospital"
          }
        };

        await admin.messaging().send(message);
        console.log(`✅ Fulfillment notification sent to requester: ${requesterData.firstName || "User"}`);
      } catch (error) {
        console.error("❌ Error notifying requester:", error);
      }
    }

    return null;
  });
