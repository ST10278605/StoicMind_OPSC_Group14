Stoic Mind

## Overview

**Stoic Mind** is an Android application that was created to help users with personal growth, self-reflection, motivation, and building better daily habits using ideas inspired by Stoic philosophy.

The app includes features such as daily quotes, motivational content, journaling, mood and reflection features, reminders, and settings that allow users to change certain preferences.

The application was developed using **Android Studio and Kotlin**. We also used **Firebase Authentication** for user registration and login, **Firebase Firestore** for storing user data, and **Retrofit** to connect the app to a REST API.

For Part 2, we took the ideas and features researched in Part 1 and worked on turning them into a working Android application. We also focused on testing the application, fixing errors, improving the API connection, checking Firebase, handling invalid inputs, and making sure the application works properly.

---
Video link: https://advtechonline-my.sharepoint.com/:v:/g/personal/st10446802_rcconnect_edu_za/IQDnA8kxDbTDQJTi2aZ9x_tvAQB8I1kEhtTXWMyCYEz4AvE?nav=eyJyZWZlcnJhbEluZm8iOnsicmVmZXJyYWxBcHAiOiJPbmVEcml2ZUZvckJ1c2luZXNzIiwicmVmZXJyYWxBcHBQbGF0Zm9ybSI6IldlYiIsInJlZmVycmFsTW9kZSI6InZpZXciLCJyZWZlcnJhbFZpZXciOiJNeUZpbGVzTGlua0NvcHkifX0&e=cydiSH
GitHub link: https://github.com/ST10278605/StoicMind_OPSC_Group14.git

## Technologies Used

The following technologies and tools were used during the development of Stoic Mind:

* **Android Studio** – Used to develop the Android application.
* **Kotlin** – Used for the application's functionality and logic.
* **XML** – Used to design the user interface.
* **Firebase Authentication** – Used for registration and login.
* **Firebase Firestore** – Used to store and retrieve user data.
* **Retrofit** – Used to connect the application to the REST API.
* **REST API** – Used to retrieve quote information.
* **GitHub** – Used for version control and collaboration.
* **GitHub Actions** – Used for automated builds and testing.
* **JUnit / Android Unit Testing** – Used to test parts of the application.

---

## Main Features

### 1. Registration and Login

Users are able to register for an account and log into the application.

Firebase Authentication is used to handle the registration and login process instead of storing passwords directly in the application's database.

We checked the following:

* User registration
* User login
* Incorrect login details
* Invalid registration information
* Authentication errors
* Password security
* User access after logging in

---

### 2. Dashboard

The Dashboard is the main screen of the application and gives users access to the main Stoic Mind features.

The Dashboard includes the daily quote and other relevant content.

During Part 2, we worked on fixing the connection between the Dashboard and the quote API so that quotes could be retrieved and displayed correctly.

We also added error handling so that the application can respond properly if something goes wrong while retrieving the quote.

---

### 3. REST API Integration

The application uses a REST API to retrieve quote information.

**Retrofit** was used to handle the communication between the Android application and the API.

During development, the REST API integration was checked and fixed to make sure that:

* Requests are sent correctly.
* API responses are received correctly.
* Retrieved information is displayed in the application.
* API errors are handled.
* The application does not crash when the API is unavailable.

---

### 4. API Error Handling

We added error handling to deal with situations where the application cannot retrieve information from the API.

This includes situations such as:

* No internet connection
* API server problems
* Failed requests
* Empty responses
* Invalid responses

Instead of simply crashing, the application can provide feedback when an error occurs.

---

### 5. Journal

The Journal feature allows users to write down their thoughts and personal reflections.

Users can create journal entries and view their previous entries.

The journal functionality was checked to make sure that:

* New journal entries can be created.
* Entries are saved correctly.
* Previous entries can be retrieved.
* Journal history can be viewed.
* User information is kept associated with the correct account.

---

### 6. Settings / Change Settings

A Settings section was included to allow users to manage the available application preferences.

This is linked to the customization requirements identified during Part 1. The settings allow users to change relevant preferences within the application.

The Settings functionality was checked to make sure that changes made by the user are handled correctly.

---

### 7. Part 1 Features

The features researched and proposed during Part 1 were used as a guide when developing the application.

The relevant features include:

* Simple and accessible user interface
* Daily Stoic inspiration
* Motivational and philosophical content
* Customizable content
* Journaling
* Mood and reflection functionality
* Habit and routine support
* Categorized content
* Reminders and notifications
* Quote and reflection features
* Daily inspirational content

The implemented features were checked during Part 2 to make sure they worked as expected.

---

## Firebase

### Firebase Authentication

Firebase Authentication is used to manage user registration and login.

We checked the authentication process to make sure that users can:

* Create an account.
* Log into their account.
* Receive feedback when incorrect details are entered.
* Access the application after successfully logging in.

### Firebase Firestore

Firebase Firestore is used as the cloud database for the application.

It is used to store relevant user information, including data needed for the Journal feature.

The Firestore connection was checked to make sure that information can be saved and retrieved correctly.

---

## Password Security

Password security was also checked during development.

Passwords are handled through **Firebase Authentication** rather than being stored directly in the application's Firestore database.

This helps prevent user passwords from being stored as normal application data.

The registration process also checks that the required information is provided before an account can be created.

---

## Invalid Input Handling

We checked how the application responds when users enter incorrect or incomplete information.

Examples include:

* Empty fields
* Invalid email addresses
* Incorrect login details
* Invalid registration information
* Missing journal information
* API errors

The purpose of this was to make sure that invalid information does not cause unexpected behaviour or crash the application.

---

## Unit Testing

Automated unit tests were added as part of Part 2.

The tests were used to check important parts of the application and help identify problems during development.

This also makes it easier to check whether changes made to the application have affected existing functionality.

---

## GitHub Actions

A **GitHub Actions workflow** was added to the project.

The workflow helps automate parts of the testing and development process when changes are pushed to the GitHub repository.

This provides an additional way of checking for build or testing problems during development.

---

## Retrofit Dependencies

The Retrofit dependencies required for the REST API connection were checked and confirmed.

Retrofit is responsible for communicating with the external API and receiving the information returned by it.

The required dependencies and converter were also checked to make sure that API responses can be converted into objects that can be used by the Kotlin code.

---

## GitHub and Version Control

GitHub was used throughout the development of the project to keep track of changes and work done on the application.

Commits and pushes were made during development to keep the project updated on the repository.

GitHub helped us to:

* Keep track of development changes.
* Save different versions of the project.
* Work as a team.
* Keep a history of our work.
* Back up the project remotely.

---

## Comments, References and Logging

Comments were added to parts of the code where they were useful for explaining what the code was doing.

References were also included where external resources or technologies were used.

Logging and debugging were used during development to help identify problems with areas such as:

* REST API communication
* Firebase Authentication
* Firestore
* Database operations
* Application errors

This helped us find and fix issues while developing the application.

---

## Demo Video

A demo video with a voice-over was created to demonstrate the Stoic Mind application.

The video shows the main features of the application and explains how the different parts of the app work.

It also demonstrates the application running as an Android application.

---

## AI Use Statement

AI tools were used during the development of the Stoic Mind application as a supporting resource.

AI was used to help with things such as:

* Understanding Kotlin and Android Studio concepts.
* Troubleshooting errors.
* Understanding API and Firebase implementation.
* Finding possible solutions to development problems.
* Improving documentation and explanations.
* Reviewing parts of the implementation.

The suggestions provided by AI were reviewed and tested before being used. The development team remained responsible for implementing, testing, and checking the final application.

---

## Project Structure

The project follows the normal structure of an Android Studio application.

It contains Kotlin files, XML layouts, resources, configuration files, and testing components.

The main areas of the application include:

* Authentication
* Dashboard
* Journal
* Settings
* REST API communication
* Firebase
* User interface
* Unit testing

---

## Installation and Setup

### Requirements

To run the Stoic Mind application, you will need:

* Android Studio
* Android SDK
* Kotlin support
* An Android emulator or Android device
* Internet connection for Firebase and API functionality

### Running the Application

1. Clone the project from GitHub.
2. Open the project in Android Studio.
3. Allow Android Studio to sync the Gradle files.
4. Make sure the Firebase configuration is correctly included.
5. Connect an Android emulator or physical Android device.
6. Build the project.
7. Run the application.

---

## Conclusion

Stoic Mind was developed to bring together Stoic philosophy, motivation, self-reflection, and journaling in one Android application.

In Part 2, we focused on taking the ideas from our Part 1 research and turning them into a working application. We worked on areas such as the REST API, Dashboard quote connection, Firebase Authentication, Firestore, settings, journal history, input validation, password security, and error handling.

We also added automated unit tests and a GitHub Actions workflow to help with testing and development. GitHub was used throughout the project to keep track of our work and changes.

Overall, Part 2 allowed us to take the concepts we researched in Part 1 and apply them practically by developing, testing, debugging, and improving the Stoic Mind Android application. 
