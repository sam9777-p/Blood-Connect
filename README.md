# 🩸 BloodConnect - Smart Blood Donation App

**BloodConnect** is an intelligent Android app that connects blood donors and hospitals in real-time. It features dynamic blood inventory, request management, and a machine learning-based eligibility verification system.

## 🚀 Features

### 👤 Donor Side
- 🔐Phone number authentication (Firebase)
- Liquid Onboarding UI (with key requirement)
- Dynamic profile setup with location and FCM token
- 🏥View nearby hospitals by city (via location permissions)
- 📲 View & respond to blood requests
- ✅Eligibility Check : Uses ML model hosted on Hugging Face via custom API
- Blood donation request with full lifecycle:
  - Submit request (pending → notified → fulfilled)
  - View current request status and cancel if needed
  - See which hospital handled the request
  - View and respond to requests from hospitals
- View personal donation history
- Request screen logic:
  - Blocks new request if one is pending/notified
  - Allows new request only after fulfillment
- Request screen displays latest request using `timestamp`
- 💌 FCM notifications for blood requests and fulfillment
- 🔍QR Code Check-in: Scan at hospital to validate donor identity & confirm donation, updates inventory automatically

### 🏥 Hospital Side
- Hospital-specific profile setup
- Initialize blood inventory by blood group
- Hospital dashboard:
  - Shows all blood types with units and status (Low, Medium, High)
  - Clicking a blood group opens a prioritized contact list:
    - Notified requesters
    - Interested donors
    - Past donors (from inventory)
- Hospital can send blood requests to eligible donors via FCM
- 📊 Shimmer Loader: Visually engaging loading effects
- 🔍QR Code Scanner: Confirm donor check-in and update inventory post-donation


## 🤖 ML Integration (Eligibility Predictor)

This project integrates a Hugging Face model deployed via Flask:

**Model Repo:** [blood-ha-api](https://github.com/sam9777-p/blood-ha-api)

- Model used: `impira/layoutlm-document-qa`
- Hosted as a REST API (Flask + Render)
- Used to predict donation eligibility based on user-uploaded documents

## 🔑 Onboarding Setup (Liquid Swipe)
- The app uses Liquid Onboarding for a modern welcome experience.
- A secret key is required to enable/skip onboarding.
- Store the key securely in `github.properties` using your own GPR key:
  ```properties
  GPR_USER=your_github_username
  GPR_API_KEY=your_github_token
  ```

## 🔧 Tech Stack
- Android (Kotlin)
- Firebase Authentication, Firestore, Cloud Functions, FCM
- ViewModel + LiveData
- RecyclerView + CardView + Shimmer
- MVVM architecture (in progress)
- Lottie animations
- ML API hosted using Flask + Render
- Zxing QR Integration: For donor-hospital check-in

## 📦 Project Structure

```
com.example.blood/
├── activities/
├── fragments/
│   ├── Request.kt
│   ├── ProfileFragment.kt
│   ├── HospitalHomeFragment.kt
├── adapters/
│   ├── InventoryAdapter.kt
├── data/
│   ├── InventoryItem.kt
├── viewmodel/
│   ├── HospitalHomeViewModel.kt
├── firebase/
│   ├── CloudFunction: sendRequestNotification
```

## 🧪 How to Use

1. Clone this repo:
   ```bash
   git clone https://github.com/sam9777-p/BloodConnect
   ```

2. Add your `google-services.json` (from Firebase Console) to `/app`.

3. Set your own GPR key for onboarding in `github.properties`.

4. ML API setup:
   - Host your own [blood-ha-api](https://github.com/sam9777-p/blood-ha-api) using Render/Heroku.
   - Replace API endpoint in app with your deployed URL.

5. Build and run the app using Android Studio.

---

## 📱 Screenshots

### Donor Side - Authentication & Onboarding
<div align="center">
  <img src="https://github.com/user-attachments/assets/78f96947-f14b-44ba-876f-504bd793c98c" width="23%" />
  <img src="https://github.com/user-attachments/assets/37ffa6e1-b1f9-49ac-8739-fef47ce60a7c" width="23%" />
  <img src="https://github.com/user-attachments/assets/8cab12a4-8b5f-4742-8872-d2b3a0a96fb2" width="23%" />
  <img src="https://github.com/user-attachments/assets/6adaa6a5-7fc3-4979-a39c-54baa2187a45" width="23%" />
</div>

### Donor Side - Profile & Requests
<div align="center">
  <img src="https://github.com/user-attachments/assets/028da997-be10-4549-ba78-00eee291662c" width="23%" />
  <img src="https://github.com/user-attachments/assets/51c3dee8-1891-4a70-9127-887eac8258c2" width="23%" />
  <img src="https://github.com/user-attachments/assets/a0da1644-a2ce-4769-83e1-dbaae3e343b3" width="23%" />
  <img src="https://github.com/user-attachments/assets/eac23b16-a793-4a13-8bd0-a47fc0c8810a" width="23%" />
</div>

### Donor Side - Eligibility & QR Check-in
<div align="center">
  <img src="https://github.com/user-attachments/assets/486ef583-aa1b-41eb-bf68-b6f29f402a55" width="23%" />
  <img src="https://github.com/user-attachments/assets/84d2dd41-cacd-42f0-9d0b-00879811fd90" width="23%" />
  <img src="https://github.com/user-attachments/assets/b0c8c343-382b-40fb-987a-7ce2d11f181d" width="23%" />
  <img src="https://github.com/user-attachments/assets/301052ec-b81f-4a2a-af65-f237b880bcee" width="23%" />
</div>

### Hospital Side - Dashboard & Blood Inventory
<div align="center">
  <img src="https://github.com/user-attachments/assets/177e0414-8665-45c1-a522-74f90a6d8a9b" width="23%" />
  <img src="https://github.com/user-attachments/assets/60f3c8e1-64f3-4a57-b340-a302dae45631" width="23%" />
  <img src="https://github.com/user-attachments/assets/0e2e41df-9d65-4b91-bd00-cc8275e241ca" width="23%" />
  <img src="https://github.com/user-attachments/assets/13998f39-a568-4fe6-9bff-39fe3ec17685" width="23%" />
</div>

### Hospital Side - Donor Management & QR Scanner
<div align="center">
  <img src="https://github.com/user-attachments/assets/00982650-dbc1-403d-8859-f978b7d3b3f4" width="23%" />
  <img src="https://github.com/user-attachments/assets/bce81b1b-eed2-4066-b421-6e8e61fc9832" width="23%" />
  <img src="https://github.com/user-attachments/assets/cf79965c-7b1f-470e-a824-d62092954007" width="23%" />
  <img src="https://github.com/user-attachments/assets/b6dece8b-90e9-40f9-ba5e-b175a0eb0ab2" width="23%" />
</div>

### Additional Features
<div align="center">
  <img src="https://github.com/user-attachments/assets/b3a0019f-2a1d-4f9a-b222-4a5901b65129" width="23%" />
  <img src="https://github.com/user-attachments/assets/6adaf7bd-3244-4a73-a05a-af8ea6d90601" width="23%" />
</div>

---

## ✨ Upcoming Features
- Full MVVM refactor
- Admin panel for managing hospitals
- Real-time chat UI enhancements
- More analytics for hospitals

## 🤝 Contributing

Pull requests and feature ideas are welcome!

## 📄 License

MIT License. See [LICENSE](LICENSE).
