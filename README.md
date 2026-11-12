# DineNest

An Android app for highway travellers to **pre-order food, book rest rooms and find EV charging stations** along their route, so every stop is quick and planned.

Built as a college project in Kotlin with Jetpack Compose and Firebase.

## The problem

On long trips such as Mumbai to Pune, travellers lose time searching for a food mall, ordering, and waiting for their food. Riders who travel long distances also struggle to find a safe place to rest, and EV owners have to plan around slow charging.

## Features

**Available now**
- Registration with name, username, email and password, with email verification
- Login with email or username, and a persistent session that skips the login screen
- Password rules: at least 6 characters, one letter, one number and one special character
- Live list of highway food malls with ratings and facilities (real-time Firestore updates)
- Separate Rest and EV tab with rest rooms and EV charging stations, including capacity and price
- Save favourite malls, rest rooms and EV stations
- Profile page with optional mobile number and home location
- Change password and change email from Settings
- Dark mode that is remembered between launches

**In progress**
- Menu, cart and food pre-order
- Live location, ETA and a smart cooking alert
- Seat and rest room booking
- Admin app for food mall owners

## Tech stack

| Area | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Authentication | Firebase Authentication (email and password) |
| Database | Cloud Firestore |
| Local storage | SharedPreferences (theme) |
| Planned | Google Maps, Geofencing, Razorpay (test mode), Room |

## Project structure

```
app/src/main/java/com/dinenest/user/
├── MainActivity.kt          App entry point and screen flow
├── data/                    Firebase and storage code
├── model/                   Data classes
└── ui/theme/                Screens, components and theme
```

## Firestore collections

| Collection | Purpose |
|---|---|
| `foodMalls` | Highway food malls |
| `facilities` | Rest rooms (`type = restroom`) and EV stations (`type = ev`) |
| `users` | Profile data, with a `saved` subcollection for favourites |
| `usernames` | Maps a username to an email so users can log in with either |

## Run it locally

1. Clone the repository and open it in Android Studio.
2. Create a Firebase project and add an Android app with the package name `com.dinenest.user`.
3. Download `google-services.json` and place it in the `app/` folder. See `app/google-services.json.example` for the expected shape. The real file is not in this repository.
4. In the Firebase console, enable **Authentication > Email/Password** and create a **Firestore Database**.
5. Add a few documents to the `foodMalls` and `facilities` collections. Field names match the classes in the `model` folder.
6. Sync Gradle, then run on an emulator or a phone.

## Security note

The Firestore rules are in test mode while the project is under development. They need tightening before any real-world use.

## Author

Atharva Mithilesh, MCA VJTI