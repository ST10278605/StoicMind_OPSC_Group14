# Stoic Mind

## Presentation link:
## Github link: https://github.com/ST10278605/StoicMind_OPSC_Group14/ 

## Purpose

**Stoic Mind** is an Android application designed to help users build emotional resilience, self-discipline, and healthier daily habits through Stoic-inspired reflection and personal growth tools. The app blends mindfulness, journaling, mood tracking, habit development, reminders, and motivational guidance into one experience that supports better routines and deeper self-awareness.

The purpose of the app is to make personal development practical and accessible. Instead of overwhelming the user with complicated tools, Stoic Mind encourages short, meaningful actions such as reflecting on the day, tracking mood, reviewing progress, and building calm, consistent habits aligned with Stoic principles.

## Design considerations

The application was designed with the following principles in mind:

- Simplicity and clarity: users should be able to move between core activities quickly through a dashboard and bottom navigation.
- Daily reflection: the app promotes consistency through quick prompts that encourage users to pause, evaluate, and improve.
- Personalization: users can set reminders, customize appearance, change language settings, and create their own habits.
- Motivation and progress: achievement badges, wellness categories, and structured prompts help preserve engagement over time.
- Accessibility: the interface uses clear labels, easy navigation, and straightforward actions to reduce friction for users.
- Trust and security: user accounts are managed using Firebase Authentication, and cloud data is separated from local app state where appropriate.
- Resilience: the app supports offline saving and sync status feedback so users are not blocked by temporary connectivity issues.

## App overview

Stoic Mind includes the following major areas:

- Registration and login
- Dashboard with a daily Stoic quote
- Daily reflection activities
- Journal and mood tracking
- Habit management
- Explore section with Stoic-inspired learning categories
- Reminders and notifications
- Progress and achievements
- Profile and customization settings

## Technologies used

The app was developed using:

- Android Studio
- Kotlin
- XML layouts
- Firebase Authentication
- Firebase Firestore
- Room database
- Retrofit for REST API access
- GitHub for version control and collaboration
- GitHub Actions for CI automation
- JUnit and Android unit testing tools

## Main features

### 1. Authentication

Users can create an account and sign in securely through Firebase Authentication. Registration includes validation for required details, valid email format, and minimum password length. This helps protect the user experience and prevents poor or incomplete account creation.

Screenshot: 
<img width="738" height="1089" alt="image" src="https://github.com/user-attachments/assets/18e76f90-d298-4d72-b52e-a949044c2eef" />


### 2. Dashboard

The Dashboard acts as the primary home screen. It displays the current date, a greeting for the user, and a daily quote fetched via a REST API. If the quote API is unavailable, the app uses a default offline Stoic quote to maintain a positive user experience.

Screenshot: <img width="738" height="1600" alt="1" src="https://github.com/user-attachments/assets/352a58a0-19fc-46ac-9a32-b50b3a080ab2" />

### 3. Daily reflection

Users can complete a brief daily reflection by entering a focus, what went well, and what they want to improve. This supports Stoic principles of self-examination and continuous improvement.

Screenshot:<img width="738" height="1600" alt="3" src="https://github.com/user-attachments/assets/a8241dab-2f00-43ee-a6be-2935aff657ce" />


### 4. Journal and mood tracking

Users can store personal thoughts in a journal and complete mood check-ins. Entries are saved locally and can be synced with the cloud for users who are signed in. Mood notes and journal content are linked to support deeper reflection over time.

Screenshot:<img width="738" height="1600" alt="2" src="https://github.com/user-attachments/assets/d803be40-c36d-4a87-9757-79ef8cc49fb5" />


### 5. Habit tracker

The app includes a habit tracking feature with default habits such as reading, journaling, studying, exercising, and meditating. Users can also create their own habits, mark them complete, and keep their progress synchronized across local storage and cloud data.

Screenshot:<img width="738" height="1600" alt="5" src="https://github.com/user-attachments/assets/3989629d-5eb9-4abb-a176-2f2dc1e20e4b" />


### 6. Explore screen

The Explore section organizes Stoic-inspired learning topics such as resilience, self-discipline, gratitude, mindfulness, relationships, and leadership. This supports both general educational value and personal growth.

Screenshot:<img width="738" height="1600" alt="6" src="https://github.com/user-attachments/assets/2721c7c9-c275-40fc-960e-8582fc6cbe5d" />


### 7. Reminders

Users can set morning and evening reminder times to encourage daily self-reflection, personal review, and better routine building. The app also supports a test notification action for quick reminder validation.

Screenshot:<img width="738" height="1600" alt="7" src="https://github.com/user-attachments/assets/c8807050-8f67-4c5f-a5c1-e67479be789c" />


### 8. Profile and settings

Users can access settings for personal details, language preferences, appearance themes, notifications, and privacy-related links. This improves usability and makes the app more personal and adaptable.

Screenshot:<img width="738" height="1600" alt="8" src="https://github.com/user-attachments/assets/84c0112c-30e3-48c8-a65b-833bbda1f70f" />


### 9. Progress and achievements

The achievements section tracks user progress using badge-style cards. This adds a gamified motivation element to encourage sustained usage of the app and to reward continued engagement.

Screenshot:<img width="738" height="1600" alt="9" src="https://github.com/user-attachments/assets/248c7a3d-0a76-45ce-b634-7e7ffe526e98" />


## Firebase and cloud data

Firebase was used to provide authentication and cloud persistence for account and user-related data. Firestore stores user information and allows the app to synchronize some settings, reminder configuration, and journal-related data when connectivity is available.

This approach supports both real-time user data access and a backup mechanism for when the app is used offline. The app is designed to save data locally first and then sync when possible.

## GitHub and GitHub Actions

GitHub was used throughout the project as the central repository for source control, documentation, collaboration, and version tracking. Commits and pushes helped the team store and review progress across the development lifecycle.

GitHub Actions was added to automate validation during development. The workflow builds the Android project and runs unit tests whenever the project is pushed or a pull request is created. This provides faster feedback and helps identify issues early before deployment or submission.

The GitHub Actions workflow file is located here:

- .github/workflows/android.yml

The workflow performs the following:

- checks out the project
- sets up JDK 17
- sets up Gradle
- runs unit tests with `./gradlew testDebugUnitTest`
- builds the debug APK with `./gradlew assembleDebug`

## Release notes for Part 3 final POE

The following release notes describe the updates made to Stoic Mind since the prototype stage and explain which features are considered innovative additions to this final version.

### Version 3.0 — Final POE Release

#### Summary

This final version expands the prototype into a more complete Stoic self-improvement app. It improves core daily workflows, adds stronger user personalization, enhances synchronization, and strengthens the app's educational and motivational value.

#### Major updates

- Added a complete daily reflection experience with guided prompts for focus, wins, and improvement areas.
- Added mood tracking with optional notes so users can connect emotional state to reflection and personal growth.
- Added local persistence for journal entries and mood data using Room, with sync status indicators for offline/online states.
- Added manual sync controls and sync-state banners so users understand whether their data is saved locally or stored in the cloud.
- Expanded the habit tracker with default activities, custom habit creation, completion tracking, and persistence across sessions.
- Added reminder functionality for morning and evening reflection routines with customizable times.
- Added a richer Explore section with structured Stoic topics to support learning and self-improvement.
- Added progress and achievement badges for motivation and user engagement.
- Improved profile settings with theme selection, language options, notification access, and logout support.
- Improved the dashboard by integrating a quote API using Retrofit and providing an offline backup quote when needed.
- Improved the app’s reliability through validation, error handling, and automated CI testing.

#### Innovative features added in this version

The following features are considered the most innovative additions in this final version:

- Innovative Feature 1: Guided Daily Reflection System
  - This feature turns Stoic self-examination into a structured daily routine by asking the user to identify focus, wins, and growth areas.
  - It promotes consistency and reflection rather than passive content consumption.

- Innovative Feature 2: Mood Check-In Integrated with Journal Entries
  - Users can log emotional state and optionally add a note; the system also stores this as part of the journaling flow.
  - This provides a stronger bridge between emotional awareness and personal development.

- Innovative Feature 3: Offline-First Sync Model
  - The app saves entries locally first and shows whether they are synced, syncing, or offline.
  - This makes the app resilient and more reliable when internet access is weak or unavailable.

- Innovative Feature 4: Personalized Habit System
  - Users can add custom habits and track completion over time, creating a habit-building journey tailored to their daily life.
  - This makes the app more than a quote generator; it becomes a practical habit companion.

- Innovative Feature 5: Stoic Learning Exploration
  - Users can navigate categories of Stoic concepts and explore themes such as resilience, leadership, gratitude, and mindfulness.
  - This adds educational depth and long-term value.

#### Improvements since prototype

- The prototype had basic concept features and a foundation for the app. The final version improved the product by adding real workflow integration, practical personalization, and more complete data handling.
- The app now feels like a usable self-improvement application rather than a demo-only concept.
- More user value was added through observable progress, reminder support, and handling of app state in offline conditions.

## Unit testing and validation

Automated unit tests were added to help ensure that the application remained stable as features were developed. The project also includes Android testing support and can be checked using Gradle test tasks.

## Installation and setup

### Requirements

- Android Studio
- Android SDK
- Kotlin support
- An Android emulator or Android device
- Internet access for Firebase and the quote API

### Steps to run

1. Clone the repository from GitHub.
2. Open the project in Android Studio.
3. Allow Gradle to sync the project.
4. Ensure Firebase configuration is set up correctly.
5. Run the app on an Android emulator or physical device.

## Conclusion

Stoic Mind was created to help users incorporate Stoic values into daily life through reflection, mood awareness, habits, and personal growth routines. The final Part 3 version improves on the prototype by delivering a more complete, personalized, and resilient Android experience. This update includes new features such as daily reflection, mood journaling, offline-first data handling, reminder workflows, learning categories, and achievement tracking, making the app significantly more robust and user-focused.

## AI use statement

AI tools were used to support the project by helping with Android/Kotlin understanding, error-solving, code review, and documentation support. The suggestions provided were evaluated by the project team and adapted where appropriate to ensure that the final implementation remained correct, consistent, and aligned with project goals.
