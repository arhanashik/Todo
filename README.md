# To-do

## Description
It's a simple but complete to-do app which was built using:
- Kotlin
- AndroidX
- firebase 
- firebase auth 
- firebase auth UI and
- firestore

Users can sign up with email and password or can continue with google and can manage their to-do list with it.

## Features
- Continue with google
- Sign up with email and password
- Simple user profile
- Create to-do with title and date
- Edit incomplete tasks
- Delete completed/incomplete task(s)
- Mark any task as completed/incomplete
- **Bulk update and delete tasks**
- Filter to-do list based on login user
- Switch account easily
- **Real time update**

## Project structure
- The root folder of the kotlin codes are `todo`
- Under `todo` the `app` folder holds the `data` and `ui` portion
- `data` folder contains the `entities`, `constant` values used in the app
- `ui` folder holds the UI related codes like `activity`, `adapter`, `viewholder` etc
- The `util` folder contains the helper classes and the library classes
- Under `lib` -> `firebase` folder all firebase and firestore related codes are stored
- `res` folder contains the resources, ui designs etc

## How to use
- Clone the repo
- Open with android studio
- Run the app
- If you want to use user own firebase account then create the app from your firebase console
- and change the google-services.json file with yours

## Contact
If you have any queries please feel free to contact me at `ashik.pstu.cse@gmail.com`